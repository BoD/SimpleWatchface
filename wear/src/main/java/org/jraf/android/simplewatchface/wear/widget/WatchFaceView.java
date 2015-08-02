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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.watchface.SimpleWatchFaceService;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;

public class WatchFaceView extends View {
    private Time mTime = new Time();
    private boolean mIs24HourFormat;
    private SimpleDateFormat mDateFormat;

    private int mMarginBorders;
    private int mMarginDate;
    private int mMarginSeconds;

    private Paint mHourMinutesPaint;
    private Paint mSecondsPaint;
    private Paint mDatePaint;
    private Paint mAmPmPaint;

    private boolean mIsRound;
    private Handler mHandler = new Handler();

    public WatchFaceView(Context context) {
        super(context);
        init(context);
    }

    public WatchFaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public WatchFaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public WatchFaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        // Margins
        mMarginBorders = getResources().getDimensionPixelOffset(R.dimen.wf_margin_borders);
        mMarginSeconds = getResources().getDimensionPixelOffset(R.dimen.wf_margin_seconds);
        mMarginDate = getResources().getDimensionPixelOffset(R.dimen.wf_margin_date);

        mIs24HourFormat = DateFormat.is24HourFormat(context);

        // Paints
        mHourMinutesPaint = new Paint();
        mHourMinutesPaint.setAntiAlias(true);

        mSecondsPaint = new Paint();
        mSecondsPaint.setAntiAlias(true);

        mAmPmPaint = new Paint();
        mAmPmPaint.setAntiAlias(true);

        mDatePaint = new Paint();
        mDatePaint.setAntiAlias(true);

        // Sizes
        SettingsHelper settingsHelper = SettingsHelper.get(getContext());
        setTimeSize(settingsHelper.getSizeTime());
        setDateSize(settingsHelper.getSizeDate());

        // Shadows
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

    public void setHourMinutesColor(int color) {
        mHourMinutesPaint.setColor(color);
    }

    public void setSecondsColor(int color) {
        mSecondsPaint.setColor(color);
    }

    public void setAmPmColor(int color) {
        mAmPmPaint.setColor(color);
    }

    public void setDateColor(int color) {
        mDatePaint.setColor(color);
    }

    public void setTimeTypeface(Typeface typeface) {
        mHourMinutesPaint.setTypeface(typeface);
        mSecondsPaint.setTypeface(typeface);
        mAmPmPaint.setTypeface(typeface);
    }

    public void setDateTypeface(Typeface typeface) {
        mDatePaint.setTypeface(typeface);
    }

    public void setTimeSize(int size) {
        float hourMinutesSize = SimpleWatchFaceService.getPixelSizeFromSpSize(getContext(), size);
        mHourMinutesPaint.setTextSize(hourMinutesSize);
        mSecondsPaint.setTextSize(hourMinutesSize * SimpleWatchFaceService.SECONDS_SIZE_FACTOR);
        mAmPmPaint.setTextSize(hourMinutesSize * SimpleWatchFaceService.AM_PM_SIZE_FACTOR);
    }

    public void setDateSize(int size) {
        mDatePaint.setTextSize(SimpleWatchFaceService.getPixelSizeFromSpSize(getContext(), size));
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
        return mDateFormat.format(new Date());
    }

    // endregion

    @Override
    protected void onDraw(Canvas canvas) {
        mTime.setToNow();
        String hourMinutesStr = getHourMinutes();
        String secondsStr = getSeconds();
        String dateStr = getDate();
        String amPmStr = mIs24HourFormat ? null : getAmPm();
        SimpleWatchFaceService
                .drawText(canvas, hourMinutesStr, secondsStr, dateStr, amPmStr, mHourMinutesPaint, mSecondsPaint, mDatePaint, mAmPmPaint,
                        mIsRound, mMarginBorders, mMarginDate, mMarginSeconds);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mHandler.postDelayed(mInvalidateRunnable, 1000);
    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mInvalidateRunnable);
        super.onDetachedFromWindow();
    }

    private Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            if (isAttachedToWindow()) {
                mHandler.postDelayed(mInvalidateRunnable, 1000);
            }
        }
    };

    public void setIsRound(boolean isRound) {
        mIsRound = isRound;
    }
}
