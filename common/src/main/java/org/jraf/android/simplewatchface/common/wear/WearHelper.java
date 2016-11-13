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
package org.jraf.android.simplewatchface.common.wear;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import org.jraf.android.util.io.IoUtil;
import org.jraf.android.util.log.wrapper.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Helper singleton class to deal with the wear APIs.<br/>
 * Note: {@link #connect(Context)} must be called prior to calling all the other methods.
 */
public class WearHelper {
    private static final WearHelper INSTANCE = new WearHelper();

    private static final String PATH_SETTINGS = "/settings";

    private static final int MAX_BITMAP_DIMEN = 400;

    public static final String EXTRA_BACKGROUND_PICTURE = "EXTRA_BACKGROUND_PICTURE";


    private Context mContext;
    private GoogleApiClient mGoogleApiClient;

    private WearHelper() {}

    public static WearHelper get() {
        return INSTANCE;
    }

    @WorkerThread
    public synchronized void connect(Context context) {
        Log.d();
        if (mGoogleApiClient != null) {
            Log.d("Already connected");
            return;
        }

        mContext = context.getApplicationContext();
        mGoogleApiClient = new GoogleApiClient.Builder(context).addApi(Wearable.API).build();
        // Blocking
        ConnectionResult connectionResult = mGoogleApiClient.blockingConnect();
        if (!connectionResult.isSuccess()) {
            // TODO handle failures
        }
    }

    public synchronized void disconnect() {
        Log.d();
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
    }

    @WorkerThread
    public void putSettings(@NonNull Bitmap backgroundPicture) {
        Log.d();
        // Create new value
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_SETTINGS);

        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putAsset(EXTRA_BACKGROUND_PICTURE, createAssetFromBitmap(backgroundPicture));

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).await();
    }

    @WorkerThread
    public void removeSettings() {
        Log.d();
        Wearable.DataApi.deleteDataItems(mGoogleApiClient, createUri(PATH_SETTINGS)).await();
    }


    /*
     * Misc.
     */

    private static Uri createUri(String path) {
        return new Uri.Builder().scheme("wear").path(path).build();
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        // First resize the bitmap to a reasonable size
        bitmap = createSmallerBitmap(bitmap, MAX_BITMAP_DIMEN, MAX_BITMAP_DIMEN);
        // Now convert it to an asset
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

    @WorkerThread
    public Bitmap loadBitmapFromAsset(Asset asset) {
        DataApi.GetFdForAssetResult fd = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await();
        InputStream inputStream = fd.getInputStream();
        return BitmapFactory.decodeStream(inputStream);
    }

    @WorkerThread
    public byte[] loadDataFromAsset(Asset asset) {
        DataApi.GetFdForAssetResult fd = Wearable.DataApi.getFdForAsset(mGoogleApiClient, asset).await();
        InputStream inputStream = fd.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            IoUtil.copy(inputStream, byteArrayOutputStream);
        } catch (IOException e) {
            // Should never happen
            Log.e("Could not read from asset", e);
            return null;
        }
        return byteArrayOutputStream.toByteArray();
    }

    private static Bitmap createSmallerBitmap(Bitmap src, int maxWidth, int maxHeight) {
        int srcWidth = src.getWidth();
        int srcHeight = src.getHeight();
        Log.d("srcWidth=" + srcWidth + " srcHeight=" + srcHeight + "maxWidth=" + maxWidth + " maxHeight=" + maxHeight);
        if (srcWidth <= maxWidth && srcHeight <= maxHeight) {
            // The source is already smaller than the wanted dimens, return it 'as is'
            return src;
        }
        int dstWidth;
        int dstHeight;
        if (srcWidth < srcHeight) {
            dstHeight = maxHeight;
            float ratio = srcWidth / (float) srcHeight;
            dstWidth = (int) (dstHeight * ratio);
        } else {
            dstWidth = maxWidth;
            float ratio = srcHeight / (float) srcWidth;
            dstHeight = (int) (dstWidth * ratio);
        }
        Log.d("dstWidth=" + dstWidth + " dstHeight=" + dstHeight);
        return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
    }
}
