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
package org.jraf.android.simplewatchface.wear.app.settings.colors;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WearableListView;

import org.jraf.android.androidwearcolorpicker.app.ColorPickActivity;
import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.app.settings.SettingsAdapter;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.util.log.Log;

public class ColorSettingsActivity extends Activity implements WearableListView.ClickListener {
    private static final int REQUEST_PICK_COLOR = 0;

    private int[] mColorsFromPreferences;
    private int mItemPosition;
    private SettingsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        WearableListView listView = (WearableListView) findViewById(R.id.list);
        mAdapter = new SettingsAdapter(this, getResources().getStringArray(R.array.settings_colors), getColorsFromPreferences());
        listView.setAdapter(mAdapter);

        // Set a click listener
        listView.setClickListener(this);
    }

    private int[] getColorsFromPreferences() {
        if (mColorsFromPreferences == null) {
            mColorsFromPreferences = new int[6];
            SettingsHelper settingsHelper= SettingsHelper.get(this);
            mColorsFromPreferences[0] = settingsHelper.getColorBackground();
            mColorsFromPreferences[1] = settingsHelper.getColorHourMinutes();
            mColorsFromPreferences[2] = settingsHelper.getColorSeconds();
            mColorsFromPreferences[3] = settingsHelper.getColorAmPm();
            mColorsFromPreferences[4] = settingsHelper.getColorDate();
        }
        return mColorsFromPreferences;
    }

    private void saveColorToPreferences(int itemPostion, int pickedColor) {
        SettingsHelper settingsHelper = SettingsHelper.get(this);

        switch (itemPostion) {
            case 0:
                settingsHelper.putColorBackground(pickedColor);
                // Also indicate that we want to use the background color (not the background picture)
                settingsHelper.setBackgroundPicture(null);
                break;
            case 1:
                settingsHelper.putColorHourMinutes(pickedColor);
                break;
            case 2:
                settingsHelper.putColorSeconds(pickedColor);
                break;
            case 3:
                settingsHelper.putColorAmPm(pickedColor);
                break;
            case 4:
                settingsHelper.putColorDate(pickedColor);
                break;
        }
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        SettingsAdapter.ItemViewHolder itemViewHolder = (SettingsAdapter.ItemViewHolder) viewHolder;
        mItemPosition = itemViewHolder.position;
        int oldColor = getColorsFromPreferences()[itemViewHolder.position];
        Intent intent = new ColorPickActivity.IntentBuilder().oldColor(oldColor).build(this);
        startActivityForResult(intent, REQUEST_PICK_COLOR);
    }

    @Override
    public void onTopEmptyRegionClick() {}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
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
