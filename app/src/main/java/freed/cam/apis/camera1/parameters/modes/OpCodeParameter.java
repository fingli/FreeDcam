/*
 *
 *     Copyright (C) 2015 Ingo Fuchs
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * /
 */

package freed.cam.apis.camera1.parameters.modes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import freed.cam.apis.basecamera.parameters.modes.AbstractModeParameter;
import freed.utils.AppSettingsManager;
import freed.utils.FreeDPool;
import freed.utils.Logger;
import freed.utils.StringUtils;


/**
 * Created by troop on 28.04.2016.
 */
public class OpCodeParameter extends AbstractModeParameter
{
    private final String TAG = OpCodeParameter.class.getSimpleName();
    private boolean hasOp2;
    private boolean hasOp3;
    private boolean OpcodeEnabled = true;
    private final boolean isSupported;
    private final AppSettingsManager appSettingsManager;
    public OpCodeParameter(AppSettingsManager appSettingsManager)
    {
        this.appSettingsManager = appSettingsManager;
        File op2 = new File(StringUtils.GetFreeDcamConfigFolder+"opc2.bin");
        if (op2.exists())
            hasOp2 =true;
        File op3 = new File(StringUtils.GetFreeDcamConfigFolder+"opc3.bin");
        if (op3.exists())
            hasOp3 =true;
        isSupported = true;

    }

    //https://github.com/troop/FreeDcam/blob/master/camera1_opcodes/HTC_OneA9/opc2.bin?raw=true
    @Override
    public void SetValue(String valueToSet, boolean setToCamera)
    {
        if(valueToSet.equals("Download")) {
            if (hasOp2 || hasOp3)
                return;
            final String urlopc2 = "https://github.com/troop/FreeDcam/blob/master/camera1_opcodes/" + appSettingsManager.getDevice().name() + "/opc2.bin?raw=true";
            final String urlopc3 = "https://github.com/troop/FreeDcam/blob/master/camera1_opcodes/" + appSettingsManager.getDevice().name() + "/opc3.bin?raw=true";
            FreeDPool.Execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpsGet(urlopc2, "opc2.bin");
                    } catch (IOException e) {
                        Logger.exception(e);
                    }
                }
            });
            FreeDPool.Execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        httpsGet(urlopc3, "opc3.bin");
                    } catch (IOException e) {
                        Logger.exception(e);
                    }
                }
            });
        }
        else OpcodeEnabled = !valueToSet.equals("Disabled");
    }

    @Override
    public boolean IsSupported() {
        return isSupported;
    }

    @Override
    public String GetValue() {
        return (hasOp2 || hasOp3) +"";
    }

    @Override
    public String[] GetValues() {
        return new String[] {"Enabled,Disabled,Download"};
    }

    @Override
    public boolean IsVisible() {
        return isSupported;
    }

    private void httpsGet(String url, String fileending) throws IOException {
        HttpsURLConnection  httpConn = null;
        InputStream inputStream = null;

        // Open connection and input stream
        try {
            trustAllHosts();
            URL urlObj = new URL(url);
            httpConn = (HttpsURLConnection ) urlObj.openConnection();
            httpConn.setHostnameVerifier(OpCodeParameter.DO_NOT_VERIFY);
            httpConn.setRequestMethod("GET");
            httpConn.setConnectTimeout(15000);
            httpConn.setReadTimeout(3000);
            httpConn.connect();

            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();
            }
            if (inputStream == null) {
                Logger.w(TAG, "httpGet: Response Code Error: " + responseCode + ": " + url);
                throw new IOException("Response Error:" + responseCode);
            }
        } catch (SocketTimeoutException e) {
            Logger.w(TAG, "httpGet: Timeout: " + url);
            throw new IOException();
        } catch (MalformedURLException e) {
            Logger.w(TAG, "httpGet: MalformedUrlException: " + url);
            throw new IOException();
        } catch (IOException e) {
            Logger.w(TAG, "httpGet: " + e.getMessage());
            if (httpConn != null) {
                httpConn.disconnect();
            }
            throw e;
        }

        // Read stream as String
        FileOutputStream responseBuf = null;
        File file = new File(StringUtils.GetFreeDcamConfigFolder+fileending);
        try {

            responseBuf = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=inputStream.read(buf))>0){
                responseBuf.write(buf,0,len);
            }
            responseBuf.flush();
        } catch (IOException e) {
            Logger.w(TAG, "httpGet: read error: " + e.getMessage());
            file.delete();
            throw e;
        } finally {
            try {
                if (responseBuf != null)
                    responseBuf.close();
            } catch (IOException e) {
                Logger.w(TAG, "IOException while closing BufferedReader");
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Logger.w(TAG, "IOException while closing InputStream");
            }
            BackgroundValueHasChanged("true");
        }
    }

    // always verify the host - dont check for certificate
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * Trust every server - dont check for any certificate
     */
    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain,
                                           String authType) throws CertificateException {
            }
        } };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
