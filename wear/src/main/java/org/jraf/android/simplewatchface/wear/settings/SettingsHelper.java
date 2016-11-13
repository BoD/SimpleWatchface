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
package org.jraf.android.simplewatchface.wear.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import org.jraf.android.util.bitmap.BitmapUtil;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.log.Log;

public class SettingsHelper extends SettingsPrefs {
    private static SettingsHelper sInstance;
    private static final String FILE_BACKGROUND_PICTURE = "background.jpg";

    private Context mContext;
    private Bitmap mBackgroundPicture;

    private SettingsHelper(SharedPreferences wrapped) {
        super(wrapped);
    }

    public static SettingsHelper get(Context context) {
        if (sInstance == null) {
            sInstance = new SettingsHelper(getWrapped(context));
            sInstance.init(context);
        }
        return sInstance;
    }

    private void init(Context context) {
        mContext = context.getApplicationContext();
        File backgroundBitmapFile = context.getFileStreamPath(FILE_BACKGROUND_PICTURE);
        if (backgroundBitmapFile.exists()) {
            mBackgroundPicture = BitmapUtil.tryDecodeFile(backgroundBitmapFile, null);
            if (mBackgroundPicture == null) Log.w("Could not open background bitmap");
        }
    }

    public Bitmap getBackgroundPicture() {
        return mBackgroundPicture;
    }

    public void setBackgroundPicture(@Nullable byte[] bitmapData) {
        if (bitmapData == null) {
            mBackgroundPicture = null;
            mContext.deleteFile(FILE_BACKGROUND_PICTURE);
        } else {
            mBackgroundPicture = BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length);
            try (FileOutputStream fileOutputStream = mContext.openFileOutput(FILE_BACKGROUND_PICTURE, Context.MODE_PRIVATE)) {
                fileOutputStream.write(bitmapData, 0, bitmapData.length);
                fileOutputStream.flush();
            } catch (IOException e) {
                Log.e("Could not save the bitmap to a file", e);
            }
        }
        dispatchToListeners();
    }


    /*
     * Listeners.
     */
    // region

    public static interface SettingsChangeListener {
        void onSettingsChanged();
    }

    private Listeners<SettingsChangeListener> mListeners = new Listeners<SettingsChangeListener>() {
        @Override
        protected void onFirstListener() {
            registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }

        @Override
        protected void onNoMoreListeners() {
            unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }
    };

    private SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            dispatchToListeners();
        }
    };

    private void dispatchToListeners() {
        mListeners.dispatch(new Listeners.Dispatcher<SettingsChangeListener>() {
            @Override
            public void dispatch(SettingsChangeListener listener) {
                listener.onSettingsChanged();
            }
        });
    }

    public void addSettingsChangeListener(SettingsChangeListener settingsChangeListener) {
        mListeners.add(settingsChangeListener);
    }

    public void removeSettingsChangeListener(SettingsChangeListener settingsChangeListener) {
        mListeners.remove(settingsChangeListener);
    }

    // endregion
}
