package com.bitflake.counter.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.bitflake.counter.SlidingWindow;

public class SensorService extends MessengerService implements SensorEventListener {
    private boolean isListening;
    private SlidingWindow window = new SlidingWindow(3, 10);
    private SensorManager mSensorManager;
    private Sensor mSensor;

    @Override
    public void onCreate() {
        super.onCreate();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void stopListening() {
        mSensorManager.unregisterListener(this);
        isListening = false;
    }

    public void startListening() {
        if (isListening)
            return;
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
        isListening = true;
    }

    public boolean isListening() {
        return isListening;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        window.addData(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void setAnalyser(SlidingWindow.WindowAnalyser analyser) {
        window.setAnalyser(analyser);
    }
}
