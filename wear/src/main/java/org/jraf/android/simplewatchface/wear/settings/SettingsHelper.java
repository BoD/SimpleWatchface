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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.util.bitmap.BitmapUtil;
import org.jraf.android.util.listeners.Listeners;
import org.jraf.android.util.log.wrapper.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SettingsHelper {
    private static final String PREF_COLOR_BACKGROUND = "PREF_COLOR_BACKGROUND";
    private static final String PREF_COLOR_HOUR_MINUTES = "PREF_COLOR_HOUR_MINUTES";
    private static final String PREF_COLOR_SECONDS = "PREF_COLOR_SECONDS";
    private static final String PREF_COLOR_AM_PM = "PREF_COLOR_AM_PM";
    private static final String PREF_COLOR_DATE = "PREF_COLOR_DATE";

    private static final SettingsHelper INSTANCE = new SettingsHelper();
    private static final String FILE_BACKGROUND_PICTURE = "background.jpg";

    private boolean mIsInit;
    private Context mContext;
    private SharedPreferences mSharedPreference;

    private int mDefaultColorBackground;
    private int mDefaultColorHourMinutes;
    private int mDefaultColorSeconds;
    private int mDefaultColorAmPm;
    private int mDefaultColorDate;

    private Bitmap mBackgroundPicture;

    private SettingsHelper() {}

    public static SettingsHelper get(Context context) {
        if (!INSTANCE.mIsInit) {
            INSTANCE.init(context);
        }
        return INSTANCE;
    }

    private void init(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        initDefaults(context);
        File backgroundBitmapFile = context.getFileStreamPath(FILE_BACKGROUND_PICTURE);
        if (backgroundBitmapFile.exists()) {
            mBackgroundPicture = BitmapUtil.tryDecodeFile(backgroundBitmapFile, null);
            if (mBackgroundPicture == null) Log.w("Could not open background bitmap");
        }
        mIsInit = true;
    }

    private void initDefaults(Context context) {
        Resources resources = context.getResources();
        mDefaultColorBackground = resources.getColor(R.color.default_background);
        mDefaultColorHourMinutes = resources.getColor(R.color.default_hourMinutes);
        mDefaultColorSeconds = resources.getColor(R.color.default_seconds);
        mDefaultColorAmPm = resources.getColor(R.color.default_amPm);
        mDefaultColorDate = resources.getColor(R.color.default_date);
    }

    public int getColorBackground() {
        return mSharedPreference.getInt(PREF_COLOR_BACKGROUND, mDefaultColorBackground);
    }

    public void setColorBackground(int color) {
        mSharedPreference.edit().putInt(PREF_COLOR_BACKGROUND, color).apply();
    }

    public int getColorHourMinutes() {
        return mSharedPreference.getInt(PREF_COLOR_HOUR_MINUTES, mDefaultColorHourMinutes);
    }

    public void setColorHourMinutes(int color) {
        mSharedPreference.edit().putInt(PREF_COLOR_HOUR_MINUTES, color).apply();
    }

    public int getColorSeconds() {
        return mSharedPreference.getInt(PREF_COLOR_SECONDS, mDefaultColorSeconds);
    }

    public void setColorSeconds(int color) {
        mSharedPreference.edit().putInt(PREF_COLOR_SECONDS, color).apply();
    }

    public int getColorAmPm() {
        return mSharedPreference.getInt(PREF_COLOR_AM_PM, mDefaultColorAmPm);
    }

    public void setColorAmPm(int color) {
        mSharedPreference.edit().putInt(PREF_COLOR_AM_PM, color).apply();
    }

    public int getColorDate() {
        return mSharedPreference.getInt(PREF_COLOR_DATE, mDefaultColorDate);
    }

    public void setColorDate(int color) {
        mSharedPreference.edit().putInt(PREF_COLOR_DATE, color).apply();
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
            mSharedPreference.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        }

        @Override
        protected void onNoMoreListeners() {
            mSharedPreference.unregisterOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
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
