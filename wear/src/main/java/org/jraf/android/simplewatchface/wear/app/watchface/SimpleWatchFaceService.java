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
package org.jraf.android.simplewatchface.wear.app.watchface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import org.jraf.android.util.log.wrapper.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
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
         * This is supposed to be the 'widest' (in pixels) possible value for the seconds.
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
        private static final float DATE_SPACE = 8f;

        private boolean mLowBitAmbient;
        private boolean mIsRound;
        private int mChinSize;

        private Paint mBackgroundPaint;
        private Paint mHourMinutesAmbientPaint;
        private Paint mHourMinutesNormalPaint;
        private Paint mSecondsPaint;
        private Paint mDatePaint;

        private Time mTime = new Time();
        private boolean mIs24HourFormat;
        private SimpleDateFormat mDateFormat;

        private HashMap<String, Integer> mCachedSizesForHourMinutesAmbient = new HashMap<>(60 * 60, 1f);
        private HashMap<String, Integer> mCachedSizesForHourMinutesNormal = new HashMap<>(60 * 60, 1f);
        private boolean mNeedToAdjustTextSizeForDate;
        private int mCachedDateDay;
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
        public void invalidate() {
            Log.d();
            super.invalidate();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            Log.d();
            // Watch face style
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

            // Typefaces
            Typeface timeTypeface = Typeface.createFromAsset(getAssets(), "fonts/Exo2-ExtraBoldItalic.ttf");
            Typeface dateTypeface = Typeface.createFromAsset(getAssets(), "fonts/Exo2-Italic.ttf");

            // Paints
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mBackgroundPaint.setStrokeWidth(1);

            mHourMinutesAmbientPaint = new Paint();
            mHourMinutesAmbientPaint.setTypeface(timeTypeface);
            mHourMinutesAmbientPaint.setTextSize(150);

            mHourMinutesNormalPaint = new Paint();
            mHourMinutesNormalPaint.setTypeface(timeTypeface);
            mHourMinutesNormalPaint.setTextSize(140);

            mSecondsPaint = new Paint();
            mSecondsPaint.setTypeface(timeTypeface);
            mSecondsPaint.setTextSize(140 * SECONDS_SIZE_FACTOR);

            mDatePaint = new Paint();
            mDatePaint.setTypeface(dateTypeface);
            mDatePaint.setTextSize(20);

            updatePaints();

            mIs24HourFormat = DateFormat.is24HourFormat(mService);
        }

        private void updatePaints() {
            boolean ambientMode = isInAmbientMode();
            if (ambientMode) {
                mBackgroundPaint.setColor(Color.BLACK);
                mHourMinutesAmbientPaint.setColor(Color.WHITE);
                mHourMinutesNormalPaint.setColor(Color.WHITE);
                mSecondsPaint.setColor(Color.WHITE);
                mDatePaint.setColor(Color.WHITE);
            } else {
                mBackgroundPaint.setColor(Color.BLACK);
                mHourMinutesAmbientPaint.setColor(Color.WHITE);
                mHourMinutesNormalPaint.setColor(Color.WHITE);
                mSecondsPaint.setColor(Color.WHITE);
                mDatePaint.setColor(Color.WHITE);
            }

            // Disable antialias for ambient mode + low bit
            if (mLowBitAmbient) {
                mHourMinutesAmbientPaint.setAntiAlias(!ambientMode);
                mHourMinutesNormalPaint.setAntiAlias(!ambientMode);
                mSecondsPaint.setAntiAlias(!ambientMode);
                mDatePaint.setAntiAlias(!ambientMode);
            }
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

            if (inAmbientMode) {
                invalidate();
            }

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
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

        private String getHourMinutes() {
            int hour = mTime.hour;
            if (!mIs24HourFormat) hour = hour % 12;
            StringBuilder res = new StringBuilder(String.valueOf(hour));
            res.append(':');
            if (mTime.minute < 10) res.append('0');
            res.append(String.valueOf(mTime.minute));
            return res.toString();
        }

        private String getSeconds() {
            StringBuilder res = new StringBuilder();
            if (mTime.second < 10) res.append('0');
            res.append(String.valueOf(mTime.second));
            return res.toString();
        }

        private String getFormattedAmPm() {
            if (mTime.hour <= 11) return "AM";
            return "PM";
        }

        private String getDate() {
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
            }
            if (mCachedDateDay != mTime.monthDay) {
                // New day: format the date
                mNeedToAdjustTextSizeForDate = true;
                mCachedDateStr = mDateFormat.format(new Date());
                mCachedDateDay = mTime.monthDay;
            }
            return mCachedDateStr;
        }

        // endregion


        /*
         * Actual drawing happens here!
         */
        // region

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            Log.d();
            int canvasWidth = bounds.width();
            int canvasHeight = bounds.height();
            int peekCardTop = getPeekCardPosition().top;

            // Background
            canvas.drawRect(0, 0, canvasWidth - 1, canvasHeight - 1, mBackgroundPaint);

            mTime.setToNow();

            // Hour / minutes
            String hourMinutesStr = getHourMinutes();

            int hourMinutesHeight;
            if (isInAmbientMode()) {
                // Ambient mode: don't show seconds
                adjustTextSizeForHourMinutes(hourMinutesStr, canvasWidth);
                Rect hourMinutesBounds = new Rect();
                mHourMinutesAmbientPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                int hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                int x = (canvasWidth - hourMinutesWidth) / 2 - hourMinutesBounds.left;
                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesAmbientPaint);
            } else {
                // 'Normal' mode: show seconds
                adjustTextSizeForHourMinutesSeconds(hourMinutesStr, canvasWidth);

                // Hour / minutes
                Rect hourMinutesBounds = new Rect();
                mHourMinutesNormalPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                int hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                // Seconds
                String secondsStr = getSeconds();
                Rect secondsBounds = new Rect();
                mSecondsPaint.getTextBounds(WIDEST_SECONDS, 0, WIDEST_SECONDS.length(), secondsBounds);
                int secondsWidth = secondsBounds.right;

                int totalWidth = hourMinutesWidth + secondsWidth;
                int x = (canvasWidth - totalWidth) / 2 - hourMinutesBounds.left;

                canvas.drawText(hourMinutesStr, x, hourMinutesHeight, mHourMinutesNormalPaint);
                canvas.drawText(secondsStr, x + hourMinutesWidth + SECONDS_SPACE - secondsBounds.left, hourMinutesHeight, mSecondsPaint);
            }

            // Date
            String dateStr = getDate();
            adjustTextSizeForDate(dateStr, canvasWidth);
            Rect dateBounds = new Rect();
            mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
            int dateWidth = dateBounds.right;
            int dateHeight = dateBounds.height();

            int x = (canvasWidth - dateWidth) / 2 - dateBounds.left;
            canvas.drawText(dateStr, x, hourMinutesHeight + dateHeight + DATE_SPACE, mDatePaint);
        }

        //endregion


        /*
         * Font size adjustment.
         */
        // region

        private void adjustTextSizeForHourMinutes(String hourMinutesStr, int canvasWidth) {
            Integer fitSize = mCachedSizesForHourMinutesAmbient.get(hourMinutesStr);
            if (fitSize == null) {
                // Cache miss: find the best value

                int step = 4;
                int measure = measureHourMinutes(hourMinutesStr);
                if (measure == canvasWidth) {
                    // Perfect fit: do nothing
                } else if (measure < canvasWidth) {
                    // Too small: try bigger
                    do {
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() + step);
                    } while ((measure = measureHourMinutes(hourMinutesStr)) < canvasWidth);
                    if (measure > canvasWidth) {
                        // We went too far
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() - step);
                    }
                } else {
                    // Too big: try smaller
                    do {
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() - step);
                    } while (measureHourMinutes(hourMinutesStr) > canvasWidth);
                }

                // Update the cache
                mCachedSizesForHourMinutesAmbient.put(hourMinutesStr, (int) mHourMinutesAmbientPaint.getTextSize());
            } else {
                // Cache hit: use the value
                mHourMinutesAmbientPaint.setTextSize(fitSize);
            }
        }

        private int measureHourMinutes(String hourMinutesStr) {
            int res = (int) mHourMinutesAmbientPaint.measureText(hourMinutesStr);
            Log.d("hourMinutesStr=" + hourMinutesStr + " res=" + res);
            return res;
        }

        private void adjustTextSizeForHourMinutesSeconds(String hourMinutesStr, int canvasWidth) {
            Integer fitSize = mCachedSizesForHourMinutesNormal.get(hourMinutesStr);
            if (fitSize == null) {
                // Cache miss: find the best value
                int step = 4;
                int measure = measureHourMinutesSeconds(hourMinutesStr);
                if (measure == canvasWidth) {
                    // Perfect fit: do nothing
                } else if (measure < canvasWidth) {
                    // Too small: try bigger
                    do {
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() + step);
                        mSecondsPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                    } while ((measure = measureHourMinutesSeconds(hourMinutesStr)) < canvasWidth);
                    if (measure > canvasWidth) {
                        // We went too far
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() - step);
                        mSecondsPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                    }
                } else {
                    // Too big: try smaller
                    do {
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() - step);
                        mSecondsPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() * SECONDS_SIZE_FACTOR);
                    } while (measureHourMinutesSeconds(hourMinutesStr) > canvasWidth);
                }

                // Update the cache
                mCachedSizesForHourMinutesNormal.put(hourMinutesStr, (int) mHourMinutesNormalPaint.getTextSize());
            } else {
                // Cache hit: use the value
                mHourMinutesNormalPaint.setTextSize(fitSize);
                mSecondsPaint.setTextSize(fitSize * SECONDS_SIZE_FACTOR);
            }
        }

        private int measureHourMinutesSeconds(String hourMinutesStr) {
            int res = (int) (mHourMinutesNormalPaint.measureText(hourMinutesStr) + mSecondsPaint.measureText(WIDEST_SECONDS) + SECONDS_SPACE);
            Log.d("hourMinutesStr=" + hourMinutesStr + " res=" + res);
            return res;
        }

        private void adjustTextSizeForDate(String dateStr, int canvasWidth) {
            if (mNeedToAdjustTextSizeForDate) {
                mCachedDateStr = dateStr;
                int step = 4;
                int measure = (int) mDatePaint.measureText(dateStr);
                if (measure == canvasWidth) {
                    // Perfect fit: do nothing
                } else if (measure < canvasWidth) {
                    // Too small: try bigger
                    do {
                        mDatePaint.setTextSize(mDatePaint.getTextSize() + step);
                    } while ((measure = (int) mDatePaint.measureText(dateStr)) < canvasWidth);
                    if (measure > canvasWidth) {
                        // We went too far
                        mHourMinutesAmbientPaint.setTextSize(mDatePaint.getTextSize() - step);
                    }
                } else {
                    // Too big: try smaller
                    do {
                        mDatePaint.setTextSize(mDatePaint.getTextSize() - step);
                    } while (mDatePaint.measureText(dateStr) > canvasWidth);
                }

                mNeedToAdjustTextSizeForDate = false;
            }
        }

        // endregion
    }
}
