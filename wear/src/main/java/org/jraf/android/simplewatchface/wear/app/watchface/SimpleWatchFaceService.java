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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
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

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.util.log.wrapper.Log;

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
         * How small the am/pm indicator is compared to hour / minutes.
         */
        private static final float AM_PM_SIZE_FACTOR = .26f;

        /**
         * Number of pixels between hour / minutes and seconds.
         */
        private static final int SECONDS_SPACE = 2;

        /**
         * Number of pixels between time and date.
         */
        private static final int DATE_SPACE = 8;

        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mIsRound;
        private int mChinSize;

        private int mColorBackgroundNormal;
        private int mColorBackgroundAmbient;
        private int mColorDateNormal;
        private int mColorDateAmbient;
        private int mColorTimeNormal;
        private int mColorTimeAmbient;
        private int mColorSecondsNormal;
        private int mColorAmPmNormal;
        private Bitmap mBackgroundPicture;

        private SettingsHelper mSettingsHelper;

        private Paint mBackgroundPaint;
        private Paint mHourMinutesAmbientPaint;
        private Paint mHourMinutesNormalPaint;
        private Paint mSecondsPaint;
        private Paint mAmPmPaint;
        private Paint mDatePaint;

        private Time mTime = new Time();
        private boolean mIs24HourFormat;
        private SimpleDateFormat mDateFormat;

        private int mCachedDateDay;
        private String mCachedDateStr;

        private HashMap<String, Integer> mDateSizeCache = new HashMap<>(4);

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

        private SettingsHelper.SettingsChangeListener mSettingsChangeListener =
                new SettingsHelper.SettingsChangeListener() {
                    @Override
                    public void onSettingsChanged() {
                        // Reset the size cache because it is no longer valid with a different font
                        mDateSizeCache.clear();

                        mBackgroundPicture = mSettingsHelper.getBackgroundPicture();
                        updateColors();
                        updatePaints();
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

            mIs24HourFormat = DateFormat.is24HourFormat(mService);

            mSettingsHelper = SettingsHelper.get(SimpleWatchFaceService.this);

            mSettingsHelper.addSettingsChangeListener(mSettingsChangeListener);
            mBackgroundPicture = mSettingsHelper.getBackgroundPicture();

            // Paints
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mBackgroundPaint.setStrokeWidth(1);

            mHourMinutesAmbientPaint = new Paint();
            mHourMinutesAmbientPaint.setTextSize(150);

            mHourMinutesNormalPaint = new Paint();
            mHourMinutesNormalPaint.setTextSize(140);

            mSecondsPaint = new Paint();
            mSecondsPaint.setTextSize(140 * SECONDS_SIZE_FACTOR);

            mAmPmPaint = new Paint();
            mAmPmPaint.setTextSize(140 * AM_PM_SIZE_FACTOR);

            mDatePaint = new Paint();
            mDatePaint.setTextSize(20);

            // Colors
            updateColors();
            updatePaints();
        }

        private void updateColors() {
            mColorBackgroundNormal = mSettingsHelper.getColorBackground();
            mColorBackgroundAmbient = getResources().getColor(R.color.background_ambient);
            mColorTimeNormal = mSettingsHelper.getColorHourMinutes();
            mColorTimeAmbient = getResources().getColor(R.color.time_ambient);
            mColorDateNormal = mSettingsHelper.getColorDate();
            mColorDateAmbient = getResources().getColor(R.color.date_ambient);
            mColorSecondsNormal = mSettingsHelper.getColorSeconds();
            mColorAmPmNormal = mSettingsHelper.getColorAmPm();
        }

        private void updatePaints() {
            boolean ambientMode = isInAmbientMode();

            // Colors
            mHourMinutesAmbientPaint.setColor(mColorTimeAmbient);
            mHourMinutesNormalPaint.setColor(mColorTimeNormal);
            mSecondsPaint.setColor(mColorSecondsNormal);
            mAmPmPaint.setColor(mColorAmPmNormal);

            if (ambientMode) {
                // Ambient mode: we maximize contrast
                mBackgroundPaint.setColor(mColorBackgroundAmbient);
                mDatePaint.setColor(mColorDateAmbient);
            } else {
                // Normal mode: colors!
                if (mBackgroundPicture == null) {
                    mBackgroundPaint.setColor(mColorBackgroundNormal);
                } else {
                    mBackgroundPaint.setColor(0xFFFFFFFF); // white
                }
                mDatePaint.setColor(mColorDateNormal);
            }

            // Enable antialias for normal mode / disable it for ambient mode + low bit
            boolean antialias = !ambientMode || !mLowBitAmbient;
            mHourMinutesAmbientPaint.setAntiAlias(antialias);
            mHourMinutesNormalPaint.setAntiAlias(antialias);
            mSecondsPaint.setAntiAlias(antialias);
            mAmPmPaint.setAntiAlias(antialias);
            mDatePaint.setAntiAlias(antialias);

            // Set an outline for ambient + burn in protection / disable it otherwise
            boolean outline = ambientMode && mBurnInProtection;
            Paint.Style style;
            if (outline) {
                style = Paint.Style.STROKE;
            } else {
                style = Paint.Style.FILL;
            }
            mHourMinutesAmbientPaint.setStyle(style);
            mDatePaint.setStyle(style);

            // Typefaces
            Typeface timeTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + mSettingsHelper.getFontTime());
            Typeface dateTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + mSettingsHelper.getFontDate());
            mHourMinutesAmbientPaint.setTypeface(timeTypeface);
            mHourMinutesNormalPaint.setTypeface(timeTypeface);
            mSecondsPaint.setTypeface(timeTypeface);
            mAmPmPaint.setTypeface(timeTypeface);
            mDatePaint.setTypeface(dateTypeface);

            // Shadows
            int shadowColor = 0xFF000000; // black
            int shadowRadiusBig = getResources().getDimensionPixelSize(R.dimen.wf_shadow_radius_big);
            int shadowDeltaBig = (int) (shadowRadiusBig / 1.5);
            int shadowRadiusSmall = getResources().getDimensionPixelSize(R.dimen.wf_shadow_radius_small);
            int shadowDeltaSmall = (int) (shadowRadiusSmall / 1.5);
            mHourMinutesNormalPaint.setShadowLayer(shadowRadiusBig, shadowDeltaBig, shadowDeltaBig, shadowColor);
            mSecondsPaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
            mAmPmPaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
            mDatePaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
            Log.d("mLowBitAmbient=" + mLowBitAmbient + " mBurnInProtection" + mBurnInProtection);
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
            mSettingsHelper.removeSettingsChangeListener(mSettingsChangeListener);
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

        private String getAmPm() {
            if (mTime.hour <= 11) return "AM";
            return "PM";
        }

        private String getDate() {
            if (mDateFormat == null) {
                mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
            }
            if (mCachedDateDay != mTime.monthDay) {
                // New day: format the date
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

            mTime.setToNow();

            int canvasWidth = bounds.width();
            int canvasHeight = bounds.height();

            Rect peekCardPosition = getPeekCardPosition();
            int peekCardTop;
            if (peekCardPosition.height() == 0) {
                // No peek card
                peekCardTop = canvasHeight;
            } else {
                peekCardTop = peekCardPosition.top;
            }

            if (isInAmbientMode()) {
                // Background
                canvas.drawColor(mColorBackgroundAmbient);

                onDrawAmbient(canvas, canvasWidth, canvasHeight, peekCardTop);
            } else {
                // Background
                if (mBackgroundPicture == null) {
                    canvas.drawColor(mColorBackgroundNormal);
                } else {
                    Matrix matrix = new Matrix();
                    RectF src = new RectF(0, 0, mBackgroundPicture.getWidth(), mBackgroundPicture.getHeight());
                    matrix.setRectToRect(src, new RectF(bounds), Matrix.ScaleToFit.CENTER);
                    canvas.drawBitmap(mBackgroundPicture, matrix, mBackgroundPaint);
                }

                onDrawNormal(canvas, canvasWidth, canvasHeight, peekCardTop);
            }
        }

        private void onDrawAmbient(Canvas canvas, int canvasWidth, int canvasHeight, int peekCardTop) {
            String hourMinutesStr = getHourMinutes();
            String dateStr = getDate();

            // Find the biggest possible font size that fits
            int step = 4;
            boolean wasNarrower = false;
            boolean firstTime = true;
            Rect hourMinutesBounds = new Rect();
            int hourMinutesWidth;
            int hourMinutesHeight;
            Rect dateBounds = new Rect();
            int dateWidth;
            int dateHeight;
            int top;
            while (true) {
                // Measure hour / minutes
                mHourMinutesAmbientPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                // Adjust the date font size to match the hour / minutes
                adjustTextSizeForDate(dateStr, hourMinutesWidth);

                // Measure date
                mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
                dateWidth = dateBounds.right;
                dateHeight = dateBounds.height();

                int totalHeight = hourMinutesHeight + DATE_SPACE + dateHeight;
                top = (peekCardTop - totalHeight) / 2;

                int availableWidth = getAvailableWidthForTop(canvasWidth, top);

                if (hourMinutesWidth == availableWidth) {
                    // Perfect fit: stop here
                    break;
                } else if (hourMinutesWidth < availableWidth) {
                    // Narrower
                    if (firstTime || wasNarrower) {
                        // Was already narrower, try a bigger font
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() + step);
                    } else {
                        // Went from wider to narrower: we found the best fit: stop here
                        break;
                    }

                    wasNarrower = true;
                } else {
                    // Wider
                    if (firstTime || !wasNarrower) {
                        // Was already wider, try a smaller font
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() - step);
                    } else {
                        // Went from narrower to wider: we went too far: go back to the previous font size and stop here
                        mHourMinutesAmbientPaint.setTextSize(mHourMinutesAmbientPaint.getTextSize() - step);

                        // Measure hour / minutes
                        mHourMinutesAmbientPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                        hourMinutesWidth = hourMinutesBounds.right;
                        hourMinutesHeight = hourMinutesBounds.height();

                        // Adjust the date font size to match the hour / minutes
                        adjustTextSizeForDate(dateStr, hourMinutesWidth);

                        // Measure date
                        mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
                        dateWidth = dateBounds.right;
                        dateHeight = dateBounds.height();
                        break;
                    }

                    wasNarrower = false;
                }
                firstTime = false;
            }

            // Draw hour / minutes
            int hourMinutesX = (canvasWidth - hourMinutesWidth) / 2 - hourMinutesBounds.left;
            int hourMinutesY = top + hourMinutesHeight;
            canvas.drawText(hourMinutesStr, hourMinutesX, hourMinutesY, mHourMinutesAmbientPaint);

            // Draw the date
            int dateX = (canvasWidth - dateWidth) / 2 - dateBounds.left;
            int dateY = top + hourMinutesHeight + DATE_SPACE + dateHeight;
            canvas.drawText(dateStr, dateX, dateY, mDatePaint);
        }

        private int getAvailableWidthForTop(int width, int top) {
            if (!mIsRound) return width;
            return (int) (2 * Math.sqrt(width * top - top * top));
        }

        private void onDrawNormal(Canvas canvas, int canvasWidth, int canvasHeight, int peekCardTop) {
            String hourMinutesStr = getHourMinutes();
            String secondsStr = getSeconds();
            String dateStr = getDate();

            // Find the biggest possible font size that fits
            int step = 4;
            boolean wasNarrower = false;
            boolean firstTime = true;
            Rect hourMinutesBounds = new Rect();
            int hourMinutesWidth;
            int hourMinutesHeight;
            Rect secondsBounds = new Rect();
            int secondsWidth;
            int hourMinutesSecondsWidth;
            Rect dateBounds = new Rect();
            int dateWidth;
            int dateHeight;
            int top;
            while (true) {
                // Measure hour / minutes
                mHourMinutesNormalPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                hourMinutesWidth = hourMinutesBounds.right;
                hourMinutesHeight = hourMinutesBounds.height();

                // Measure seconds
                mSecondsPaint.getTextBounds(WIDEST_SECONDS, 0, WIDEST_SECONDS.length(), secondsBounds);
                secondsWidth = secondsBounds.right;

                hourMinutesSecondsWidth = hourMinutesWidth + SECONDS_SPACE + secondsWidth;
                Log.d("hourMinutesSecondsWidth=" + hourMinutesSecondsWidth);

                // Adjust the date font size to match the hour / minutes / seconds
                adjustTextSizeForDate(dateStr, hourMinutesSecondsWidth);

                // Measure date
                mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
                dateWidth = dateBounds.right;
                dateHeight = dateBounds.height();

                int totalHeight = hourMinutesHeight + DATE_SPACE + dateHeight;
                top = (peekCardTop - totalHeight) / 2;

                int availableWidth = getAvailableWidthForTop(canvasWidth, top);

                if (hourMinutesSecondsWidth == availableWidth) {
                    // Perfect fit: stop here
                    break;
                } else if (hourMinutesSecondsWidth < availableWidth) {
                    // Narrower
                    if (firstTime || wasNarrower) {
                        // Was already narrower, try a bigger font
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() + step);
                        adjustSecondsAndAmPmSizes();
                    } else {
                        // Went from wider to narrower: we found the best fit: stop here
                        break;
                    }

                    wasNarrower = true;
                } else {
                    // Wider
                    if (firstTime || !wasNarrower) {
                        // Was already wider, try a smaller font
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() - step);
                        adjustSecondsAndAmPmSizes();
                    } else {
                        // Went from narrower to wider: we went too far: go back to the previous font size and stop here
                        mHourMinutesNormalPaint.setTextSize(mHourMinutesNormalPaint.getTextSize() - step);
                        adjustSecondsAndAmPmSizes();

                        // Measure hour / minutes
                        mHourMinutesNormalPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
                        hourMinutesWidth = hourMinutesBounds.right;
                        hourMinutesHeight = hourMinutesBounds.height();

                        // Measure seconds
                        mSecondsPaint.getTextBounds(WIDEST_SECONDS, 0, WIDEST_SECONDS.length(), secondsBounds);
                        secondsWidth = secondsBounds.right;

                        hourMinutesSecondsWidth = hourMinutesWidth + SECONDS_SPACE + secondsWidth;

                        // Adjust the date font size to match the hour / minutes
                        adjustTextSizeForDate(dateStr, hourMinutesSecondsWidth);

                        // Measure date
                        mDatePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
                        dateWidth = dateBounds.right;
                        dateHeight = dateBounds.height();
                        break;
                    }

                    wasNarrower = false;
                }
                firstTime = false;
            }

            int hourMinutesX = (canvasWidth - hourMinutesSecondsWidth) / 2 - hourMinutesBounds.left;
            int hourMinutesY = top + hourMinutesHeight;

            // Draw hour / minutes
            canvas.drawText(hourMinutesStr, hourMinutesX, hourMinutesY, mHourMinutesNormalPaint);

            // Draw seconds
            int secondsX = hourMinutesX + hourMinutesWidth + SECONDS_SPACE - secondsBounds.left;
            canvas.drawText(secondsStr, secondsX, hourMinutesY, mSecondsPaint);

            // Draw AM/PM
            if (!mIs24HourFormat) {
                String amPm = getAmPm();
                int amPmY = top + secondsBounds.height();
                canvas.drawText(amPm, secondsX, amPmY, mAmPmPaint);
            }

            // Draw the date
            int dateX = (canvasWidth - dateWidth) / 2 - dateBounds.left;
            int dateY = top + hourMinutesHeight + DATE_SPACE + dateHeight;
            canvas.drawText(dateStr, dateX, dateY, mDatePaint);
        }

        //endregion


        /*
         * Font size adjustment.
         */
        // region

        private void adjustTextSizeForDate(String dateStr, int wantedWidth) {
            mDatePaint.setTextSize(getFitTextSizeForDate(dateStr, wantedWidth));
        }

        private int getFitTextSizeForDate(String dateStr, int wantedWidth) {
            String key = wantedWidth + dateStr;
            Integer res = mDateSizeCache.get(key);
            if (res == null) {
                res = computeFitTextSizeForDate(dateStr, wantedWidth);
                mDateSizeCache.put(key, res);
            }
            return res;
        }

        private int computeFitTextSizeForDate(String dateStr, int wantedWidth) {
            int step = 4;
            int measure = (int) mDatePaint.measureText(dateStr);
            if (measure == wantedWidth) {
                // Perfect fit: do nothing
            } else if (measure < wantedWidth) {
                // Narrower: try a bigger font
                do {
                    mDatePaint.setTextSize(mDatePaint.getTextSize() + step);
                } while ((measure = (int) mDatePaint.measureText(dateStr)) < wantedWidth);
                if (measure > wantedWidth) {
                    // We went too far
                    mDatePaint.setTextSize(mDatePaint.getTextSize() - step);
                }
            } else {
                // Wider: try a smaller font
                do {
                    mDatePaint.setTextSize(mDatePaint.getTextSize() - step);
                } while (mDatePaint.measureText(dateStr) > wantedWidth);
            }
            return (int) mDatePaint.getTextSize();
        }

        private void adjustSecondsAndAmPmSizes() {
            float size = mHourMinutesNormalPaint.getTextSize();
            mSecondsPaint.setTextSize(size * SECONDS_SIZE_FACTOR);
            mAmPmPaint.setTextSize(size * AM_PM_SIZE_FACTOR);
        }

        // endregion
    }
}
