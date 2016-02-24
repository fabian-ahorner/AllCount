package com.bitflake.allcount;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bitflake.counter.SensorCounter;
import com.bitflake.counter.SlidingWindow;
import com.bitflake.counter.StateExtractor;
import com.bitflake.counter.StateWindow;
import com.bitflake.counter.TextToSpeachActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends TextToSpeachActivity implements SensorEventListener, SensorCounter.CountListener {


    private static final int SENSOR_COUNT = 3;
    private static final int PARTICLE_COUNT = 100;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private LineChart chart;
    private ArrayList<LineDataSet> chartLines = new ArrayList<>();
    private LineData chartData;
    private boolean isRecording = false;
    private Button bStartRecording;
    private TextView tLog;
    private SlidingWindow slidingWindow;
    private StateExtractor stateExtractor = new StateExtractor();
    private List<StateWindow> states;
    private Button bCount;
    private boolean isCounting;
    private int readCount;
    private SensorCounter counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        tLog = (TextView) findViewById(R.id.tLog);

        chartData = new LineData();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        chart = (LineChart) findViewById(R.id.chart1);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getXAxis().setDrawLabels(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawBorders(true);
        chart.setDrawGridBackground(false);

        // create a data object with the datasets
        chart.setData(chartData);
        slidingWindow = new SlidingWindow(3, 10);
        counter = new SensorCounter();
        counter.setCountListener(this);
    }

    protected void onPause() {
        super.onPause();
        if (isRecording)
            stopRecording();
        if (isCounting)
            stopCounting();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if (isRecording || isCounting) {
                    recordData(event.values);
                }
        }
    }

    private void recordData(float[] values) {
        chartData.addXValue(Integer.toString(readCount));

        slidingWindow.addData(values);
        for (int i = 0; i < SENSOR_COUNT; i++) {
            chartData.addEntry(new Entry(values[i], readCount), i);
        }

        chart.notifyDataSetChanged();
        chart.invalidate();
        readCount++;
    }

    public void startRecording(View v) {
        bStartRecording = (Button) v;
        if (isRecording) {
            stopRecording();
        } else {
            startRecording();
        }
    }

    private void startRecording() {
        startListening();
        stateExtractor.clear();
        slidingWindow.setAnalyser(stateExtractor);
        this.isRecording = true;
        speak(getString(R.string.start_recording));
        bStartRecording.setText(R.string.stop_recording);
    }

    private void startListening() {
        slidingWindow.clear();
        readCount = 0;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        initCharts();
        readCount = 0;
        readCount = 0;
    }

    private void initCharts() {
        int[] lineColors = new int[]{
                R.color.colorAccent, R.color.colorAccent, R.color.colorAccent, R.color.colorAccent
        };
        chartData.clearValues();
        for (int i = 0; i < SENSOR_COUNT + 1; i++) {
            LineDataSet set = new LineDataSet(new ArrayList<Entry>(), "DataSet 1");
            chartLines.add(set);
            set.setColor(getResources().getColor(lineColors[i]));
            set.setLineWidth(1f);
            set.setDrawCircles(false);
            set.setValueTextSize(9f);
            chartData.addDataSet(set);
        }
        chartLines.get(SENSOR_COUNT).setLineWidth(2);
    }

    private void stopRecording() {
        this.isRecording = false;
        speak(getString(R.string.stop_recording));
        bStartRecording.setText(R.string.start_recording);
        stateExtractor.compressStates();
        stopListening();
        states = stateExtractor.getStates();
    }

    private void stopListening() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void addState(StateWindow stateWindow) {
    }

    public void startCounting(View view) {
        bCount = (Button) view;
        if (isCounting) {
            stopCounting();
        } else {
            startCounting();
        }
    }

    private void stopCounting() {
        stopListening();
        isCounting = false;
        bCount.setText(R.string.count);
    }

    private void startCounting() {
        Intent intent = new Intent(this, CountActivity.class);
        intent.putExtras(states.get(0).toBundles());
        startActivity(intent);
//        isCounting = true;
//        startListening();
//        counter.reset();
//        counter.setStates(stateExtractor.getStates());
//        slidingWindow.setAnalyser(counter);
//        bCount.setText(R.string.stop_counting);
//        tLog.setText(String.valueOf(0));
    }

    @Override
    public void onCount(int count) {
        speak(String.valueOf(count));
        tLog.setText(String.valueOf(count));
    }

    @Override
    public void onCountProgress(float progress) {

    }
}
