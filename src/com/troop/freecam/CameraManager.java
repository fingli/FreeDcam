package com.troop.freecam;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by troop on 25.08.13.
 */
public class CameraManager implements SurfaceHolder.Callback
{
    public static final String SwitchCamera = "switchcam";
    public static final String SwitchCamera_MODE_3D = "3D";
    public static final String SwitchCamera_MODE_2D = "2D";
    public static final String SwitchCamera_MODE_Front = "Front";

    public static final String Preferences_Flash3D = "3d_flash";
    public static final String Preferences_Flash2D = "2d_flash";
    public static final String Preferences_Focus2D = "2d_focus";
    public static final String Preferences_Focus3D = "3d_focus";
    public static final String Preferences_FocusFront = "front_focus";
    public static final String Preferences_WhiteBalanceFront = "front_whitebalance";
    public static final String Preferences_WhiteBalance3D = "3d_whitebalance";
    public static final String Preferences_WhiteBalance2D = "2d_whitebalance";
    public static final String Preferences_SceneFront = "front_scene";
    public static final String Preferences_Scene3D = "3d_scene";
    public static final String Preferences_Scene2D = "2d_scene";
    public static final String Preferences_Color2D = "2d_color";
    public static final String Preferences_Color3D = "3d_color";
    public static final String Preferences_ColorFront = "front_color";
    public static final String Preferences_IsoFront = "front_iso";
    public static final String Preferences_Iso3D = "3d_iso";
    public static final String Preferences_Iso2D = "2d_iso";
    public static final String Preferences_Exposure2D = "2d_exposure";
    public static final String Preferences_Exposure3D = "3d_exposure";
    public static final String Preferences_ExposureFront = "front_exposure";





    CamPreview context;
    public Camera mCamera;
    //MediaScannerManager scanManager;
    CameraManager cameraManager;
    public  Camera.Parameters parameters;
    public  ZoomManager zoomManager;
    public boolean Running = false;
    MediaScannerManager scanManager;
    public AutoFocusManager autoFocusManager;
    public static final String KEY_CAMERA_INDEX = "camera-index";
    public static final String KEY_S3D_SUPPORTED_STR = "s3d-supported";
    public boolean picturetaking = false;
    public boolean touchtofocus = false;
    MainActivity activity;
    SharedPreferences preferences;
    public ManualExposureManager manualExposureManager;
    public String lastPicturePath;
    public ManualSharpnessManager manualSharpnessManager;
    public ManualContrastManager manualContrastManager;
    public ManualBrightnessManager manualBrightnessManager;

    public boolean takePicture = false;

    public CameraManager(CamPreview context, MainActivity activity)
    {
        this.context = context;
        scanManager = new MediaScannerManager(context.getContext());
        context.mHolder.addCallback(this);
        zoomManager = new ZoomManager(this);
        autoFocusManager = new AutoFocusManager(this);
        this.activity = activity;
        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        manualExposureManager = new ManualExposureManager(this);
        cameraManager = this;
        manualSharpnessManager = new ManualSharpnessManager(this);
        manualContrastManager = new ManualContrastManager(this);
        manualBrightnessManager = new ManualBrightnessManager(this);

    }

    public  void Start()
    {
        String tmp = preferences.getString(CameraManager.SwitchCamera, CameraManager.SwitchCamera_MODE_3D);
        if (tmp.equals(CameraManager.SwitchCamera_MODE_3D))
            mCamera = Camera.open(2);
        if(tmp.equals(CameraManager.SwitchCamera_MODE_2D))
            mCamera = Camera.open(0);
            //mCamera.setDisplayOrientation(90);
        if (tmp.equals(CameraManager.SwitchCamera_MODE_Front))
            mCamera = Camera.open(1);

        try {
            mCamera.setPreviewDisplay(context.getHolder());
            mCamera.setZoomChangeListener(zoomManager);
            zoomManager.ResetZoom();
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;

            // TODO: add more exception handling logic here
        }
    }

