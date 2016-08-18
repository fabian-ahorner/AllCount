package com.bitflake.counter.services;

import android.os.PowerManager;

import com.bitflake.counter.algo.shared.old.SlidingWindow;
import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.algo.shared.old.SensorDataProvider;
import com.bitflake.counter.algo.shared.old.SensorValueListener;

public class SensorService extends BroadcastReceiverService implements SensorValueListener {
    protected SlidingWindow window = new SlidingWindow(3, 20);
    protected SensorDataProvider sensor;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
    }

    public void stopListening() {
        sensor.stopListening();
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
    }

    public void startListening() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Allcount");
        mWakeLock.acquire();
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
