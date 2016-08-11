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

package freed.cam.apis.basecamera.parameters.modes;

import android.os.Build.VERSION;

import freed.ActivityInterface;
import freed.utils.AppSettingsManager;

/**
 * Created by troop on 21.07.2015.
 */
public class ApiParameter extends AbstractModeParameter
{
    private final ActivityInterface fragment_activityInterface;
    private final boolean DEBUG = false;

    public ApiParameter(ActivityInterface fragment_activityInterface) {
        this.fragment_activityInterface = fragment_activityInterface;
    }

    @Override
    public String[] GetValues()
    {
        if (VERSION.SDK_INT >= 21)
        {
            if (fragment_activityInterface.getAppSettings().IsCamera2FullSupported().equals("true"))
                return new String[]{AppSettingsManager.API_SONY, AppSettingsManager.API_2, AppSettingsManager.API_1};
            else
                return new String[]{AppSettingsManager.API_SONY, AppSettingsManager.API_1};
        } else
            return new String[]{AppSettingsManager.API_SONY, AppSettingsManager.API_1};
    }

    @Override
    public String GetValue() {
        String ret = fragment_activityInterface.getAppSettings().getCamApi();
        if (ret.equals(""))
            ret = AppSettingsManager.API_1;
        return ret;
    }

    @Override
    public void SetValue(String valueToSet, boolean setToCamera) {
        fragment_activityInterface.getAppSettings().setCamApi(valueToSet);
        fragment_activityInterface.SwitchCameraAPI(valueToSet);
    }

    @Override
    public boolean IsSupported() {
        return true;
    }
}
