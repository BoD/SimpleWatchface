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
package org.jraf.android.simplewatchface.wear.app.settings.presets;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.presets.ColorPreset;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.simplewatchface.wear.widget.WatchFaceView;

public class PresetPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private final SettingsHelper mSettingsHelper;
    private ArrayList<ColorPreset> mColorPresetList = new ArrayList<>();
    private boolean mIsRound;

    public PresetPagerAdapter(Context context) {
        mContext = context;
        mSettingsHelper = SettingsHelper.get(context);
        try {
            Bitmap backgroundPicture = mSettingsHelper.getBackgroundPicture();
            if (backgroundPicture != null) {
                mColorPresetList.add(ColorPreset.fromImage(backgroundPicture));
            }
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_arctic_blue));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_1));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_2));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_3));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_4));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_5));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_6));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_guillaume_7));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_adobe_1));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_adobe_2));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_adobe_3));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_adobe_4));

        } catch (Exception e) {
            // Should never happen
            throw new AssertionError(e);
        }
    }

    @Override
    public int getCount() {
        return mColorPresetList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ColorPreset colorPreset = mColorPresetList.get(position);

        View res = LayoutInflater.from(mContext).inflate(R.layout.preset_pick_page_rect, container, false);

        // Background
        ImageView conBackground = (ImageView) res.findViewById(R.id.imgBackground);
        Bitmap backgroundPicture = mSettingsHelper.getBackgroundPicture();
        if (backgroundPicture != null) {
            conBackground.setImageBitmap(backgroundPicture);
        } else {
            conBackground.setBackgroundColor(colorPreset.background);
        }

        WatchFaceView watchFaceView = (WatchFaceView) res.findViewById(R.id.vieWatchFace);
        watchFaceView.setIsRound(mIsRound);

        // Typefaces
        Typeface timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontTime());
        Typeface dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontDate());
        watchFaceView.setTimeTypeface(timeTypeface);
        watchFaceView.setDateTypeface(dateTypeface);

        watchFaceView.setHourMinutesColor(0xFFFF0000);
        watchFaceView.setSecondsColor(0xFFFF0000);
        watchFaceView.setAmPmColor(0xFFFF0000);
        watchFaceView.setDateColor(0xFFFF0000);

        // Colors
        watchFaceView.setHourMinutesColor(colorPreset.hourMinutes);
        watchFaceView.setSecondsColor(colorPreset.seconds);
        watchFaceView.setAmPmColor(colorPreset.amPm);
        watchFaceView.setDateColor(colorPreset.date);

        container.addView(res);

        return res;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public ColorPreset getColorPreset(int index) {
        return mColorPresetList.get(index);
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }

}
