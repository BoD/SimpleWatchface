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
import android.os.Handler;
import android.support.wearable.view.WatchViewStub;
import android.text.format.DateFormat;
import android.widget.TextView;

public class WatchfaceActivity extends Activity {
    private TextView mTxtTime;
    private TextView mTxtDate;
    private TextView mTxtAmPm;
    private TextView mTxtSeconds;

    private boolean mTimeTickReceiverRegistered;
    private java.text.DateFormat mTimeFormat;
    private java.text.DateFormat mDateFormat;
    private boolean mIs24HourFormat;
    private SimpleDateFormat mTimeAmPmFormat;
    private SimpleDateFormat mTimeSecondsFormat;

    private Handler mHandler = new Handler();
    private boolean mPaused;

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
                mTxtSeconds = (TextView) stub.findViewById(R.id.txtSeconds);

                if (mIs24HourFormat) {
                    mTxtTime.setTextSize(getResources().getInteger(R.integer.timeSize_withoutAmPm));
                } else {
                    mTxtTime.setTextSize(getResources().getInteger(R.integer.timeSize_withAmPm));
                }

//                Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Arista2.0 light.ttf");
//                mTxtTime.setTypeface(typeface);

                mTxtTime.setPivotX(0);
                mTxtTime.setPivotY(0);

                updateDisplay();

                registerTimeTick();

                mTxtTime.post(new Runnable() {
                    @Override
                    public void run() {
                        showSeconds();
                    }
                });
            }
        });
    }

    private void showSeconds() {
        mTxtTime.animate().scaleX(.9f).scaleY(.9f).setStartDelay(0);
        mTxtSeconds.animate().alpha(1).setStartDelay(250);
    }

    private void hideSeconds() {
        mTxtTime.animate().scaleX(1).scaleY(1).setStartDelay(250);
        mTxtSeconds.animate().alpha(0).setStartDelay(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTxtTime != null) {
            showSeconds();
            mHandler.post(mUpdateDisplayRunnable);
        }
        mPaused = false;
    }

    @Override
    protected void onPause() {
        if (mTxtTime != null) {
            hideSeconds();
        }
        mPaused = true;
        mHandler.removeCallbacks(mUpdateDisplayRunnable);
        super.onPause();
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
        mTxtAmPm.setText(getFormattedAmPm());
        mTxtSeconds.setText(getFormattedSeconds());
    }

    private Runnable mUpdateDisplayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTxtTime != null) {
                updateDisplay();
            }
            if (!mPaused) mHandler.postDelayed(mUpdateDisplayRunnable, 1000);
        }
    };

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

    private CharSequence getFormattedSeconds() {
        if (mTimeSecondsFormat == null) {
            mTimeSecondsFormat = new SimpleDateFormat("ss");
        }
        String formatted = mTimeSecondsFormat.format(new Date());
        return formatted;
    }

    private CharSequence getFormattedDate() {
        if (mDateFormat == null) {
            mDateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "dEEEMMM"));
        }
        return mDateFormat.format(new Date());
    }

    private String getFormattedAmPm() {
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