    //if startstop true cam preview will be stopped and restartet
    public  void Restart(boolean startstop)
    {
        if (startstop)
        {
            parameters = mCamera.getParameters();
            mCamera.stopPreview();
            String tmp = preferences.getString(SwitchCamera, SwitchCamera_MODE_3D);

            if (tmp.equals("3D"))
            {
                parameters.setFlashMode(preferences.getString(Preferences_Flash3D, "auto"));
                parameters.setFocusMode(preferences.getString(Preferences_Focus3D, "auto"));
                parameters.setWhiteBalance(preferences.getString(Preferences_WhiteBalance3D,"auto"));
                parameters.setSceneMode(preferences.getString(Preferences_Scene3D,"auto"));
                parameters.setColorEffect(preferences.getString(Preferences_Color3D,"none"));
                parameters.set("iso", preferences.getString(Preferences_Iso3D, "auto"));
                parameters.set("exposure", preferences.getString(Preferences_Exposure3D , "auto"));
            }

            if(tmp.equals("2D"))
            {
                parameters.setFlashMode(preferences.getString(Preferences_Flash2D, "auto"));
                parameters.setFocusMode(preferences.getString(Preferences_Focus2D, "auto"));
                parameters.setWhiteBalance(preferences.getString(Preferences_WhiteBalance2D,"auto"));
                parameters.setSceneMode(preferences.getString(Preferences_Scene2D,"auto"));
                parameters.setColorEffect(preferences.getString(Preferences_Color2D,"none"));
                parameters.set("iso", preferences.getString(Preferences_Iso2D, "auto"));
                parameters.set("exposure", preferences.getString(Preferences_Exposure2D , "auto"));
            }
            if (tmp.equals("Front"))
            {
                parameters.setFocusMode(preferences.getString(Preferences_FocusFront, "auto"));
                parameters.setWhiteBalance(preferences.getString(Preferences_WhiteBalanceFront,"auto"));
                parameters.setSceneMode(preferences.getString(Preferences_SceneFront,"auto"));
                parameters.setColorEffect(preferences.getString(Preferences_ColorFront,"none"));
                parameters.set("iso", preferences.getString(Preferences_IsoFront, "auto"));
                parameters.set("exposure", preferences.getString(Preferences_ExposureFront , "auto"));
            }
            parameters.set("jpeg-quality", 100);
        }

        List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
        int w = sizes.get(0).width;
        int h = sizes.get(0).height;
        parameters.setPictureSize(w,h);
        parameters.set("preview-format", "yuv420p");


        try
        {

            mCamera.setParameters(parameters);
            parameters = mCamera.getParameters();
        }
        catch (Exception ex)
        {
            Log.e("Parameters Set Fail: ", ex.getMessage());
            parameters = mCamera.getParameters();
        }
        activity.flashButton.setText(parameters.getFlashMode());
        activity.focusButton.setText(parameters.getFocusMode());
        activity.sceneButton.setText(parameters.getSceneMode());
        activity.whitebalanceButton.setText(parameters.getWhiteBalance());
        activity.colorButton.setText(parameters.getColorEffect());
        activity.isoButton.setText(parameters.get("iso"));
        activity.exposureButton.setText(parameters.get("exposure"));
        manualExposureManager.ExternalSet = true;
        activity.exposureSeekbar.setProgress(parameters.getExposureCompensation() - parameters.getMinExposureCompensation());
        activity.sharpnessTextView.setText("Sharpness: " + parameters.getInt("sharpness"));
        activity.exposureTextView.setText("Exposure: " + parameters.getExposureCompensation());
        activity.contrastTextView.setText("Contrast: " + parameters.get("contrast"));
        activity.brightnessTextView.setText("Brightness: " + parameters.get("brightness"));



        if (startstop)
        {
            int max = 60; //parameters.getMaxExposureCompensation() - parameters.getMinExposureCompensation();
            activity.exposureSeekbar.setMax(max);
            activity.sharpnessSeekBar.setMax(180);
            activity.sharpnessSeekBar.setProgress(parameters.getInt("sharpness"));
            activity.contrastSeekBar.setMax(180);
            activity.contrastSeekBar.setProgress(parameters.getInt("contrast"));
            activity.brightnessSeekBar.setMax(100);
            mCamera.startPreview();
        }
    }

    public  void Stop()
    {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void StartTakePicture()
    {
        if (parameters.getFocusMode().equals(Camera.Parameters.FOCUS_MODE_AUTO))
        {
            takePicture = true;
            mCamera.autoFocus(autoFocusManager);
        }
        else
        {
            TakePicture();
        }
    }

    public void TakePicture()
    {
        if (picturetaking == false)
        {
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            picturetaking = true;
        }
    }

    public  void SetTouchFocus(Rect rectangle)
    {
        if (touchtofocus == false)
        {
            touchtofocus = true;
        //Convert from View's width and height to +/- 1000
            final Rect targetFocusRect = new Rect(
                rectangle.left * 2000/context.getWidth() - 1000,
                rectangle.top * 2000/context.getHeight() - 1000,
                rectangle.right * 2000/context.getWidth() - 1000,
                rectangle.bottom * 2000/context.getHeight() - 1000);

            final List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(targetFocusRect, 600);
            focusList.add(focusArea);
            if (parameters.getMaxNumFocusAreas() > 0 && parameters.getMaxNumMeteringAreas() > 0)
            {
                //if (parameters.getFocusAreas() == null && parameters.getMeteringAreas() == null)
                //{

                    parameters.setFocusAreas(focusList);
                    parameters.setMeteringAreas(focusList);
                    //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    //mCamera.cancelAutoFocus();
                try
                {
                    mCamera.setParameters(parameters);
                    mCamera.autoFocus(autoFocusManager);
                }
                catch (Exception ex)
                {
                    String exe = ex.getMessage();
                }


                //}
            }
        }
        else
        {
            mCamera.cancelAutoFocus();
            touchtofocus = false;
        }
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {

            MediaPlayer mediaPlayer = MediaPlayer.create(activity.getApplicationContext(), R.raw.camerashutter);
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                public void onCompletion(MediaPlayer mp)
                {
                    mp.release();
                }
            });
            mediaPlayer.setVolume(1,1);
            mediaPlayer.start(); // no need to call prepare(); create() does that for you
            Log.d("FreeCam", "onShutter'd");
        }
    };

    /** Handles data for raw picture */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("FreeCam", "onPictureTaken - raw");
        }
    };

    /** Handles data for jpeg picture */
    public Camera.PictureCallback jpegCallback = new Camera.PictureCallback()
    {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            //Log.d("Camman", "Data clone startet;");
            //byte[] newdata = data.clone();
            //Log.d("Camman", "Data clone ended;");
            boolean is3d = false;
            if (preferences.getString("switchcam", "3D").equals("3D"))
            {
                is3d = true;
            }


            SavePictureTask task = new SavePictureTask(scanManager, is3d, cameraManager);
            task.execute(data);

            //activity.thumbButton.invalidate();

            picturetaking =false;
            mCamera.startPreview();
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w,
                               int h) {
        parameters = mCamera.getParameters();
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        // 0 : first camera, 1 : second camera, 2 : dual(3D) camera
        //parameters.set(KEY_CAMERA_INDEX, 2);
        //parameters.set(KEY_S3D_SUPPORTED_STR, "true");
        //parameters.setPictureSize(4096, 1536);


       Restart(true);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        Start();
        Running = true;

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Stop();
        Running = false;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}
