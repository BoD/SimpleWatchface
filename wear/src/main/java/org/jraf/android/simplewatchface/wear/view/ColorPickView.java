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
package org.jraf.android.simplewatchface.wear.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.jraf.android.simplewatchface.R;

public class ColorPickView extends View {
    private static final int[] COLORS = new int[] {0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000};
    public static final float[] LIGHT_POSITIONS = new float[] {.5f, .75f, 1f};
    public static final float[] SATURATION_POSITIONS = new float[] {0f, .5f};

    // Color (hue) wheel
    private Paint mColorWheelPaint;
    private RectF mColorWheelRect = new RectF();
    private float mColorWheelRadius;
    private float mColorAngleRad;

    // Saturation arc
    private Paint mSaturationArcPaint;
    private int[] mSaturationColors = new int[2];
    private float mSaturationAngleRad;
    private RectF mSaturationLightRect = new RectF();
    private float mSaturationLightWheelRadius;

    // Light arc
    private Paint mLightArcPaint;
    private int[] mLightColors = new int[3];
    private float mLightAngleRad;

    // Ok half circle
    private Paint mOkHalfCirclePaint;
    private RectF mOkCancelRectangle = new RectF();
    private float mOkCancelRadius;

    // Cancel half circle
    private Paint mCancelHalfCirclePaint;

    // Indicator / separator
    private Paint mIndicatorPaint;

    private float mStrokeWidth;
    private int mSpacerPx;

    private int mTranslationOffset;
    private boolean mIsInColorWheel;
    private boolean mIsInSaturationArc;
    private boolean mIsInLightArc;
    private float[] mHsl = new float[3];
    private float[] mHsv = new float[3];

