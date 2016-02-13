package com.bitflake.counter;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

public class SensorActivity extends TextToSpeachActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private SlidingWindow slidingWindow;
    private boolean isListening;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        slidingWindow = new SlidingWindow(3, 10);
    }

    public void startListening() {
        isListening = true;
        slidingWindow.clear();
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void stopListening() {
        isListening = false;
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isListening)
            slidingWindow.addData(event.values);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isListening)
            stopListening();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setAnalyser(SlidingWindow.WindowAnalyser analyser) {
        slidingWindow.setAnalyser(analyser);
    }

    public boolean isListening() {
        return isListening;
    }

    public void onStartPress() {

    }

    public void onStopPress() {

    }
}
