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
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.presets.ColorPreset;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.simplewatchface.wear.widget.WatchFaceView;

public class PresetPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private ArrayList<ColorPreset> mColorPresetList = new ArrayList<>();

    public PresetPagerAdapter(Context context) {
        mContext = context;
        try {
            Bitmap backgroundPicture = SettingsHelper.get(context).getBackgroundPicture();
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
        View res = LayoutInflater.from(mContext).inflate(R.layout.preset_pick_page_rect, container, false);
        WatchFaceView watchFaceView = (WatchFaceView) res.findViewById(R.id.vieWatchFace);
        watchFaceView.setHourMinutesColor(0xFFFF0000);
        watchFaceView.setSecondsColor(0xFFFF0000);
        watchFaceView.setAmPmColor(0xFFFF0000);
        watchFaceView.setDateColor(0xFFFF0000);
//        View conBackground = res.findViewById(R.id.conBackground);
//        TextView txtHourMinutes = (TextView) res.findViewById(R.id.txtHourMinutes);
//        TextView txtSeconds = (TextView) res.findViewById(R.id.txtSeconds);
//        TextView txtAmPm = (TextView) res.findViewById(R.id.txtAmPm);
//        TextView txtDate = (TextView) res.findViewById(R.id.txtDate);
//
//        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);
//        if (is24HourFormat) txtAmPm.setVisibility(View.INVISIBLE);
//
//        // Typefaces
//        Typeface timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + SettingsHelper.get(mContext).getFontTime());
//        Typeface dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + SettingsHelper.get(mContext).getFontDate());
//        txtHourMinutes.setTypeface(timeTypeface);
//        txtSeconds.setTypeface(timeTypeface);
//        txtAmPm.setTypeface(timeTypeface);
//        txtDate.setTypeface(dateTypeface);
//
//        // Colors
//        ColorPreset colorPreset = mColorPresetList.get(position);
//        // Background
//        Bitmap backgroundPicture = SettingsHelper.get(mContext).getBackgroundPicture();
//        if (backgroundPicture != null) {
//            conBackground.setBackground(new BitmapDrawable(mContext.getResources(), backgroundPicture));
//        } else {
//            conBackground.setBackgroundColor(colorPreset.background);
//        }
//        txtHourMinutes.setTextColor(colorPreset.hourMinutes);
//        txtSeconds.setTextColor(colorPreset.seconds);
//        txtAmPm.setTextColor(colorPreset.amPm);
//        txtDate.setTextColor(colorPreset.date);
//
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
}
