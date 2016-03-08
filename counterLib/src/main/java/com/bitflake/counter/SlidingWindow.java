package com.bitflake.counter;

import java.util.Arrays;

public class SlidingWindow {
    private static final int INITIAL_WINDOW_SIZE = 10;
    double[][] window;
    double[] sums;
    private int windowPosition;
    private WindowAnalyser analyser;

    public SlidingWindow(int sensorCount, int windowDuration) {
        window = new double[INITIAL_WINDOW_SIZE][sensorCount];
        sums = new double[sensorCount];
        windowPosition = 0;
    }

    public void clear() {
        windowPosition = 0;
    }

    public void addData(float[] values) {
        if (windowPosition >= values.length) {
            double[][] newWindow = new double[window.length * 2][window[0].length];
            for (int i = 0; i < window.length; i++) {
                newWindow[i] = window[i];
            }
        }
        for (int i = 0; i < values.length; i++) {
            this.window[windowPosition][i] = values[i];
            sums[i] += values[i];
        }
        windowPosition++;
        if (windowPosition == this.window.length) {
            windowPosition = 0;
            analyseWindow();
        }
    }

    private void analyseWindow() {
        if (analyser != null) {
            double[] means = new double[sums.length];
            for (int sensor = 0; sensor < sums.length; sensor++) {
                means[sensor] = sums[sensor] / values.length;
            }
            double[] var = new double[means.length];
            for (int i = 0; i < windowPosition; i++) {
                for (int sensor = 0; sensor < sums.length; sensor++) {
                    var[sensor] += Math.pow(values[i][sensor] - means[sensor], 2);
                }
            }
            double[] sd = new double[means.length];
            for (int sensor = 0; sensor < sums.length; sensor++) {
                var[sensor] /= values.length;
                sd[sensor] = Math.sqrt(var[sensor]);
                sums[sensor] = 0;
            }
            analyser.analyseWindow(new StateWindow(means, sd));
        }
    }


    public interface WindowAnalyser {
        void analyseWindow(StateWindow state);
    }

    public void setAnalyser(WindowAnalyser analyser) {
        this.analyser = analyser;
    }
}
