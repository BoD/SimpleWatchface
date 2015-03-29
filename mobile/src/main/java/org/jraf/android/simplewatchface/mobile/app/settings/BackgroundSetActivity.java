/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2015 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jraf.android.simplewatchface.mobile.app.settings;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.common.wear.WearHelper;
import org.jraf.android.util.bitmap.BitmapUtil;
import org.jraf.android.util.file.FileUtil;

import java.io.File;

public class BackgroundSetActivity extends Activity {
    private Uri mOutputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri imageUri = (Uri) getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri == null) {
            // Should never happen, but avoid crashing
            finish();
            return;
        }

        if (savedInstanceState != null) {
            mOutputUri = (Uri) savedInstanceState.getSerializable("mOutputUri");
        }
        mOutputUri = getTempFile();
        new Crop(imageUri).output(mOutputUri).asSquare().start(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("mOutputUri", mOutputUri);
        super.onSaveInstanceState(outState);
    }

    private Uri getTempFile() {
        File tempFile = FileUtil.newTemporaryFile(this, ".jpg");
        return Uri.fromFile(tempFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == RESULT_CANCELED) {
                finish();
                return;
            }

            File imageFile = new File(mOutputUri.getPath());
            final Bitmap bitmap = BitmapUtil.tryDecodeFile(imageFile, null);
            if (bitmap == null) {
                Toast.makeText(this, R.string.background_set_fail_cannotDecodeBitmap, Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Send the image to the watch, on a background thread because it involves blocking (network) calls
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    WearHelper wearHelper = WearHelper.get();
                    wearHelper.connect(BackgroundSetActivity.this);
                    wearHelper.putSettings(bitmap);
                    wearHelper.disconnect();
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Toast.makeText(BackgroundSetActivity.this, R.string.background_set_success, Toast.LENGTH_LONG).show();
                    finish();
                }
            }.execute();
        }
    }
}
