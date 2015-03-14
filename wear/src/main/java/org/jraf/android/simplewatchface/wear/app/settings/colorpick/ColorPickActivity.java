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
package org.jraf.android.simplewatchface.wear.app.settings.colorpick;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.view.ColorPickListener;
import org.jraf.android.simplewatchface.wear.view.ColorPickView;
import org.jraf.android.util.log.wrapper.Log;

public class ColorPickActivity extends Activity {
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_pick);
        ColorPickView colorPickView = (ColorPickView) findViewById(R.id.colorPick);
        colorPickView.setOldColor(0xFFa3f556);
        colorPickView.setListener(new ColorPickListener() {
            @Override
            public void onColorPicked(int pickedColor) {
                Log.d("pickedColor=" + Integer.toHexString(pickedColor));
            }

            @Override
            public void onOkPressed(int pickedColor) {
                Log.d("pickedColor=" + Integer.toHexString(pickedColor));
            }

            @Override
            public void onCancelPressed() {
                Log.d();
            }
        });
    }
}
