package com.whitespider.impact.ble.sensortag;

import android.bluetooth.BluetoothGattService;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.whitespider.impact.ble.common.GenericBluetoothProfile;
import com.whitespider.impact.history.CsvFileWriter;
import com.whitespider.impact.history.HistoryItemRecyclerViewAdapter;
import com.whitespider.impact.util.BrainGearSmsSender;
import com.whitespider.impact.util.CustomMarkerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HeadGearActivity extends DeviceActivity {
    private HeadGearSectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ArrayList<BluetoothGattService> mServiceList;
    private SampleChart liveStreamingChart;
    private SampleChart concussionChart;
    private ConcussionDetector concussionDetector;
    private ConcussionLedInidicator concussionLedInidicator;
    private CsvFileWriter csvFileWriter = new CsvFileWriter();
    private TextView concussionTimeTextView;
    private TextView concussionMagnitudeTextView;

    int counterForLiveChart;
    private BrainGearSmsSender mSmsSender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_head_gear);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mSectionsPagerAdapter = new HeadGearSectionsPagerAdapter(getFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        concussionDetector = new ConcussionDetector(this, prefs);
        mSmsSender = new BrainGearSmsSender(this, prefs);
    }

    @Override
    public void setSensorTagIoProfile(SensorTagIoProfile sensorTagIoProfile) {
        super.setSensorTagIoProfile(sensorTagIoProfile);
        concussionLedInidicator = new ConcussionLedInidicator(this, sensorTagIoProfile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_head_gear, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void observeAcceleration(MotionSensor p) {
        //if(counterForLiveChart++ % 10 == 0) {
            liveStreamingChart.observeAcceleration(p);
        //}
        byte concussionSeverity = concussionDetector.concussionSeverity(p);
        if(concussionSeverity != 0x00) {
            if(concussionChart != null) {
                concussionChart.startRecording(concussionDetector.getSamples());
                concussionChart.indicateSeverity(concussionSeverity);
                concussionLedInidicator.headGearLED(concussionSeverity);
                final String concussionEventTime = new SimpleDateFormat("hh:mm").format(new Date());
                concussionTimeTextView.setText(concussionEventTime);

                final Double totalAcceleration = ConcussionDetector.getTotalAcceleration(p.getReading());

                final String concussionEventSeverity = String.format("%.2f G", totalAcceleration);
                concussionMagnitudeTextView.setText(concussionEventSeverity);
                csvFileWriter.addConcussionEvent(p, concussionSeverity);

                mSmsSender.sendSms(concussionEventSeverity, concussionEventTime);
            }
        }
        if(concussionChart != null) {
            if (concussionChart.isRecording()) {
                concussionChart.observeAcceleration(p);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSmsSender.onDestroy();
    }

    protected void setBusy(boolean b) {
        //mDeviceView.setBusy(b);
    }

    public void enableService(final GenericBluetoothProfile p) {
        p.enableService();
    }

    public void createStreamingChart(View rootView) {
        LineChart chart = (LineChart) rootView.findViewById(R.id.line_chart);
        liveStreamingChart = new SampleChart(chart, this);
        liveStreamingChart.onCreate();
        liveStreamingChart.setBackgroundColor("#262626");
        liveStreamingChart.setGridColor(HistoryItemRecyclerViewAdapter.HEADGEAR_GRAY);

        rootView.findViewById(R.id.concussionTimeTextView).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.concussionMagnitudeTextView).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.concussionHighHit).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.errorImageView).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.scheduleImageView).setVisibility(View.INVISIBLE);
    }
    public void createConcussionChart(View rootView) {
        LineChart chart = (LineChart) rootView.findViewById(R.id.line_chart);
        concussionChart = new SampleChart(chart, this);
        CustomMarkerView mv = new CustomMarkerView(this, R.layout.chart_marker);
        concussionChart.setMarkerView(mv);
        concussionChart.onCreate();
        concussionChart.setBackgroundColor("#262626");

        concussionTimeTextView = (TextView)rootView.findViewById(R.id.concussionTimeTextView);
        concussionMagnitudeTextView = (TextView)rootView.findViewById(R.id.concussionMagnitudeTextView);
    }
}
