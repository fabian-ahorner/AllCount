package com.bitflake.counter;

import java.util.Arrays;

public class SlidingWindow {
    private static final int INITIAL_WINDOW_SIZE = 10;
    private final int[] pos;
    private final long windowDuration;
    double[][] window;
    double[] sums;
    private int windowPosition;
    private WindowAnalyser analyser;
    private long lastRun = 0;

    /**
     * @param sensorCount
     * @param analyseFrequency Per seconds
     */
    public SlidingWindow(int sensorCount, double analyseFrequency) {
        window = new double[sensorCount][INITIAL_WINDOW_SIZE];
        sums = new double[sensorCount];
        pos = new int[sensorCount];
        windowPosition = 0;
        lastRun = System.nanoTime();
        windowDuration = (long) (1000000000 / analyseFrequency);
    }

    public void resetWindow() {
        for (int sensor = 0; sensor < sums.length; sensor++) {
            pos[sensor] = 0;
            sums[sensor] = 0;
        }
        lastRun = System.nanoTime();
    }

    public void addValue(int sensor, double value) {
        analyseIfTimePassed();

        // Double buffer size
        ensureSpaceInWindow(sensor);
        window[sensor][pos[sensor]] = value;
        sums[sensor] += value;
        pos[sensor]++;
    }

    private void analyseIfTimePassed() {
        if (getElapsedTime() > windowDuration) {
            analyseWindow();
            lastRun = System.nanoTime();
        }
    }

    private void ensureSpaceInWindow(int sensor) {
        if (pos[sensor] >= window[sensor].length) {
            window[sensor] = Arrays.copyOf(window[sensor], window[sensor].length * 2);
        }
    }

    private void analyseWindow() {
        if (analyser != null) {
            double[] means = new double[window.length];
            double[] var = new double[means.length];
            double[] sd = new double[means.length];

            for (int sensor = 0; sensor < window.length; sensor++) {
                if (pos[sensor] > 0) {
                    means[sensor] = sums[sensor] / pos[sensor];
                    for (int i = 0; i < pos[sensor]; i++) {
                        var[sensor] += Math.pow(window[sensor][i] - means[sensor], 2);
                    }
                    var[sensor] /= pos[sensor];
                    sd[sensor] = Math.sqrt(var[sensor]);
                }
            }
            analyser.analyseWindow(new CountState(means, sd));
        }
        resetWindow();
    }

    public long getElapsedTime() {
        return System.nanoTime() - lastRun;
    }

    public interface WindowAnalyser {
        void analyseWindow(CountState state);
    }

    public void setAnalyser(WindowAnalyser analyser) {
        this.analyser = analyser;
    }
}
