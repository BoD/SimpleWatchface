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

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jraf.android.simplewatchface.R;

public class WearableListItemLayout extends LinearLayout implements WearableListView.OnCenterProximityListener {
    private static final long ANIMATION_DURATION_MS = 150;
    private static final float SCALE_SHRINK = .75f;
    private static final float ALPHA_SHRINK = .5f;

    private ImageView mImgColorIndicator;
    private TextView mTxtLabel;

    private ObjectAnimator mExpandColorIndicatorScaleXAnimator;
    private ObjectAnimator mExpandColorIndicatorScaleYAnimator;
    private ObjectAnimator mExpandColorIndicatorAlphaAnimator;
    private AnimatorSet mExpandAnimator;

    private ObjectAnimator mShrinkColorIndicatorScaleXAnimator;
    private ObjectAnimator mShrinkColorIndicatorScaleYAnimator;
    private ObjectAnimator mShrinkColorIndicatorAlphaAnimator;
    private AnimatorSet mShrinkAnimator;

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

        mExpandColorIndicatorScaleXAnimator = ObjectAnimator.ofFloat(mImgColorIndicator, "scaleX", SCALE_SHRINK, 1f);
        mExpandColorIndicatorScaleYAnimator = ObjectAnimator.ofFloat(mImgColorIndicator, "scaleY", SCALE_SHRINK, 1f);
        mExpandColorIndicatorAlphaAnimator = ObjectAnimator.ofFloat(mTxtLabel, "alpha", ALPHA_SHRINK, 1f);
        mExpandAnimator = new AnimatorSet();
        mExpandAnimator.playTogether(mExpandColorIndicatorScaleXAnimator, mExpandColorIndicatorScaleYAnimator, mExpandColorIndicatorAlphaAnimator);
        mExpandAnimator.setDuration(ANIMATION_DURATION_MS);

        mShrinkColorIndicatorScaleXAnimator = ObjectAnimator.ofFloat(mImgColorIndicator, "scaleX", 1f, SCALE_SHRINK);
        mShrinkColorIndicatorScaleYAnimator = ObjectAnimator.ofFloat(mImgColorIndicator, "scaleY", 1f, SCALE_SHRINK);
        mShrinkColorIndicatorAlphaAnimator = ObjectAnimator.ofFloat(mTxtLabel, "alpha", 1f, ALPHA_SHRINK);
        mShrinkAnimator = new AnimatorSet();
        mShrinkAnimator.playTogether(mShrinkColorIndicatorScaleXAnimator, mShrinkColorIndicatorScaleYAnimator, mShrinkColorIndicatorAlphaAnimator);
        mShrinkAnimator.setDuration(ANIMATION_DURATION_MS);

        super.onFinishInflate();
    }

    public void setLabel(CharSequence text) {
        mTxtLabel.setText(text);
    }

    public void setColorIndicator(int color) {
        GradientDrawable drawable = (GradientDrawable) mImgColorIndicator.getDrawable().mutate();
        drawable.setColor(color);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (animate) {
            mShrinkAnimator.cancel();
            if (!mExpandAnimator.isRunning()) {
                mExpandColorIndicatorScaleXAnimator.setFloatValues(mImgColorIndicator.getScaleX(), 1f);
                mExpandColorIndicatorScaleYAnimator.setFloatValues(mImgColorIndicator.getScaleY(), 1f);
                mExpandColorIndicatorAlphaAnimator.setFloatValues(mTxtLabel.getAlpha(), 1f);
                mExpandAnimator.start();
            }
        } else {
            mExpandAnimator.cancel();
            mImgColorIndicator.setScaleX(1f);
            mImgColorIndicator.setScaleY(1f);
            mTxtLabel.setAlpha(1f);
        }
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (animate) {
            mExpandAnimator.cancel();
            if (!mShrinkAnimator.isRunning()) {
                mShrinkColorIndicatorScaleXAnimator.setFloatValues(mImgColorIndicator.getScaleX(), SCALE_SHRINK);
                mShrinkColorIndicatorScaleYAnimator.setFloatValues(mImgColorIndicator.getScaleY(), SCALE_SHRINK);
                mShrinkColorIndicatorAlphaAnimator.setFloatValues(mTxtLabel.getAlpha(), ALPHA_SHRINK);
                mShrinkAnimator.start();
            }
        } else {
            mShrinkAnimator.cancel();
            mImgColorIndicator.setScaleX(SCALE_SHRINK);
            mImgColorIndicator.setScaleY(SCALE_SHRINK);
            mTxtLabel.setAlpha(ALPHA_SHRINK);
        }
    }
}
