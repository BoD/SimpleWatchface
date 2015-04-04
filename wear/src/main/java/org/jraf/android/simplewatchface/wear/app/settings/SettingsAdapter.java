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

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jraf.android.simplewatchface.R;
import org.jraf.android.simplewatchface.wear.widget.WearableListItemLayout;

public class SettingsAdapter extends WearableListView.Adapter {
    private String[] mLabels;
    private int[] mColors;
    private LayoutInflater mInflater;

    public SettingsAdapter(Context context, String[] labels, @Nullable int[] colors) {
        mInflater = LayoutInflater.from(context);
        mLabels = labels;
        mColors = colors;
    }

    public static class ItemViewHolder extends WearableListView.ViewHolder {
        public int position;

        public ItemViewHolder(View itemView) {
            super(itemView);
        }
    }

    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(mInflater.inflate(R.layout.settings_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder, int position) {
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        WearableListItemLayout view = (WearableListItemLayout) itemHolder.itemView;
        view.setLabel(mLabels[position]);
        if (mColors != null) view.setColorIndicator(mColors[position]);

        itemHolder.position = position;
    }

    @Override
    public int getItemCount() {
        return mLabels.length;
    }

    public void setColors(int[] colors) {
        mColors = colors;
        notifyDataSetChanged();
    }
}
