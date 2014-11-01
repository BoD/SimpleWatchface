package org.jraf.android.watchface.simple;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateFormat;
import android.widget.TextView;

public class WatchfaceActivity extends Activity {
    private TextView mTxtTime;
    private TextView mTxtDate;
    private TextView mTxtAmPm;

    private boolean mTimeTickReceiverRegistered;
    private java.text.DateFormat mTimeFormat;
    private java.text.DateFormat mDateFormat;
    private boolean mIs24HourFormat;
    private SimpleDateFormat mTimeAmPmFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchface);
        mIs24HourFormat = DateFormat.is24HourFormat(this);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTxtTime = (TextView) stub.findViewById(R.id.txtTime);
                mTxtDate = (TextView) stub.findViewById(R.id.txtDate);
                mTxtAmPm = (TextView) stub.findViewById(R.id.txtAmPm);

                if (mIs24HourFormat) {
                    mTxtTime.setTextSize(getResources().getInteger(R.integer.timeSize_withoutAmPm));
                } else {
                    mTxtTime.setTextSize(getResources().getInteger(R.integer.timeSize_withAmPm));
                }

//                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Arista2.0 light.ttf");
//                mTxtTime.setTypeface(typeface);
                updateDisplay();

                registerTimeTick();
            }
        });
    }

    private void registerTimeTick() {
        registerReceiver(mTimeTickReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
        mTimeTickReceiverRegistered = true;
    }

    private BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateDisplay();
        }
    };

    private void updateDisplay() {
        mTxtTime.setText(getFormattedTime());
        mTxtDate.setText(getFormattedDate());
        mTxtAmPm.setText(getAmPm());
    }

    private CharSequence getFormattedTime() {
        if (mTimeFormat == null) {
            mTimeFormat = DateFormat.getTimeFormat(this);
        }
        String formatted = mTimeFormat.format(new Date());
        if (!mIs24HourFormat) {
            formatted = formatted.substring(0, formatted.length() - 3);
        }

//        SpannableString builder = new SpannableString(formatted);
//        builder.setSpan(new RelativeSizeSpan(.2f), formatted.indexOf("PM"), formatted.indexOf("PM") + "PM".length(), 0);
//        return builder;
        return formatted;
    }

    private CharSequence getFormattedDate() {
        if (mDateFormat == null) {
            mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
        }
        return mDateFormat.format(new Date());
    }

    private String getAmPm() {
        if (mIs24HourFormat) return null;
        if (mTimeAmPmFormat == null) {
            mTimeAmPmFormat = new SimpleDateFormat("a");
        }
        return mTimeAmPmFormat.format(new Date());
    }

    @Override
    protected void onDestroy() {
        if (mTimeTickReceiverRegistered) unregisterReceiver(mTimeTickReceiver);
        super.onDestroy();
    }
}
