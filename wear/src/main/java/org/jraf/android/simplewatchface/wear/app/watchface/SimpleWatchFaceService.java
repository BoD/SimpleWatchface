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
import java.util.Locale;

import android.content.Context;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.util.log.wrapper.Log;

public class SimpleWatchFaceService extends CanvasWatchFaceService {
    /**
     * How small seconds are compared to hour / minutes.
     */
    public static final float SECONDS_SIZE_FACTOR = .5f;

    /**
     * How small the am/pm indicator is compared to hour / minutes.
     */
    public static final float AM_PM_SIZE_FACTOR = .35f;

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
        private int mColorAmPmAmbient;
        private Bitmap mBackgroundPicture;

        private SettingsHelper mSettingsHelper;

        private Paint mBackgroundPaint;
        private Paint mHourMinutesPaint;
        private Paint mSecondsPaint;
        private Paint mAmPmPaint;
        private Paint mDatePaint;

        private Time mTime = new Time();
        private boolean mIs24HourFormat;
        private SimpleDateFormat mDateFormat;

        private int mCachedDateDay;
        private String mCachedDateStr;

        private int mMarginBorders;
        private int mMarginSeconds;
        private int mMarginDate;

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

            mHourMinutesPaint = new Paint();
            mSecondsPaint = new Paint();
            mAmPmPaint = new Paint();
            mDatePaint = new Paint();

            // Colors
            updateColors();
            updatePaints();

