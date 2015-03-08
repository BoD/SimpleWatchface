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
package org.jraf.android.simplewatchface.wear.widget;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jraf.android.simplewatchface.R;

public class WearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {
    private ImageView mImgColorIndicator;
    private TextView mTxtLabel;

    public WearableListItemLayout(Context context) {
        super(context);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        mImgColorIndicator = (ImageView) findViewById(R.id.imgColorIndicator);
        mTxtLabel = (TextView) findViewById(R.id.txtLabel);
        super.onFinishInflate();
    }

    public void setLabel(CharSequence text) {
        mTxtLabel.setText(text);
    }

    public void setColorIndicator(int color) {
        GradientDrawable drawable = (GradientDrawable) mImgColorIndicator.getBackground().mutate();
        drawable.setColor(color);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            animate().scaleX(1f).scaleY(1f);
        } else {
            setScaleX(1f);
            setScaleY(1f);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            animate().scaleX(.8f).scaleY(.8f);
        } else {
            setScaleX(.8f);
            setScaleY(.8f);
        }
    }
}