    public ColorPickView(Context context) {
        super(context);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ColorPickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mStrokeWidth = getResources().getDimensionPixelSize(R.dimen.color_pick_stroke_width);
        mSpacerPx = getResources().getDimensionPixelSize(R.dimen.color_pick_spacer);

        // Color
        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Shader shader = new SweepGradient(0, 0, COLORS, null);
        mColorWheelPaint.setShader(shader);
        mColorWheelPaint.setStyle(Paint.Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mStrokeWidth);

        // Saturation
        mSaturationArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSaturationArcPaint.setStyle(Paint.Style.STROKE);
        mSaturationArcPaint.setStrokeWidth(mStrokeWidth);
        mSaturationColors[1] = 0xFF808080; // grey

        // Light
        mLightArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLightArcPaint.setStyle(Paint.Style.STROKE);
        mLightArcPaint.setStrokeWidth(mStrokeWidth);
        mLightColors[0] = 0xFF000000; // black
        mLightColors[2] = 0xFFFFFFFF; // white

        // OK
        mOkHalfCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mOkHalfCirclePaint.setStyle(Paint.Style.FILL);

        // Indicator / separator
        mIndicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mIndicatorPaint.setStyle(Paint.Style.STROKE);
        mIndicatorPaint.setStrokeWidth(mSpacerPx);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.translate(mTranslationOffset, mTranslationOffset);

        int color = angleToColor(mColorAngleRad);

        // Color wheel
        canvas.drawOval(mColorWheelRect, mColorWheelPaint);

        // Saturation arc
        mSaturationColors[0] = color;
        Shader shader = new SweepGradient(0, 0, mSaturationColors, SATURATION_POSITIONS);
        mSaturationArcPaint.setShader(shader);
        canvas.drawArc(mSaturationLightRect, 0, 180, false, mSaturationArcPaint);

        // Light arc
        mLightColors[1] = color;
        shader = new SweepGradient(0, 0, mLightColors, LIGHT_POSITIONS);
        mLightArcPaint.setShader(shader);
        canvas.drawArc(mSaturationLightRect, 180, 180, false, mLightArcPaint);

        // OK half circle
        float hue = (float) Math.toDegrees(mColorAngleRad); // rad to deg
        if (hue < 0) hue = hue + 360;
        hue = 360 - hue; // invert
        mHsl[0] = hue;

        double saturationTurn = mSaturationAngleRad / (2 * Math.PI); // rad to turn
        saturationTurn = saturationTurn * 2; // circle to half circle
        saturationTurn = 1 - saturationTurn; // invert direction
        mHsl[1] = (float) saturationTurn;

        double lightTurn = mLightAngleRad / (2 * Math.PI); // rad to turn
        if (lightTurn < 0) lightTurn += 1;
        lightTurn = (lightTurn - .5) * 2; // circle to half circle
        mHsl[2] = (float) lightTurn;

        hslToHsv(mHsl, mHsv);
        mOkHalfCirclePaint.setColor(Color.HSVToColor(mHsv));

        canvas.drawArc(mOkCancelRectangle, 180, 180, false, mOkHalfCirclePaint);

        // Separator
        mIndicatorPaint.setColor(0xFF000000); // black
        canvas.drawLine(-mTranslationOffset + mStrokeWidth, 0, mTranslationOffset - mStrokeWidth, 0, mIndicatorPaint);

        // Color indicator
        int invertColor = Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
        mIndicatorPaint.setColor(invertColor);
        double cos = Math.cos(mColorAngleRad);
        double sin = Math.sin(mColorAngleRad);
        int startX = (int) ((mTranslationOffset - mStrokeWidth / 2) * cos);
        int startY = (int) ((mTranslationOffset - mStrokeWidth / 2) * sin);
        canvas.drawCircle(startX, startY, mStrokeWidth / 3, mIndicatorPaint);

        // Saturation indicator
        cos = Math.cos(mSaturationAngleRad);
        sin = Math.sin(mSaturationAngleRad);
        startX = (int) ((mTranslationOffset - mStrokeWidth / 2 - mStrokeWidth - mSpacerPx) * cos);
        startY = (int) ((mTranslationOffset - mStrokeWidth / 2 - mStrokeWidth - mSpacerPx) * sin);
        canvas.drawCircle(startX, startY, mStrokeWidth / 3, mIndicatorPaint);

        // Light indicator
        cos = Math.cos(mLightAngleRad);
        sin = Math.sin(mLightAngleRad);
        startX = (int) ((mTranslationOffset - mStrokeWidth / 2 - mStrokeWidth - mSpacerPx) * cos);
        startY = (int) ((mTranslationOffset - mStrokeWidth / 2 - mStrokeWidth - mSpacerPx) * sin);
        canvas.drawCircle(startX, startY, mStrokeWidth / 3, mIndicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        mTranslationOffset = min / 2;

        mColorWheelRadius = (min - mStrokeWidth) / 2;
        mColorWheelRect.set(-mColorWheelRadius, -mColorWheelRadius, mColorWheelRadius, mColorWheelRadius);

        mSaturationLightWheelRadius = mColorWheelRadius - mSpacerPx - mStrokeWidth;
        mSaturationLightRect.set(-mSaturationLightWheelRadius, -mSaturationLightWheelRadius, mSaturationLightWheelRadius, mSaturationLightWheelRadius);

        mOkCancelRadius = mSaturationLightWheelRadius - mSpacerPx - mStrokeWidth / 2;
        mOkCancelRectangle.set(-mOkCancelRadius, -mOkCancelRadius, mOkCancelRadius, mOkCancelRadius);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);

        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;

        float angle = (float) Math.atan2(y, x);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                double distanceFromCenter = Math.sqrt(x * x + y * y);

                if (distanceFromCenter >= mColorWheelRadius - mStrokeWidth / 2 && distanceFromCenter < mColorWheelRadius + mStrokeWidth / 2) {
                    // The point is in the color wheel
                    mIsInColorWheel = true;
                    mColorAngleRad = angle;
                    invalidate();
                } else if (distanceFromCenter >= mSaturationLightWheelRadius - mStrokeWidth / 2 &&
                        distanceFromCenter < mSaturationLightWheelRadius + mStrokeWidth / 2) {
                    // The point is in the saturation / light wheel
                    if (y >= 0) {
                        // The point is in the saturation arc
                        mIsInSaturationArc = true;
                        mSaturationAngleRad = angle;
                    } else {
                        // The point is in the light arc
                        mIsInLightArc = true;
                        mLightAngleRad = angle;
                    }
                    invalidate();
                } else {
                    // If user did not press pointer or center, report event not handled
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (mIsInColorWheel) {
                    mColorAngleRad = angle;
                    invalidate();
                } else if (mIsInSaturationArc) {
                    if (y < 0) {
                        // Jump outside the saturation arc
                        return false;
                    }
                    mSaturationAngleRad = angle;
                    invalidate();
                } else if (mIsInLightArc) {
                    if (y >= 0) {
                        // Jump outside the light arc
                        return false;
                    }
                    mLightAngleRad = angle;
                    invalidate();
                } else {
                    // The point was nowhere interesting
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                break;

            case MotionEvent.ACTION_UP:
                mIsInColorWheel = false;
                mIsInLightArc = false;
                mIsInSaturationArc = false;
                invalidate();
                break;
        }
        return true;
    }

    /**
     * Calculate the color using the supplied angle.
     *
     * @param angle The selected color's position expressed as angle (in rad).
     * @return The ARGB value of the color on the color wheel at the specified angle.
     */
    private int angleToColor(float angle) {
        // This is the "scale" between 0 and 1 (in other words, a number of turns of the circle)
        float scale = (float) (angle / (2 * Math.PI));
        if (scale < 0) scale += 1;

        if (scale <= 0) {
            return COLORS[0];
        }
        if (scale >= 1) {
            return COLORS[COLORS.length - 1];
        }

        // Get the value of the scale in relation to the colors
        float span = scale * (COLORS.length - 1);
        // Index of the nearest color
        int colorIndex = (int) span;
        // Factor between 0 and 1 to calculate how close of colorA or colorB we are
        float factor = span - colorIndex;

        int colorA = COLORS[colorIndex];
        int colorB = COLORS[colorIndex + 1];
        int r = average(Color.red(colorA), Color.red(colorB), factor);
        int g = average(Color.green(colorA), Color.green(colorB), factor);
        int b = average(Color.blue(colorA), Color.blue(colorB), factor);

        return Color.rgb(r, g, b);
    }

    private int average(int a, int b, float factor) {
        return a + Math.round(factor * (b - a));
    }

    /**
     * Convert an HSL color to HSV.<br/>
     * Source:
     * - http://en.wikipedia.org/wiki/HSL_and_HSV and
     * - http://ariya.blogspot.com.ar/2008/07/converting-between-hsl-and-hsv.html
     *
     * @param inHsl  HSL color to convert.
     * @param outHsv converted HSV color.
     */
    private static void hslToHsv(float[] inHsl, float[] outHsv) {
        outHsv[0] = inHsl[0];

        float s = inHsl[1];
        float l = inHsl[2] * 2;
        if (l <= 1) {
            s *= l;
        } else {
            s *= 2 - l;
        }

        outHsv[1] = (2 * s) / (l + s);
        outHsv[2] = (l + s) / 2;
    }

    /**
     * Convert an HSV color to HSL.<br/>
     * Source:
     * - http://en.wikipedia.org/wiki/HSL_and_HSV and
     * - http://ariya.blogspot.com.ar/2008/07/converting-between-hsl-and-hsv.html
     *
     * @param inHsv  HSv color to convert.
     * @param outHsl converted HSL color.
     */
    private static void hsvToHsl(float[] inHsv, float[] outHsl) {
        outHsl[0] = inHsv[0];

        float l = (2 - inHsv[1]) * inHsv[2];
        float s = inHsv[1] * inHsv[2];
        if (l <= 1) {
            s /= l;
        } else {
            s /= 2 - l;
        }

        outHsl[1] = s;
        outHsl[2] = l / 2;
    }

    public void setOldColor(int oldColor) {
        float[] hsv = new float[3];
        float[] hsl = new float[3];
        Color.colorToHSV(oldColor, hsv);
        hsvToHsl(hsv, hsl);
        mColorAngleRad = (float) Math.toRadians(-hsl[0]);
        mSaturationAngleRad = (float) (2 * Math.PI * (1 - hsl[1]) / 2);
        mLightAngleRad = (float) (2 * Math.PI * (.5 + hsl[2] / 2));
        invalidate();
    }
}
