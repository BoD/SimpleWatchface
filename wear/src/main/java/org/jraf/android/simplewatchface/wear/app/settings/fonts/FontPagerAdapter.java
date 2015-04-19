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
package org.jraf.android.simplewatchface.wear.app.settings.fonts;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.simplewatchface.R;

public class FontPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private final FontPickActivity.Mode mMode;
    private ArrayList<String> mFontNameList = new ArrayList<>();

    public FontPagerAdapter(Context context, FontPickActivity.Mode mode) {
        mContext = context;
        mMode = mode;
        try {
            mFontNameList.addAll(Arrays.asList(mContext.getAssets().list("fonts")));
        } catch (Exception e) {
            // Should never happen
            throw new AssertionError(e);
        }
    }

    @Override
    public int getCount() {
        return mFontNameList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View res = LayoutInflater.from(mContext).inflate(R.layout.preset_pick_page_rect, container, false);
//        View conBackground = res.findViewById(R.id.conBackground);
//        TextView txtHourMinutes = (TextView) res.findViewById(R.id.txtHourMinutes);
//        TextView txtSeconds = (TextView) res.findViewById(R.id.txtSeconds);
//        TextView txtAmPm = (TextView) res.findViewById(R.id.txtAmPm);
//        TextView txtDate = (TextView) res.findViewById(R.id.txtDate);
//
//        boolean is24HourFormat = DateFormat.is24HourFormat(mContext);
//        if (is24HourFormat) txtAmPm.setVisibility(View.INVISIBLE);
//
//        SettingsHelper settingsHelper = SettingsHelper.get(mContext);
//
//        // Typefaces
//        Typeface timeTypeface;
//        Typeface dateTypeface;
//        if (mMode == FontPickActivity.Mode.TIME) {
//            // Preview the time font
//            timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mFontNameList.get(position));
//            dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + settingsHelper.getFontDate());
//        } else {
//            // Preview the date font
//            timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + settingsHelper.getFontTime());
//            dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mFontNameList.get(position));
//        }
//
//        txtHourMinutes.setTypeface(timeTypeface);
//        txtSeconds.setTypeface(timeTypeface);
//        txtAmPm.setTypeface(timeTypeface);
//        txtDate.setTypeface(dateTypeface);
//
//        // Colors
//        // Background
//        Bitmap backgroundPicture = settingsHelper.getBackgroundPicture();
//        if (backgroundPicture != null) {
//            conBackground.setBackground(new BitmapDrawable(mContext.getResources(), backgroundPicture));
//        } else {
//            conBackground.setBackgroundColor(settingsHelper.getColorBackground());
//        }
//        txtHourMinutes.setTextColor(settingsHelper.getColorHourMinutes());
//        txtSeconds.setTextColor(settingsHelper.getColorSeconds());
//        txtAmPm.setTextColor(settingsHelper.getColorAmPm());
//        txtDate.setTextColor(settingsHelper.getColorDate());
//
//        container.addView(res);

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

    public String getFontName(int index) {
        return mFontNameList.get(index);
    }
}
