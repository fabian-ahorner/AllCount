package com.bitflake.counter.algo.shared.current.count;

public interface CountListener {
    void onCount(int count);

    void onCountProgress(float progress);
}
