package com.bitflake.counter.sensors;

public interface SensorValueListener {
    void onValueChanged(float[] values);
}
