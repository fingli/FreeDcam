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

package freed.viewer.stack;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Type;
import android.support.v4.provider.DocumentFile;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.troop.freedcam.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import freed.ActivityAbstract;
import freed.cam.apis.camera1.parameters.modes.StackModeParameter;
import freed.cam.ui.handler.MediaScannerManager;
import freed.utils.Logger;
import freed.utils.RenderScriptHandler;
import freed.utils.ScriptField_MinMaxPixel;
import freed.utils.StringUtils;
import freed.viewer.dngconvert.DngConvertingFragment;

import static freed.cam.apis.camera1.parameters.modes.StackModeParameter.*;

/**
 * Created by troop on 06.07.2016.
 */
public class StackActivity extends ActivityAbstract
{
    private final String TAG = StackActivity.class.getSimpleName();
    private String[] filesToStack = null;
    private RenderScriptHandler renderScriptHandler;
    private int stackMode = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stack_activity);
        Spinner stackvaluesButton = (Spinner)findViewById(R.id.freedviewer_stack_stackvalues_button);
        String[] items =  new String[] {AVARAGE, AVARAGE1x2, AVARAGE1x3, AVARAGE3x3, LIGHTEN, LIGHTEN_V, MEDIAN, "Focus"};
        ArrayAdapter<String> stackadapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, items);
        stackvaluesButton.setAdapter(stackadapter);
        filesToStack = getIntent().getStringArrayExtra(DngConvertingFragment.EXTRA_FILESTOCONVERT);
        renderScriptHandler = new RenderScriptHandler(getContext());

        stackvaluesButton.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                stackMode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button buttonStartStack = (Button)findViewById(R.id.button_stackPics);
        buttonStartStack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processStack();
            }
        });
    }

    private void processStack()
    {
        Logger.d(TAG, "Process Stack: Mode:"+ stackMode);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filesToStack[0],options);
        int mWidth = options.outWidth;
        int mHeight = options.outHeight;
        Type.Builder tbIn2 = new Type.Builder(renderScriptHandler.GetRS(), Element.RGBA_8888(renderScriptHandler.GetRS()));
        tbIn2.setX(mWidth);
        tbIn2.setY(mHeight);
        renderScriptHandler.SetAllocsTypeBuilder(tbIn2,tbIn2, Allocation.USAGE_SCRIPT,Allocation.USAGE_SCRIPT);

        if (stackMode < 7)
        {
            renderScriptHandler.imagestack.set_Width(mWidth);
            renderScriptHandler.imagestack.set_Height(mHeight);
            renderScriptHandler.imagestack.set_yuvinput(false);
            renderScriptHandler.imagestack.set_gCurrentFrame(renderScriptHandler.GetIn());
            renderScriptHandler.imagestack.set_gLastFrame(renderScriptHandler.GetOut());
            if (stackMode == 6) {
                ScriptField_MinMaxPixel medianMinMax = new ScriptField_MinMaxPixel(renderScriptHandler.GetRS(), mWidth * mHeight);
                renderScriptHandler.imagestack.bind_medianMinMaxPixel(medianMinMax);
            }
        }
        else
        {
            renderScriptHandler.focus_stack.set_gCurrentFrame(renderScriptHandler.GetIn());
            renderScriptHandler.focus_stack.set_gLastFrame(renderScriptHandler.GetOut());
        }
        for (int i = 0; i< filesToStack.length;i++)
        {
            String f = filesToStack[i];
            Logger.d(TAG, "Stack File:" + f);
            if (i == 0)
            {
                renderScriptHandler.GetOut().copyFrom(BitmapFactory.decodeFile(f));
            }
            else {
                renderScriptHandler.GetIn().copyFrom(BitmapFactory.decodeFile(f));
                switch (stackMode) {
                    case 0: //AVARAGE
                        renderScriptHandler.imagestack.forEach_stackimage_avarage(renderScriptHandler.GetOut());
                        break;
                    case 1: //AVARAGE1x2
                        renderScriptHandler.imagestack.forEach_stackimage_avarage1x2(renderScriptHandler.GetOut());
                        break;
                    case 2: //AVARAGE1x3
                        renderScriptHandler.imagestack.forEach_stackimage_avarage1x3(renderScriptHandler.GetOut());
                        break;
                    case 3: // AVARAGE3x3
                        renderScriptHandler.imagestack.forEach_stackimage_avarage3x3(renderScriptHandler.GetOut());
                        break;
                    case 4: // LIGHTEN
                        renderScriptHandler.imagestack.forEach_stackimage_lighten(renderScriptHandler.GetOut());
                        break;
                    case 5: // LIGHTEN_V
                        renderScriptHandler.imagestack.forEach_stackimage_lightenV(renderScriptHandler.GetOut());
                        break;
                    case 6: //MEDIAN
                        renderScriptHandler.imagestack.forEach_stackimage_median(renderScriptHandler.GetOut());
                        break;
                    case 7: //focus
                        renderScriptHandler.focus_stack.forEach_stack(renderScriptHandler.GetOut());
                        break;
                }
            }
        }
        Bitmap outputBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        renderScriptHandler.GetOut().copyTo(outputBitmap);
        File file = new File(filesToStack[0]);
        String parent = file.getParent();
        saveBitmapToFile(outputBitmap,new File(parent+"/" + StringUtils.getStringDatePAttern().format(new Date()) + "_Stack.jpg"));
        Logger.d(TAG,"Stack Done!");
    }

    private void saveBitmapToFile(Bitmap bitmap, File file)
    {
        OutputStream outStream = null;
        String intsd = StringUtils.GetInternalSDCARD();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP || file.getAbsolutePath().contains(intsd) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            try {
                outStream= new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            DocumentFile df =  getFreeDcamDocumentFolder();

            DocumentFile wr = df.createFile("image/*", file.getName());
            try {
                outStream = getContentResolver().openOutputStream(wr.getUri());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        MediaScannerManager.ScanMedia(getContext(), file);
    }
}