            // Margins
            mMarginBorders = getResources().getDimensionPixelOffset(R.dimen.wf_margin_borders);
            mMarginSeconds = getResources().getDimensionPixelOffset(R.dimen.wf_margin_seconds);
            mMarginDate = getResources().getDimensionPixelOffset(R.dimen.wf_margin_date);
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
            mColorAmPmAmbient = getResources().getColor(R.color.amPm_ambient);
        }

        private void updatePaints() {
            boolean ambientMode = isInAmbientMode();

            // Colors
            if (ambientMode) {
                // Ambient mode: we maximize contrast
                mBackgroundPaint.setColor(mColorBackgroundAmbient);
                mHourMinutesPaint.setColor(mColorTimeAmbient);
                mAmPmPaint.setColor(mColorAmPmAmbient);
                mDatePaint.setColor(mColorDateAmbient);
            } else {
                // Normal mode: colors!
                if (mBackgroundPicture == null) {
                    mBackgroundPaint.setColor(mColorBackgroundNormal);
                } else {
                    mBackgroundPaint.setColor(0xFFFFFFFF); // white
                }
                mHourMinutesPaint.setColor(mColorTimeNormal);
                mAmPmPaint.setColor(mColorAmPmNormal);
                mDatePaint.setColor(mColorDateNormal);
            }
            mSecondsPaint.setColor(mColorSecondsNormal);

            // Enable antialias for normal mode / disable it for ambient mode + low bit
            boolean antialias = !ambientMode || !mLowBitAmbient;
            mHourMinutesPaint.setAntiAlias(antialias);
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
            mHourMinutesPaint.setStyle(style);
            mSecondsPaint.setStyle(style);
            mAmPmPaint.setStyle(style);
            mDatePaint.setStyle(style);

            // Typefaces
            Typeface timeTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + mSettingsHelper.getFontTime());
            Typeface dateTypeface = Typeface.createFromAsset(getAssets(), "fonts/" + mSettingsHelper.getFontDate());
            mHourMinutesPaint.setTypeface(timeTypeface);
            mSecondsPaint.setTypeface(timeTypeface);
            mAmPmPaint.setTypeface(timeTypeface);
            mDatePaint.setTypeface(dateTypeface);

            // Sizes
            float hourMinutesTextSize = getPixelSizeFromSpSize(mService, mSettingsHelper.getSizeTime());
            mHourMinutesPaint.setTextSize(hourMinutesTextSize);
            mSecondsPaint.setTextSize(hourMinutesTextSize * SECONDS_SIZE_FACTOR);
            mAmPmPaint.setTextSize(hourMinutesTextSize * AM_PM_SIZE_FACTOR);

            float dateTextSize = getPixelSizeFromSpSize(mService, mSettingsHelper.getSizeDate());
            mDatePaint.setTextSize(dateTextSize);

            // Shadows
            if (ambientMode) {
                mHourMinutesPaint.setShadowLayer(0, 0, 0, 0);
                mSecondsPaint.setShadowLayer(0, 0, 0, 0);
                mAmPmPaint.setShadowLayer(0, 0, 0, 0);
                mDatePaint.setShadowLayer(0, 0, 0, 0);
            } else {
                int shadowColor = 0x80000000; // black
                int shadowRadiusBig = getResources().getDimensionPixelSize(R.dimen.wf_shadow_radius_big);
                int shadowDeltaBig = 0;
                int shadowRadiusSmall = getResources().getDimensionPixelSize(R.dimen.wf_shadow_radius_small);
                int shadowDeltaSmall = 0;
                mHourMinutesPaint.setShadowLayer(shadowRadiusBig, shadowDeltaBig, shadowDeltaBig, shadowColor);
                mSecondsPaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
                mAmPmPaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
                mDatePaint.setShadowLayer(shadowRadiusSmall, shadowDeltaSmall, shadowDeltaSmall, shadowColor);
            }
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

            if (isInAmbientMode()) {
                // Background
                canvas.drawColor(mColorBackgroundAmbient);

                onDrawAmbient(canvas);
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

                onDrawNormal(canvas);
            }
        }

        private void onDrawAmbient(Canvas canvas) {
            String hourMinutesStr = getHourMinutes();
            String secondsStr = null;
            String dateStr = getDate();
            String amPmStr = mIs24HourFormat ? null : getAmPm();
            drawText(canvas, hourMinutesStr, secondsStr, dateStr, amPmStr, mHourMinutesPaint, mSecondsPaint, mDatePaint,
                    mAmPmPaint, mIsRound, mMarginBorders, mMarginDate, mMarginSeconds);
        }

        private void onDrawNormal(Canvas canvas) {
            String hourMinutesStr = getHourMinutes();
            String secondsStr = getSeconds();
            String dateStr = getDate();
            String amPmStr = mIs24HourFormat ? null : getAmPm();
            drawText(canvas, hourMinutesStr, secondsStr, dateStr, amPmStr, mHourMinutesPaint, mSecondsPaint, mDatePaint,
                    mAmPmPaint, mIsRound, mMarginBorders, mMarginDate, mMarginSeconds);
        }


        //endregion
    }

    private static int getTopForWidth(int diameter, int width) {
        return (int) (diameter - Math.sqrt(diameter * diameter - width * width)) / 2;
    }

    public static void drawText(Canvas canvas, String hourMinutesStr, String secondsStr, String dateStr, String amPmStr,
                                Paint hourMinutesPaint, Paint secondsPaint, Paint datePaint, Paint amPmPaint, boolean isRound,
                                int marginBorders, int marginDate, int marginSeconds) {
        int canvasWidth = canvas.getWidth();

        // Measure hour / minutes
        Rect hourMinutesBounds = new Rect();
        hourMinutesPaint.getTextBounds(hourMinutesStr, 0, hourMinutesStr.length(), hourMinutesBounds);
        int hourMinutesWidth = hourMinutesBounds.width();
        int hourMinutesHeight = hourMinutesBounds.height();

        Rect secondsBounds = null;
        int secondsHeight = 0;
        if (secondsStr != null) {
            // Measure seconds
            secondsBounds = new Rect();
            // Use "00" as a fixed text that's wide
            secondsPaint.getTextBounds("00", 0, 2, secondsBounds);
            secondsHeight = secondsBounds.height();
        }

        Rect amPmBounds = null;
        if (amPmStr != null) {
            // Measure AM/PM
            amPmBounds = new Rect();
            amPmPaint.getTextBounds(amPmStr, 0, amPmStr.length(), amPmBounds);
        }

        // Measure date
        Rect dateBounds = new Rect();
        datePaint.getTextBounds(dateStr, 0, dateStr.length(), dateBounds);
        int dateWidth = dateBounds.width();

        // Compute coordinates
        int top;
        int hourMinutesX;
        int dateX;
        if (isRound) {
            // Top depends on width
            int hourMinutesTotalWidth = hourMinutesWidth + Math.max(secondsStr == null ? 0 : marginSeconds + secondsBounds.width(), amPmStr == null ? 0 :
                    marginSeconds + amPmBounds.width());
            int topForWidth = getTopForWidth(canvasWidth, hourMinutesTotalWidth);
            top = topForWidth + marginBorders;

            // Horizontally centered
            hourMinutesX = (canvasWidth - hourMinutesTotalWidth) / 2;
            dateX = (canvasWidth - dateWidth) / 2;
        } else {
            // Top is always the border margin
            top = marginBorders;

            // Left centered
            hourMinutesX = marginBorders;
            dateX = marginBorders;
        }
        int hourMinutesY = top;
        int dateY = hourMinutesY + hourMinutesHeight + marginDate;

        int secondsX = hourMinutesX + hourMinutesWidth + marginSeconds;
        int secondsY = hourMinutesY + hourMinutesHeight - secondsHeight;

        int amPmX = secondsX;
        int amPmY = hourMinutesY;

        // Draw hour / minutes
        drawText(canvas, hourMinutesStr, hourMinutesBounds, hourMinutesX, hourMinutesY, hourMinutesPaint);

        if (secondsStr != null) {
            // Draw seconds
            drawText(canvas, secondsStr, secondsBounds, secondsX, secondsY, secondsPaint);
        }

        if (amPmStr != null) {
            // Draw AM/PM
            drawText(canvas, amPmStr, amPmBounds, amPmX, amPmY, amPmPaint);
        }

        // Draw the date
        drawText(canvas, dateStr, dateBounds, dateX, dateY, datePaint);
    }

    private static Rect getTextOffsetBounds(Rect bounds) {
        Rect res = new Rect(bounds);
        res.offset(-bounds.left, -bounds.top);
        return res;
    }

    private static void debugRect(Canvas c, Rect rect) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x80FFFF00);
        c.drawRect(rect, paint);
    }

    private static void drawText(Canvas c, String text, Rect textBounds, int x, int y, Paint paint) {
        c.drawText(text, -textBounds.left + x, -textBounds.top + y, paint);
//        Rect textOffsetBounds = getTextOffsetBounds(textBounds);
//        textOffsetBounds.offset(x, y);
//        debugRect(c, textOffsetBounds);
    }

    public static float getPixelSizeFromSpSize(Context context, int spSize) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spSize, context.getResources().getDisplayMetrics());
    }
}
