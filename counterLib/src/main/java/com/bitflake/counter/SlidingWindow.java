package com.bitflake.counter;

public class SlidingWindow {
    double[][] values;
    double[] sums;
    private int windowPosition;
    private WindowAnalyser analyser;

    public SlidingWindow(int sensorCount, int windowSize) {
        values = new double[windowSize][sensorCount];
        sums = new double[sensorCount];
        windowPosition = 0;
    }

    public void clear() {
        windowPosition = 0;
    }

    public void addData(float[] values) {
        for (int i = 0; i < values.length; i++) {
            this.values[windowPosition][i] = values[i];
        }
        for (int i = 0; i < values.length; i++) {
            sums[i] += values[i];
        }
        windowPosition++;
        if (windowPosition == this.values.length) {
            windowPosition = 0;
            analyseWindow(this.values, sums);
            for (int i = 0; i < values.length; i++) {
                sums[i] = 0;
            }
        }
    }

    private void analyseWindow(double[][] values, double[] sums) {
        if (analyser != null) {
            double[] means = new double[sums.length];
            for (int sensor = 0; sensor < sums.length; sensor++) {
                means[sensor] = sums[sensor] / values.length;
            }
            double[] var = new double[means.length];
            for (int i = 0; i < values.length; i++) {
                for (int sensor = 0; sensor < sums.length; sensor++) {
                    var[sensor] += Math.pow(values[i][sensor] - means[sensor], 2);
                }
            }
            double[] sd = new double[means.length];
            for (int sensor = 0; sensor < sums.length; sensor++) {
                var[sensor] /= values.length;
                sd[sensor] = Math.sqrt(var[sensor]);
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
