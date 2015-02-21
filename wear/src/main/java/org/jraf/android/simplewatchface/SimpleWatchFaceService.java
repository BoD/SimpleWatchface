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
package org.jraf.android.simplewatchface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import org.jraf.android.util.log.wrapper.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleWatchFaceService extends CanvasWatchFaceService {
    protected SimpleWatchFaceService mService = this;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        /**
         * Update rate in milliseconds for normal (not ambient) mode.
         */
        private static final long NORMAL_UPDATE_RATE_MS = 1000;

        private boolean mLowBitAmbient;
        private boolean mIsRound;
        private int mChinSize;

        private Paint mBackgroundPaint;
        private Paint mHourMinutesPaint;
        private Paint mSecondsPaint;

        private boolean mIs24HourFormat;
        private java.text.DateFormat mHourMinutesFormat;
        private SimpleDateFormat mSecondsFormat;
        private SimpleDateFormat mDateFormat;
        private SimpleDateFormat mAmPmFormat;

        /**
         * Handler to update the time periodically in interactive mode.
         */
        private Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Log.d();
                invalidate();
                if (shouldTimerBeRunning()) {
                    long timeMs = System.currentTimeMillis();
                    long delayMs = NORMAL_UPDATE_RATE_MS - (timeMs % NORMAL_UPDATE_RATE_MS);
                    sendEmptyMessageDelayed(0, delayMs);
                }
            }
        };


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.d();
            WatchFaceStyle.Builder builder = new WatchFaceStyle.Builder(mService);
            builder.setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE);
            builder.setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_OPAQUE);
            builder.setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE);
            builder.setShowSystemUiTime(false);
            builder.setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_VISIBLE);
            builder.setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
            builder.setShowUnreadCountIndicator(false);
            builder.setStatusBarGravity(Gravity.RIGHT | Gravity.TOP);
            builder.setViewProtection(WatchFaceStyle.PROTECT_HOTWORD_INDICATOR | WatchFaceStyle.PROTECT_STATUS_BAR);
            setWatchFaceStyle(builder.build());

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mBackgroundPaint.setStrokeWidth(1);

            mHourMinutesPaint = new Paint(); mHourMinutesPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
            mHourMinutesPaint.setTextSize(90);

            mSecondsPaint = new Paint();
            mSecondsPaint.setTypeface(Typeface.SANS_SERIF);
            mSecondsPaint.setTextSize(40);

            updatePaints();

            mIs24HourFormat = DateFormat.is24HourFormat(mService);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            Log.d("mLowBitAmbient=" + mLowBitAmbient);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            Log.d();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            Log.d("inAmbientMode=" + inAmbientMode);

            updatePaints();

            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        private void updatePaints() {
            boolean ambientMode = isInAmbientMode();
            if (ambientMode) {
                mBackgroundPaint.setColor(Color.BLACK);
                mHourMinutesPaint.setColor(Color.WHITE);
                mSecondsPaint.setColor(Color.WHITE);
            } else {
                mBackgroundPaint.setColor(Color.WHITE);
                mHourMinutesPaint.setColor(Color.BLACK);
                mSecondsPaint.setColor(Color.BLACK);
            }

            // Disable antialias for ambient mode + low bit
            if (mLowBitAmbient) {
                mHourMinutesPaint.setAntiAlias(!ambientMode);
                mSecondsPaint.setAntiAlias(!ambientMode);
            }
        }

        @Override
        public void onInterruptionFilterChanged(int interruptionFilter) {
            super.onInterruptionFilterChanged(interruptionFilter);
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            Log.d("visible=" + visible);

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();
            Log.d("mIsRound=" + mIsRound + " mChinSize=" + mChinSize);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            Log.d();
            mUpdateTimeHandler.removeMessages(0);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(0);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(0);
            super.onDestroy();
        }

        /*
         * Time / date formatting.
         */
        // region

        private String getHourMinutes(Date date) {
            if (mHourMinutesFormat == null) {
                mHourMinutesFormat = DateFormat.getTimeFormat(mService);
            }
            String formatted = mHourMinutesFormat.format(date);
            if (!mIs24HourFormat) {
                // Remove AM/PM - because we manually add it ourselves
                formatted = formatted.substring(0, formatted.length() - 3);
            }
            return formatted;
        }

        private String getSeconds(Date date) {
            if (mSecondsFormat == null) {
                mSecondsFormat = new SimpleDateFormat("ss");
            }
            return mSecondsFormat.format(date);
        }

        private String getDate(Date date) {
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
            }
            return mDateFormat.format(date);
        }

        @Nullable
        private String getFormattedAmPm(Date date) {
            if (mIs24HourFormat) return null;
            if (mAmPmFormat == null) {
                mAmPmFormat = new SimpleDateFormat("a");
            }
            return mAmPmFormat.format(date);
        }

        // endregion


        /*
         * Actual drawing happens here!
         */
        // region

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            int width = bounds.width();
            int height = bounds.height();
            int peekCardTop = getPeekCardPosition().top;

            // Background
            canvas.drawRect(0, 0, width - 1, height - 1, mBackgroundPaint);

            Date date = new Date();

            // Hour / minutes
            String hourMinutesStr = getHourMinutes(date);
            Rect hourMinutesBounds = new Rect();
            mHourMinutesPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds); int hourMinutesWidth = hourMinutesBounds.right;
            int hourMinutesHeight = hourMinutesBounds.height();

            if (!isInAmbientMode()) {
                // Seconds
                String secondsStr = " " + getSeconds(date);
                Rect secondsBounds = new Rect();
                mSecondsPaint.getTextBounds(secondsStr, 0, secondsStr.length(), secondsBounds); int secondsWidth = secondsBounds.right;

                int totalWidth = hourMinutesWidth + secondsWidth;
                int x = (width - totalWidth) / 2;

                Paint p = new Paint();
                p.setColor(Color.RED);
                canvas.drawRect(x, 0, x + totalWidth, hourMinutesHeight, p);

                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesPaint);
                canvas.drawText(secondsStr, x + hourMinutesWidth, hourMinutesHeight, mSecondsPaint);
            } else {

                int x = (width - hourMinutesWidth) / 2;

                Paint p = new Paint();
                p.setColor(Color.RED);
                canvas.drawRect(x, 0, x + hourMinutesWidth, hourMinutesHeight, p);

                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesPaint);
            }

        }

        //endregion
    }
}
