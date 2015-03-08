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
package org.jraf.android.simplewatchface.wear.app.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.settings.colorpick.ColorPickActivity;

public class SettingsActivity extends Activity implements WearableListView.ClickListener {
    private static final int REQUEST_PICK_COLOR = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        WearableListView listView = (WearableListView) findViewById(R.id.list);
        listView.setAdapter(new SettingsAdapter(this));

        // Set a click listener
        listView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Intent intent = new Intent(this, ColorPickActivity.class);
        startActivityForResult(intent, REQUEST_PICK_COLOR);
    }

    @Override
    public void onTopEmptyRegionClick() {

    }
}
