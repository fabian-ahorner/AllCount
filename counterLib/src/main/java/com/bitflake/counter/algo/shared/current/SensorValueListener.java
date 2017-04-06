package com.bitflake.counter.algo.shared.current;

public interface SensorValueListener {
    void onValueChanged(int time, float[] values);
}
