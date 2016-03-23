package com.bitflake.counter.tools;

public class Stats {
    public static double softmax(double s1, double s2) {
        return Math.exp(s1) / (Math.exp(s1) + Math.exp(s2));
    }
    public static double sigmoidal(double v) {
        return 1/(1+Math.exp(v));
    }
}
