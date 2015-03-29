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

import org.jraf.android.androidwearcolorpicker.app.ColorPickActivity;
import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.settings.presets.PresetPickActivity;
import org.jraf.android.simplewatchface.wear.presets.ColorPreset;
import org.jraf.android.util.log.wrapper.Log;

public class SettingsActivity extends Activity implements WearableListView.ClickListener {
    private static final int REQUEST_PICK_COLOR = 0;
    private static final int REQUEST_PICK_PRESET = 1;

    private int[] mColorsFromPreferences;
    private int mItemPosition;
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        WearableListView listView = (WearableListView) findViewById(R.id.list);
        mAdapter = new SettingsAdapter(this, getColorsFromPreferences());
        listView.setAdapter(mAdapter);

        // Set a click listener
        listView.setClickListener(this);
    }

    private int[] getColorsFromPreferences() {
        if (mColorsFromPreferences == null) {
            mColorsFromPreferences = new int[6];
            SettingsHelper preferenceHelper = SettingsHelper.get(this);
            // Special case: the first item is "presets"
            mColorsFromPreferences[0] = getResources().getColor(R.color.settings_color_presets);
            // Normal cases
            mColorsFromPreferences[1] = preferenceHelper.getColorBackground();
            mColorsFromPreferences[2] = preferenceHelper.getColorHourMinutes();
            mColorsFromPreferences[3] = preferenceHelper.getColorSeconds();
            mColorsFromPreferences[4] = preferenceHelper.getColorAmPm();
            mColorsFromPreferences[5] = preferenceHelper.getColorDate();
        }
        return mColorsFromPreferences;
    }

    private void saveColorToPreferences(int itemPostion, int pickedColor) {
        SettingsHelper preferenceHelper = SettingsHelper.get(this);

        switch (itemPostion) {
            case 1:
                preferenceHelper.setColorBackground(pickedColor);
                break;
            case 2:
                preferenceHelper.setColorHourMinutes(pickedColor);
                break;
            case 3:
                preferenceHelper.setColorSeconds(pickedColor);
                break;
            case 4:
                preferenceHelper.setColorAmPm(pickedColor);
                break;
            case 5:
                preferenceHelper.setColorDate(pickedColor);
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

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        SettingsAdapter.ItemViewHolder itemViewHolder = (SettingsAdapter.ItemViewHolder) viewHolder;
        mItemPosition = itemViewHolder.position;
        if (mItemPosition == 0) {
            // Special case: the first item is "presets"
            Intent intent = new Intent(this, PresetPickActivity.class);
            startActivityForResult(intent, REQUEST_PICK_PRESET);
        } else {
            int oldColor = getColorsFromPreferences()[itemViewHolder.position];
            Intent intent = new ColorPickActivity.IntentBuilder().oldColor(oldColor).build(this);
            startActivityForResult(intent, REQUEST_PICK_COLOR);
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
                mColorsFromPreferences = null;
                mAdapter.setColors(getColorsFromPreferences());
                break;

            case REQUEST_PICK_COLOR:
                if (resultCode == RESULT_CANCELED) {
                    // The user pressed 'Cancel'
                    break;
                }

                int pickedColor = ColorPickActivity.getPickedColor(data);
                Log.d("pickedColor=" + Integer.toHexString(pickedColor));

                saveColorToPreferences(mItemPosition, pickedColor);
                mColorsFromPreferences = null;
                mAdapter.setColors(getColorsFromPreferences());

                break;
        }
    }
}
