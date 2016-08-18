package com.bitflake.counter.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.bitflake.counter.algo.shared.old.SensorDataProvider;

public class LocalAccelerationSensor extends SensorDataProvider implements SensorEventListener {
    private final SensorManager sensorManager;
    private final Sensor sensor;
    private boolean isListening;

    public LocalAccelerationSensor(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public void start() {
        if (isListening)
            return;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        isListening = true;
    }

    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
        isListening = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        notifyValueChanged(event.values);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
