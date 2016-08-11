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

package freed.cam.apis.camera1.modules;

import android.hardware.Camera;

import java.io.File;

import freed.cam.apis.KEYS;
import freed.cam.apis.basecamera.CameraWrapperInterface;
import freed.cam.apis.basecamera.modules.ModuleAbstract;
import freed.cam.apis.basecamera.modules.ModuleHandlerAbstract;
import freed.cam.apis.camera1.CameraHolder;
import freed.utils.StringUtils;

/**
 * Created by troop on 07.07.2016.
 */
public class AfBracketModule extends ModuleAbstract implements Camera.PictureCallback
{
    private int currentFocusPosition = 0;
    private int step = 0;
    private final int IMAGES_TO_TAKE = 10;
    private int picsTaken;
    private CameraHolder cameraHolder;
    public AfBracketModule(CameraWrapperInterface cameraUiWrapper) {
        super(cameraUiWrapper);
        cameraHolder = (CameraHolder) cameraUiWrapper.GetCameraHolder();
        name = KEYS.MODULE_AFBRACKET;
    }

    /**
     * this gets called when the module gets loaded. set here specific paramerters that are needed by the module
     */
    @Override
    public void InitModule() {

    }

    /**
     * this gets called when module gets unloaded reset the parameters that where set on InitModule
     */
    @Override
    public void DestroyModule() {

    }

    @Override
    public String LongName() {
        return "AfBracket";
    }

    @Override
    public String ShortName() {
        return "AfB";
    }


    @Override
    public boolean DoWork()
    {
        //no work in progress, lets start new
        if (!isWorking)
        {
            //get the maximal focus postition
            int max = cameraUiWrapper.GetParameterHandler().ManualFocus.getStringValues().length;
            //get the step to incrase the focuspostions after each image
            step = max / IMAGES_TO_TAKE;
            //start with lowest focuspos
            currentFocusPosition = 1;
            //set mf to first position, 0 is automode
            cameraUiWrapper.GetParameterHandler().ManualFocus.SetValue(currentFocusPosition);
            //start capture
            picsTaken = 0;
            takePicture();
        }
        return true;
    }

    private void takePicture()
    {
        cameraHolder.TakePicture(this);
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_start);
    }

    /**
     * Called when image data is available after a picture is taken.
     * The format of the data depends on the context of the callback
     * and {@link Camera.Parameters} settings.
     *
     * @param data   a byte array of the picture data
     * @param camera the Camera service object
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
        changeCaptureState(ModuleHandlerAbstract.CaptureStates.image_capture_stop);
        cameraHolder.StartPreview();
        picsTaken++;
        File file = new File(cameraUiWrapper.getActivityInterface().getStorageHandler().getNewFilePath(appSettingsManager.GetWriteExternal(), "Af" + picsTaken+ ".jpg"));
        saveBytesToFile(data,file);
        scanAndFinishFile(file);
        if (picsTaken < IMAGES_TO_TAKE)
        {
            currentFocusPosition +=step;
            cameraUiWrapper.GetParameterHandler().ManualFocus.SetValue(currentFocusPosition);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            takePicture();
        }

    }
}
