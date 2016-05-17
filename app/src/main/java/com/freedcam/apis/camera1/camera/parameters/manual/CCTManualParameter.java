package com.freedcam.apis.camera1.camera.parameters.manual;

import android.hardware.Camera;

import com.freedcam.apis.basecamera.camera.parameters.AbstractParameterHandler;
import com.freedcam.apis.camera1.camera.parameters.CamParametersHandler;
import com.freedcam.utils.DeviceUtils;
import com.freedcam.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;

public class CCTManualParameter extends BaseManualParameter
{
    final String TAG = CCTManualParameter.class.getSimpleName();
    final String WBCURRENT = "wb-current-cct";
    final String WB_CCT = "wb-cct";
    final String WB_CT = "wb-ct";
    final String WB_MANUAL = "wb-manual-cct";
    final String MANUAL_WB_VALUE = "manual-wb-value";
    final String MAX_WB_CCT = "max-wb-cct";
    final String MIN_WB_CCT = "min-wb-cct";
    final String MAX_WB_CT = "max-wb-ct";
    final String MIN_WB_CT = "min-wb-ct";
    final String LG_Min = "lg-wb-supported-min";
    final String LG_Max = "lg-wb-supported-max";
    final String LG_WB = "lg-wb";
    final String WB_MODE_MANUAL = "manual";
    final String WB_MODE_MANUAL_CCT = "manual-cct";


    private int min = -1;
    private int max = -1;
    private String manualWbMode;
    public CCTManualParameter(Camera.Parameters parameters, String value, String maxValue, String MinValue, CamParametersHandler camParametersHandler)
    {
        super(parameters, "", "", "", camParametersHandler,1);

        this.isSupported = false;
        if (DeviceUtils.IS(DeviceUtils.Devices.ZTE_ADV) ||DeviceUtils.IS_DEVICE_ONEOF(DeviceUtils.QC_Manual_New) || DeviceUtils.IS(DeviceUtils.Devices.LenovoK920))
        {
            this.min = 2000;
            this.max = 8000;
            this.value = WB_MANUAL;
            if(DeviceUtils.IS_DEVICE_ONEOF(DeviceUtils.QC_Manual_New))
                this.manualWbMode = WB_MODE_MANUAL;
            else
                this.manualWbMode = WB_MODE_MANUAL_CCT;
            this.isSupported = true;
            createStringArray(min,max,100);
        }
        else
        {
            //check first all possible values
            if (parameters.get(WBCURRENT) != null)
                this.value = WBCURRENT;
            else if (parameters.get(WB_CCT)!=null)
                this.value = WB_CCT;
            else if (parameters.get(WB_CT)!= null)
                this.value = WB_CT;
            else if (parameters.get(WB_MANUAL)!= null)
                this.value = WB_MANUAL;
            else if (parameters.get(MANUAL_WB_VALUE)!= null)
                this.value = MANUAL_WB_VALUE;
            else if (parameters.get(LG_WB)!= null)
                this.value = LG_WB;

            //check all possible max values
            if (parameters.get(LG_Max)!= null)
                setmax(LG_Max);

            else if (parameters.get(MAX_WB_CT)!= null)
                setmax(MAX_WB_CT);
            else if (parameters.get(MAX_WB_CCT)!= null) {
                setmax(MAX_WB_CCT);
            }

            //check all possible min values
            if (parameters.get(LG_Min)!= null)
                setmin(LG_Min);
             else if (parameters.get(MIN_WB_CT)!= null)
                setmin(MIN_WB_CT);
            else if (parameters.get(MIN_WB_CCT)!= null) {
                setmin(MIN_WB_CCT);
            }

            //check wbmode manual
            if (arrayContainsString(camParametersHandler.WhiteBalanceMode.GetValues(), WB_MODE_MANUAL))
                this.manualWbMode = WB_MODE_MANUAL;
            else if (arrayContainsString(camParametersHandler.WhiteBalanceMode.GetValues(), WB_MODE_MANUAL_CCT))
                this.manualWbMode = WB_MODE_MANUAL_CCT;

            if (min != -1 && max != -1 && !this.value.equals(""))
            {
                isSupported = true;
                createStringArray(min,max,100);
            }
        }
        Logger.d(TAG, "value:" + "" + " max value:" + "" + " min value:" + min_value);
    }

    private boolean arrayContainsString(String[] ar,String dif)
    {
        boolean ret = false;
        for (String s: ar)
            if (s.equals(dif))
                ret = true;
        return ret;
    }

    private void setmax(String m)
    {
        this.max_value = m;
        max = Integer.parseInt(parameters.get(max_value));
    }

    private void setmin(String m)
    {
        this.min_value = m;
        min = Integer.parseInt(parameters.get(min_value));
    }

    @Override
    protected String[] createStringArray(int min, int max, float step)
    {
        ArrayList<String> t = new ArrayList<>();
        t.add("Auto");
        for (int i = min; i<=max;i+=step)
        {
            t.add(i+"");
        }
        stringvalues = new String[t.size()];
        t.toArray(stringvalues);
        return stringvalues;
    }

    @Override
    public boolean IsSupported()
    {
        return this.isSupported;
    }

    @Override
    public boolean IsVisible() {
        return IsSupported();
    }

    @Override
    public int GetValue()
    {
        return currentInt;
    }

    @Override
    public String GetStringValue()
    {
        if (stringvalues != null)
            return stringvalues[currentInt];
        return null;
    }

    @Override
    protected void setvalue(int valueToSet) {
        currentInt = valueToSet;
        //set to auto
        if (currentInt == 0)
        {
            if (DeviceUtils.IS_DEVICE_ONEOF(DeviceUtils.HTC_m8_9))
            {
                parameters.set(value, "-1");
            } else if (DeviceUtils.IS(DeviceUtils.Devices.LG_G4))
                parameters.set(value, "0");
            else
                camParametersHandler.WhiteBalanceMode.SetValue("auto", true);
        }
        else //set manual wb mode and value
        {
            if (!camParametersHandler.WhiteBalanceMode.GetValue().equals(manualWbMode) && manualWbMode != "")
                camParametersHandler.WhiteBalanceMode.SetValue(manualWbMode, true);
            parameters.set(value, stringvalues[currentInt]);
            Logger.d(TAG,"set "+ value + " to " + stringvalues[currentInt]);

            if (DeviceUtils.IS_DEVICE_ONEOF(DeviceUtils.QC_Manual_New))
                try {
                    parameters.set("manual-wb-type", "color-temperature");
                    parameters.set("manual-wb-value", stringvalues[currentInt]);
                } catch (Exception ex) {
                    Logger.exception(ex);
                }
        }
        camParametersHandler.SetParametersToCamera(parameters);
    }


    @Override
    public String[] getStringValues() {
        return stringvalues;
    }
}


