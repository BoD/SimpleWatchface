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
package org.jraf.android.simplewatchface.wear.settings;

import org.jraf.android.prefs.DefaultInt;
import org.jraf.android.prefs.DefaultString;
import org.jraf.android.prefs.Prefs;

@Prefs
public class Settings {
    @DefaultInt(0xFF002244)
    Integer colorBackground;

    @DefaultInt(0xFF99EEFF)
    Integer colorHourMinutes;

    @DefaultInt(0xFFAAFFFF)
    Integer colorSeconds;

    @DefaultInt(0xFF55DDFF)
    Integer colorAmPm;

    @DefaultInt(0xDD99EEFF)
    Integer colorDate;

    @DefaultString("Exo2-ExtraBoldItalic.ttf")
    String fontTime;

    @DefaultString("Exo2-Italic.ttf")
    String fontDate;

    @DefaultInt(44)
    Integer sizeTime;

    @DefaultInt(28)
    Integer sizeDate;
}
