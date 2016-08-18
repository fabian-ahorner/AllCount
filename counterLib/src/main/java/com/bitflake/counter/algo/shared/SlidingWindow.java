package com.bitflake.counter.algo.shared;

import com.bitflake.counter.algo.shared.old.CountState;

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
    private long time = -1;

    /**
     * @param sensorCount
     * @param analyseFrequency Per seconds
     */
    public SlidingWindow(int sensorCount, double analyseFrequency) {
        window = new double[sensorCount][INITIAL_WINDOW_SIZE];
        sums = new double[sensorCount];
        pos = new int[sensorCount];
        windowPosition = 0;
        lastRun = 0;
        windowDuration = (long) (1000000000 / analyseFrequency);
    }

    public void resetWindow() {
        for (int sensor = 0; sensor < sums.length; sensor++) {
            pos[sensor] = 0;
            sums[sensor] = 0;
        }
        lastRun = getCurTime();
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
            lastRun = getCurTime();
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
//            double[] var = used double[means.length];
//            double[] sd = used double[means.length];

            for (int sensor = 0; sensor < window.length; sensor++) {
                if (pos[sensor] > 0) {
                    means[sensor] = sums[sensor] / pos[sensor];
//                    for (int i = 0; i < pos[sensor]; i++) {
//                        var[sensor] += Math.pow(window[sensor][i] - means[sensor], 2);
//                    }
//                    var[sensor] /= pos[sensor];
//                    sd[sensor] = Math.sqrt(var[sensor]);
                }
            }
            analyser.analyseWindow(means);
        }
        resetWindow();
    }

    public long getElapsedTime() {
        return getCurTime() - lastRun;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getCurTime() {
        return time < 0 ? System.nanoTime() : time;
    }

    public interface WindowAnalyser {
        void analyseWindow(double[] means);
    }


    public interface CountWindowAnalyser extends WindowAnalyser {
        int getCount();

        void reset();
    }

    public void setAnalyser(WindowAnalyser analyser) {
        this.analyser = analyser;
    }
}
