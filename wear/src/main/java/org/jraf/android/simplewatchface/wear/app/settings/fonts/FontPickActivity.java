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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.wearable.activity.ConfirmationActivity;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowInsets;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.settings.ZoomOutPageTransformer;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FontPickActivity extends Activity {
    public enum Mode {TIME, DATE,}

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String EXTRA_RESULT = "EXTRA_RESULT";

    @Bind(R.id.vpgPresets)
    protected ViewPager mVpgPresets;

    private Mode mMode = Mode.TIME;
    private FontPagerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preset_pick);

        mMode = (Mode) getIntent().getSerializableExtra(EXTRA_MODE);
        mAdapter = new FontPagerAdapter(FontPickActivity.this, mMode);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.viewStub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ButterKnife.bind(FontPickActivity.this, stub);
                mVpgPresets.setAdapter(mAdapter);
                mVpgPresets.setPageTransformer(true, new ZoomOutPageTransformer());
                mVpgPresets.setPageMargin(0);

                // Reveal a bit of the next page to indicate to the user that this is a ViewPager
                mVpgPresets.beginFakeDrag();
                Object revealObject = new Object() {
                    private int mPrevValue;

                    public void setDragBy(int value) {
                        mVpgPresets.fakeDragBy(value - mPrevValue);
                        mPrevValue = value;
                    }
                };
                int revealDistance = -getResources().getDimensionPixelOffset(R.dimen.settings_preset_pick_reveal);
                ObjectAnimator revealAnimator = ObjectAnimator.ofInt(revealObject, "dragBy", 0, revealDistance);
                revealAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mVpgPresets.endFakeDrag();
                    }
                });
                revealAnimator.setDuration(200);
                revealAnimator.setStartDelay(500);
                revealAnimator.start();

            }
        });

        stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                mAdapter.setIsRound(insets.isRound());
                stub.onApplyWindowInsets(insets);
                return insets;
            }
        });
    }

    @OnClick(R.id.btnCancel)
    protected void onCancelClicked() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.btnOk)
    protected void onOkClicked() {
        Intent result = new Intent();
        int currentIndex = mVpgPresets.getCurrentItem();
        String fontName = mAdapter.getFontName(currentIndex);
        result.putExtra(EXTRA_RESULT, fontName);
        setResult(RESULT_OK, result);

        showConfirmAnimation();

        finish();
        overridePendingTransition(0, 0);
    }

    private void showConfirmAnimation() {
        Intent intent = new Intent(this, ConfirmationActivity.class);
        intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION);
        startActivity(intent);
    }
}