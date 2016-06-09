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

package com.freedcam.apis.camera1.parameters.manual;


import android.hardware.Camera.Parameters;

import com.freedcam.apis.KEYS;
import com.freedcam.apis.basecamera.interfaces.I_CameraHolder;
import com.freedcam.apis.camera1.parameters.ParametersHandler;
import com.freedcam.utils.DeviceUtils.Devices;

/**
 * Created by troop on 17.08.2014.
 */
public class FocusManualParameterLG extends  BaseManualParameter
{
    private I_CameraHolder baseCameraHolder;
    private static String TAG =FocusManualParameterLG.class.getSimpleName();

    public FocusManualParameterLG(Parameters parameters, I_CameraHolder cameraHolder, ParametersHandler parametersHandler) {
        super(parameters, "", "", "", parametersHandler,1);
        baseCameraHolder = cameraHolder;
        isSupported = true;
        isVisible = isSupported;
        if (isSupported)
        {
            int max = 0;
            step = 1;
            if (parametersHandler.appSettingsManager.getDevice() == Devices.LG_G4)
                max = 60;
            else
                max = 79;
            stringvalues = createStringArray(0,max,step);
        }

    }


    @Override
    public void SetValue(int valueToSet)
    {
        currentInt = valueToSet;
        if(valueToSet != 0)
        {
            if (!parametersHandler.FocusMode.GetValue().equals(KEYS.FOCUS_MODE_NORMAL)) {
                parametersHandler.FocusMode.SetValue(KEYS.FOCUS_MODE_NORMAL, true);
            }
            parameters.set(KEYS.MANUALFOCUS_STEP, stringvalues[valueToSet]);
            parametersHandler.SetParametersToCamera(parameters);
        }
        else if (valueToSet == 0)
        {
            parametersHandler.FocusMode.SetValue(KEYS.AUTO, true);
        }


    }

    @Override
    public String GetStringValue()
    {
        if (parametersHandler.FocusMode.GetValue().equals(KEYS.AUTO))
            return KEYS.AUTO;
        else
            return GetValue()+"";
    }
}