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
package org.jraf.android.simplewatchface.wear.presets;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.XmlRes;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ColorPreset implements Parcelable {
    public int background;
    public int hourMinutes;
    public int seconds;
    public int amPm;
    public int date;

    public static ColorPreset fromXml(Context context, @XmlRes int xmlResId) throws IOException, XmlPullParserException {
        ColorPreset res = new ColorPreset();
        XmlResourceParser xmlResourceParser = context.getResources().getXml(xmlResId);
        String name = null;
        int eventType;
        while ((eventType = xmlResourceParser.next()) != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String tagName = xmlResourceParser.getName();
                    if ("color".equals(tagName)) {
                        name = xmlResourceParser.getAttributeValue(null, "name");
                    }
                    break;

                case XmlPullParser.TEXT:
                    String colorStr = xmlResourceParser.getText().trim();
                    int color = Color.parseColor(colorStr);
                    switch (name) {
                        case "background":
                            res.background = color;
                            break;
                        case "hourMinutes":
                            res.hourMinutes = color;
                            break;
                        case "seconds":
                            res.seconds = color;
                            break;
                        case "amPm":
                            res.amPm = color;
                            break;
                        case "date":
                            res.date = color;
                            break;
                    }
            }
        }
        xmlResourceParser.close();

        return res;
    }


    /*
     * Parcelable implementation.
     */

    public ColorPreset() {}

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(background);
        dest.writeInt(hourMinutes);
        dest.writeInt(seconds);
        dest.writeInt(amPm);
        dest.writeInt(date);
    }

    private ColorPreset(Parcel in) {
        background = in.readInt();
        hourMinutes = in.readInt();
        seconds = in.readInt();
        amPm = in.readInt();
        date = in.readInt();
    }

    public static final Parcelable.Creator<ColorPreset> CREATOR = new Parcelable.Creator<ColorPreset>() {
        public ColorPreset createFromParcel(Parcel source) {return new ColorPreset(source);}

        public ColorPreset[] newArray(int size) {return new ColorPreset[size];}
    };
}
