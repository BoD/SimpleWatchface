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

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.presets.ColorPreset;

import java.util.ArrayList;

public class PresetPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private ArrayList<ColorPreset> mColorPresetList = new ArrayList<ColorPreset>();

    public PresetPagerAdapter(Context context) {
        mContext = context;
        try {
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_arctic_blue));
            mColorPresetList.add(ColorPreset.fromXml(context, R.xml.preset_color_nounours));
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
        View res = LayoutInflater.from(mContext).inflate(R.layout.preset_pick_page, container, false);
        View conBackground = res.findViewById(R.id.conBackground);
        TextView txtHourMinutes = (TextView) res.findViewById(R.id.txtHourMinutes);
        TextView txtSeconds = (TextView) res.findViewById(R.id.txtSeconds);
        TextView txtAmPm = (TextView) res.findViewById(R.id.txtAmPm);
        TextView txtDate = (TextView) res.findViewById(R.id.txtDate);

        // Typefaces
        Typeface timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Exo2-ExtraBoldItalic.ttf");
        txtHourMinutes.setTypeface(timeTypeface);
        txtSeconds.setTypeface(timeTypeface);
        txtAmPm.setTypeface(timeTypeface);
        Typeface dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Exo2-Italic.ttf");
        txtDate.setTypeface(dateTypeface);

        // Colors
        ColorPreset colorPreset = mColorPresetList.get(position);
        conBackground.setBackgroundColor(colorPreset.background);
        txtHourMinutes.setTextColor(colorPreset.hourMinutes);
        txtSeconds.setTextColor(colorPreset.seconds);
        txtAmPm.setTextColor(colorPreset.amPm);
        txtDate.setTextColor(colorPreset.date);

        container.addView(res);

        return res;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public ColorPreset getColorPreset(int index) {
        return mColorPresetList.get(index);
    }
}
