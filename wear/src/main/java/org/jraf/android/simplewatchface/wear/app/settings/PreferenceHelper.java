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
package org.jraf.android.simplewatchface.wear.app.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import org.jraf.android.simplewatchface.R;

public class PreferenceHelper {
    private static final String PREF_COLOR_BACKGROUND = "PREF_COLOR_BACKGROUND";
    private static final String PREF_COLOR_HOUR_MINUTES = "PREF_COLOR_HOUR_MINUTES";
    private static final String PREF_COLOR_SECONDS = "PREF_COLOR_SECONDS";
    private static final String PREF_COLOR_AM_PM = "PREF_COLOR_AM_PM";
    private static final String PREF_COLOR_DATE = "PREF_COLOR_DATE";

    private static final PreferenceHelper INSTANCE = new PreferenceHelper();

    private SharedPreferences mSharedPreference;
    private int mDefaultColorBackground;
    private int mDefaultColorHourMinutes;
    private int mDefaultColorSeconds;
    private int mDefaultColorAmPm;
    private int mDefaultColorDate;

    public static PreferenceHelper get(Context context) {
        if (INSTANCE.mSharedPreference == null) {
            INSTANCE.mSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
            INSTANCE.initDefaults(context);
        }
        return INSTANCE;
    }

    private PreferenceHelper() {}

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


    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreference.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        mSharedPreference.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
