package com.bitflake.counter.algo.shared.current.tools;

public class MathHelper {
    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }
}
