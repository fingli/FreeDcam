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

package freed;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import java.util.List;

import freed.utils.AppSettingsManager;
import freed.utils.LocationHandler;
import freed.utils.StorageFileHandler;
import freed.viewer.helper.BitmapHelper;
import freed.viewer.holder.FileHolder;

/**
 * Created by troop on 22.03.2015.
 */
public interface ActivityInterface
{
    void SwitchCameraAPI(String Api);
    void closeActivity();
    void ChooseSDCard(I_OnActivityResultCallback callback);
    interface I_OnActivityResultCallback
    {
        void onActivityResultCallback(Uri uri);
    }



    BitmapHelper getBitmapHelper();

    Context getContext();

    AppSettingsManager getAppSettings();

    StorageFileHandler getStorageHandler();

    boolean DeleteFile(FileHolder file);
    void DeleteFiles(List<FileHolder> files);

    void AddFile(FileHolder file);

    void AddFileListner(ActivityAbstract.FileEvent event);

    List<FileHolder> getFiles();

    void LoadDCIMDirs();
    void LoadFreeDcamDCIMDirsFiles();

    void LoadFolder(FileHolder fileHolder, ActivityAbstract.FormatTypes types);

    DocumentFile getFreeDcamDocumentFolder();
    DocumentFile getExternalSdDocumentFile();

    void DisablePagerTouch(boolean disable);

    boolean hasExternalSDPermission();
    boolean hasCameraPermission();
    boolean hasLocationPermission();
    boolean hasWifiPermission();

    LocationHandler getLocationHandler();

    int getOrientation();
}


