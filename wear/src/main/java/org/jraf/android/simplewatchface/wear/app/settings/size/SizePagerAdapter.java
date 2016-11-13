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
package org.jraf.android.simplewatchface.wear.app.settings.size;

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
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.simplewatchface.wear.widget.WatchFaceView;

public class SizePagerAdapter extends PagerAdapter {
    private static final int MIN_SIZE = 16;
    private static final int MAX_SIZE = 54;
    private final Context mContext;
    private final SettingsHelper mSettingsHelper;
    private final SizePickActivity.Mode mMode;
    private ArrayList<Integer> mSizeList = new ArrayList<>();
    private boolean mIsRound;

    public SizePagerAdapter(Context context, SizePickActivity.Mode mode) {
        mContext = context;
        mMode = mode;
        mSettingsHelper = SettingsHelper.get(context);
        for (int size = MIN_SIZE; size <= MAX_SIZE; size += 2) {
            mSizeList.add(size);
        }
    }

    @Override
    public int getCount() {
        return mSizeList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View res = LayoutInflater.from(mContext).inflate(R.layout.settings_pick_page, container, false);

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
        Typeface timeTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontTime());
        watchFaceView.setTimeTypeface(timeTypeface);
        Typeface dateTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/" + mSettingsHelper.getFontDate());
        watchFaceView.setDateTypeface(dateTypeface);

        // Sizes
        if (mMode == SizePickActivity.Mode.TIME) {
            watchFaceView.setTimeSize(mSizeList.get(position));
        } else {
            watchFaceView.setDateSize(mSizeList.get(position));
        }

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

    public int getSize(int index) {
        return mSizeList.get(index);
    }

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }
}
