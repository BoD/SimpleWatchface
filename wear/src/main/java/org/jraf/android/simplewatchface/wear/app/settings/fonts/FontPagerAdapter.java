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
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.simplewatchface.wear.widget.WatchFaceView;

public class FontPagerAdapter extends PagerAdapter {
    private final Context mContext;
    private final SettingsHelper mSettingsHelper;
    private final FontPickActivity.Mode mMode;
    private ArrayList<String> mFontNameList = new ArrayList<>();
    private boolean mIsRound;

    public FontPagerAdapter(Context context, FontPickActivity.Mode mode) {
        mContext = context;
        mMode = mode;
        mSettingsHelper = SettingsHelper.get(context);
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
        View res = LayoutInflater.from(mContext).inflate(R.layout.preset_pick_page, container, false);

        // Background
        ImageView conBackground = (ImageView) res.findViewById(R.id.imgBackground);
        Bitmap backgroundPicture = mSettingsHelper.getBackgroundPicture();
        if (backgroundPicture != null) {
            conBackground.setImageBitmap(backgroundPicture);
        } else {
            conBackground.setBackgroundColor(mSettingsHelper.getColorBackground());
        }

        WatchFaceView watchFaceView = (WatchFaceView) res.findViewById(R.id.vieWatchFace);
        watchFaceView.setIsRound(mIsRound);

        // Typefaces
        Typeface timeTypeface;
        Typeface dateTypeface;
        if (mMode == FontPickActivity.Mode.TIME) {
            // Preview the time font
            timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mFontNameList.get(position));
            dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontDate());
        } else {
            // Preview the date font
            timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontTime());
            dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mFontNameList.get(position));
        }
        watchFaceView.setTimeTypeface(timeTypeface);
        watchFaceView.setDateTypeface(dateTypeface);

        // Colors
        watchFaceView.setHourMinutesColor(mSettingsHelper.getColorHourMinutes());
        watchFaceView.setSecondsColor(mSettingsHelper.getColorSeconds());
        watchFaceView.setAmPmColor(mSettingsHelper.getColorAmPm());
        watchFaceView.setDateColor(mSettingsHelper.getColorDate());

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

    public String getFontName(int index) {
        return mFontNameList.get(index);
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }
}
