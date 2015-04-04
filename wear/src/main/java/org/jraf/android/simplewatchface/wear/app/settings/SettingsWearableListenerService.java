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

import android.net.Uri;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.WearableListenerService;

import org.jraf.android.simplewatchface.common.wear.WearHelper;
import org.jraf.android.simplewatchface.wear.settings.SettingsHelper;
import org.jraf.android.util.log.LogUtil;
import org.jraf.android.util.log.wrapper.Log;

public class SettingsWearableListenerService extends WearableListenerService {
    private static final int NOTIFICATION_ID = 0;
    private WearHelper mWearHelper = WearHelper.get();

    public SettingsWearableListenerService() {}

    @Override
    public void onPeerConnected(Node peer) {}

    @Override
    public void onPeerDisconnected(Node peer) {}

    @Override
    public void onMessageReceived(MessageEvent messageEvent) { }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d("count=" + dataEvents.getCount());
        // There should always be only one item, but we iterate to be safe
        for (DataEvent dataEvent : dataEvents) {
            DataItem dataItem = dataEvent.getDataItem();
            Uri uri = dataItem.getUri();
            Log.d("uri=" + uri);
            String path = uri.getPath();
            Log.d("path=" + path);
            int type = dataEvent.getType();
            Log.d("type=" + LogUtil.getConstantName(DataEvent.class, type, "TYPE_"));
            if (type == DataEvent.TYPE_DELETED) {
                SettingsHelper.get(this).setBackgroundPicture(null);
            } else {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap dataMap = dataMapItem.getDataMap();
                Asset backgroundPictureAsset = dataMap.getAsset(WearHelper.EXTRA_BACKGROUND_PICTURE);
                if (backgroundPictureAsset != null) {
                    // Blocking
                    mWearHelper.connect(this);
                    byte[] bitmapData = mWearHelper.loadDataFromAsset(backgroundPictureAsset);
                    SettingsHelper.get(this).setBackgroundPicture(bitmapData);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        mWearHelper.disconnect();
        super.onDestroy();
    }
}
