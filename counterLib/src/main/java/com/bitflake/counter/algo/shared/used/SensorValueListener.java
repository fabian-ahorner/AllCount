package com.bitflake.counter.algo.shared.used;

public interface SensorValueListener {
    void onValueChanged(int time, float[] values);
}
