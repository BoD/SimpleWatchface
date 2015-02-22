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

        /**
         * This is supposed to be the 'widest' (in pixels) possible value for the seconds value.
         */
        private static final String WIDEST_SECONDS = "00";

        /**
         * How small seconds are compared to hour / minutes.
         */
        private static final float SECONDS_SIZE_FACTOR = .33f;

        /**
         * Number of pixels between hour / minutes and seconds.
         */
        private static final float SECONDS_SPACE = 2f;

        /**
         * Number of pixels between time and date.
         */
        private static final float DATE_SPACE = 4f;

        private boolean mLowBitAmbient;
        private boolean mIsRound;
        private int mChinSize;

        private Paint mBackgroundPaint;
        private Paint mHourMinutesPaint;
        private Paint mSecondsPaint;
        private Paint mDatePaint;

        private boolean mIs24HourFormat;
        private SimpleDateFormat mHourMinutesFormat;
        private SimpleDateFormat mSecondsFormat;
        private SimpleDateFormat mAmPmFormat;
        private SimpleDateFormat mDateFormat;
        private String mCachedHourMinutesStr;
        private String mCachedDateStr;

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

            Typeface timeTypeface = Typeface.createFromAsset(getAssets(), "fonts/Exo2-ExtraBoldItalic.ttf");
            mHourMinutesPaint = new Paint();
            mHourMinutesPaint.setTypeface(timeTypeface);
            mHourMinutesPaint.setTextSize(90);

            mSecondsPaint = new Paint();
            mSecondsPaint.setTypeface(timeTypeface);
            mSecondsPaint.setTextSize(90 * SECONDS_SIZE_FACTOR);

            Typeface dateTypeface = Typeface.createFromAsset(getAssets(), "fonts/Exo2-Italic.ttf");
            mDatePaint = new Paint();
            mDatePaint.setTypeface(dateTypeface);
            mDatePaint.setTextSize(20);

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
                mDatePaint.setColor(Color.WHITE);
            } else {
                mBackgroundPaint.setColor(Color.BLACK);
                mHourMinutesPaint.setColor(Color.WHITE);
                mSecondsPaint.setColor(Color.WHITE);
                mDatePaint.setColor(Color.WHITE);
            }

            // Disable antialias for ambient mode + low bit
            if (mLowBitAmbient) {
                mHourMinutesPaint.setAntiAlias(!ambientMode);
                mSecondsPaint.setAntiAlias(!ambientMode);
                mDatePaint.setAntiAlias(!ambientMode);
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
                mHourMinutesFormat = (SimpleDateFormat) DateFormat.getTimeFormat(mService);
                // We actually don't want HH/hh but H/h
                String pattern = mHourMinutesFormat.toPattern();
                pattern = pattern.replace("HH", "H").replace("hh", "h");
                mHourMinutesFormat = new SimpleDateFormat(pattern);
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

        @Nullable
        private String getFormattedAmPm(Date date) {
            if (mIs24HourFormat) return null;
            if (mAmPmFormat == null) {
                mAmPmFormat = new SimpleDateFormat("a");
            }
            return mAmPmFormat.format(date);
        }

        private String getDate(Date date) {
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
            }
            return mDateFormat.format(date);
        }

        // endregion


        /*
         * Actual drawing happens here!
         */
        // region

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            int canvasWidth = bounds.width();
            int canvasHeight = bounds.height();
            int peekCardTop = getPeekCardPosition().top;

            // Background
            canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - 1, mBackgroundPaint);

            Date now = new Date();

            // Hour / minutes
            String hourMinutesStr = getHourMinutes(now);

            int hourMinutesHeight;
            if (isInAmbientMode()) {
                // Ambient mode: don't show seconds
                adjustTextSizeForHourMinutes(hourMinutesStr, canvasWidth);
                Rect hourMinutesBounds = new Rect();
                mHourMinutesPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                int hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                int x = (canvasWidth - hourMinutesWidth) / 2 - hourMinutesBounds.left;
                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesPaint);
            } else {
                // 'Normal' mode: show seconds
                adjustTextSizeForHourMinutesSeconds(hourMinutesStr, canvasWidth);

                // Hour / minutes
                Rect hourMinutesBounds = new Rect();
                mHourMinutesPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                int hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                // Seconds
                String secondsStr = getSeconds(now);
                Rect secondsBounds = new Rect();
                mSecondsPaint.getTextBounds(WIDEST_SECONDS, 0, WIDEST_SECONDS.length(), secondsBounds);
                int secondsWidth = secondsBounds.right;

                int totalWidth = hourMinutesWidth + secondsWidth;
                int x = (canvasWidth - totalWidth) / 2 - hourMinutesBounds.left;

                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesPaint);
                canvas.drawText(secondsStr, x + hourMinutesWidth + SECONDS_SPACE + DATE_SPACE - secondsBounds.left, hourMinutesHeight, mSecondsPaint);
            }

            // Date
            String dateStr = getDate(now);
            adjustTextSizeForDate(dateStr, canvasWidth);
            Rect dateBounds = new Rect();
            mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
            int dateWidth = dateBounds.right;
            int dateHeight = dateBounds.height();

            int x = (canvasWidth - dateWidth) / 2 - dateBounds.left;
            canvas.drawText(dateStr, x, hourMinutesHeight + dateHeight, mDatePaint);
        }

        //endregion

        private void adjustTextSizeForHourMinutes(String hourMinutesStr, int canvasWidth) {
            mCachedHourMinutesStr = null;
            int step = 4;
            int measure = (int) mHourMinutesPaint.measureText(hourMinutesStr);
            if (measure < canvasWidth) {
                // Too small
                do {
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() + step);
                } while ((measure = (int) mHourMinutesPaint.measureText(hourMinutesStr)) < canvasWidth);
                if (measure > canvasWidth) {
                    // We went too far
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() - step);
                }
            } else {
                // Too big
                do {
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() - step);
                } while (mHourMinutesPaint.measureText(hourMinutesStr) > canvasWidth);
            }
        }

        private void adjustTextSizeForHourMinutesSeconds(String hourMinutesStr, int canvasWidth) {
            if (hourMinutesStr.equals(mCachedHourMinutesStr)) {
                // We already adjusted for this hour / minutes - don't do anything
                return;
            }
            mCachedHourMinutesStr = hourMinutesStr;
            int step = 4;
            int measure = measureHourMinutesSeconds(hourMinutesStr);
            if (measure < canvasWidth) {
                // Too small
                do {
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() + step);
                    mSecondsPaint.setTextSize(mHourMinutesPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                } while ((measure = measureHourMinutesSeconds(hourMinutesStr)) < canvasWidth);
                if (measure > canvasWidth) {
                    // We went too far
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() - step);
                    mSecondsPaint.setTextSize(mHourMinutesPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                }
            } else {
                // Too big
                do {
                    mHourMinutesPaint.setTextSize(mHourMinutesPaint.getTextSize() - step);
                    mSecondsPaint.setTextSize(mHourMinutesPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                } while (measureHourMinutesSeconds(hourMinutesStr) > canvasWidth);
            }
        }

        private int measureHourMinutesSeconds(String hourMinutesStr) {
            return (int) (mHourMinutesPaint.measureText(hourMinutesStr) + mSecondsPaint.measureText(WIDEST_SECONDS) + SECONDS_SPACE);
        }

        private void adjustTextSizeForDate(String dateStr, int canvasWidth) {
            if (dateStr.equals(mCachedDateStr)) {
                // We already adjusted for this date - don't do anything
                return;
            }
            mCachedDateStr = dateStr;
            int step = 4;
            int measure = (int) mDatePaint.measureText(dateStr);
            if (measure < canvasWidth) {
                // Too small
                do {
                    mDatePaint.setTextSize(mDatePaint.getTextSize() + step);
                } while ((measure = (int) mDatePaint.measureText(dateStr)) < canvasWidth);
                if (measure > canvasWidth) {
                    // We went too far
                    mHourMinutesPaint.setTextSize(mDatePaint.getTextSize() - step);
                }
            } else {
                // Too big
                do {
                    mDatePaint.setTextSize(mDatePaint.getTextSize() - step);
                } while (mDatePaint.measureText(dateStr) > canvasWidth);
            }
        }
    }
}
