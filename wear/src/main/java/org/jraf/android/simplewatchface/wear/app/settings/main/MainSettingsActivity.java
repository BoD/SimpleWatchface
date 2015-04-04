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
package org.jraf.android.simplewatchface.wear.app.settings.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;
import android.widget.Toast;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.settings.SettingsAdapter;
import org.jraf.android.simplewatchface.wear.app.settings.colors.ColorSettingsActivity;
import org.jraf.android.simplewatchface.wear.app.settings.presets.PresetPickActivity;
import org.jraf.android.simplewatchface.wear.presets.ColorPreset;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;

public class MainSettingsActivity extends Activity implements WearableListView.ClickListener {
    private static final int REQUEST_PICK_PRESET = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        WearableListView listView = (WearableListView) findViewById(R.id.list);
        SettingsAdapter adapter = new SettingsAdapter(this, getResources().getStringArray(R.array.settings), null);
        listView.setAdapter(adapter);

        // Set a click listener
        listView.setClickListener(this);
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        SettingsAdapter.ItemViewHolder itemViewHolder = (SettingsAdapter.ItemViewHolder) viewHolder;
        switch (itemViewHolder.position) {
            case 0:
                // Color presets
                Intent intent = new Intent(this, PresetPickActivity.class);
                startActivityForResult(intent, REQUEST_PICK_PRESET);
                break;

            case 1:
                // Colors
                intent = new Intent(this, ColorSettingsActivity.class);
                startActivity(intent);
                break;

            case 2:
                // Reset background image
                SettingsHelper.get(this).setBackgroundPicture(null);
                Toast.makeText(this, R.string.settings_resetBackgroundImage_success, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onTopEmptyRegionClick() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_PICK_PRESET:
                if (resultCode == RESULT_CANCELED) {
                    // The user pressed 'Cancel'
                    break;
                }
                ColorPreset colorPreset = data.getParcelableExtra(PresetPickActivity.EXTRA_RESULT);
                saveColorPresetToPreferences(colorPreset);
                break;
        }
    }

    private void saveColorPresetToPreferences(ColorPreset colorPreset) {
        SettingsHelper preferenceHelper = SettingsHelper.get(this);
        preferenceHelper.setColorBackground(colorPreset.background);
        preferenceHelper.setColorHourMinutes(colorPreset.hourMinutes);
        preferenceHelper.setColorSeconds(colorPreset.seconds);
        preferenceHelper.setColorAmPm(colorPreset.amPm);
        preferenceHelper.setColorDate(colorPreset.date);
    }
}
