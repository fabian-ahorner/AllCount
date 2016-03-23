package com.bitflake.counter.services;

import com.bitflake.counter.SlidingWindow;
import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.sensors.SensorDataProvider;
import com.bitflake.counter.sensors.SensorValueListener;
import com.bitflake.counter.sensors.WearAccelerationSensor;

public class SensorService extends BroadcastReceiverService implements SensorValueListener {
    protected SlidingWindow window = new SlidingWindow(3, 10);
    private SensorDataProvider sensor;

    @Override
    public void onCreate() {
        super.onCreate();
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
    }

    public void stopListening() {
        sensor.stopListening();
    }

    public void startListening() {
        sensor.startListening();
    }

    public boolean isListening() {
        return sensor.isListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensor.destroy();
    }

    public void setAnalyser(SlidingWindow.WindowAnalyser analyser) {
        window.setAnalyser(analyser);
    }

    @Override
    public void onValueChanged(float[] values) {
        for (int sensor = 0; sensor < values.length; sensor++) {
            window.addValue(sensor, values[sensor]);
        }
    }
}
