package com.bitflake.counter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventExtractor implements SlidingWindow.WindowAnalyser {
    private static final int STILL_WINDOWS = 20;
    private final SlidingWindow.WindowAnalyser delegate;
    private final RecordingStatusListener listener;
    private List<CountState> states = new ArrayList<>();
    private boolean wasStill;
    private boolean hasMoved;
    private double[] maxOfMax = new double[3];
    private double[] minOfMin = new double[3];
    private double[] startMin;
    private double[] startMax;
    private double[] errors = new double[3];
    private boolean wasStartingPos;
    private int statesObserved;

    public EventExtractor(SlidingWindow.WindowAnalyser delegate, RecordingStatusListener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    public void clear() {
        states.clear();
        wasStill = false;
        hasMoved = false;
    }

    @Override
    public void analyseWindow(CountState state) {
        states.add(state);
        statesObserved++;
        float stillness = isStill();
        boolean isStill = stillness >= 1;
        if (wasStill || isStill) {
            if (!wasStill) {
                startMin = Arrays.copyOf(minOfMin, minOfMin.length);
                startMax = Arrays.copyOf(maxOfMax, maxOfMax.length);
                listener.onIsStill(1);
                listener.onStartRecording(startMin, startMax);
//                states.clear();
            }
            delegate.analyseWindow(state);
            listener.onNewState(state);
            wasStill = true;
            if (hasMoved && statesObserved > 10 && isStartingPos(state)) {
                wasStartingPos = true;
                if (isStill) {
                    states.clear();
                    listener.onFinishedRecording();
                } else {
                    listener.onIsStill(1 - stillness);
                }
            } else if (!hasMoved && !isStill) {
                hasMoved = true;
                statesObserved = 0;
            } else if (wasStartingPos) {
                listener.onIsStill(1);
                wasStartingPos = false;
            }
        } else {
            listener.onIsStill(stillness);
//            Log.d("my", "Stillness from " + (int) (stillness * 100));
        }
    }

    private boolean isStartingPos(CountState s) {
        boolean isStart = true;
        for (int sensor = 0; sensor < s.means.length; sensor++) {
            double m = s.means[sensor];

            double avg = (startMax[sensor] + startMin[sensor]) / 2;
            double maxMargin = avg - startMin[sensor];
            maxMargin *= 4;

            errors[sensor] = (m - avg) / maxMargin;
            isStart &= Math.abs(m - avg) < maxMargin;
        }
//        Log.d("my", Arrays.toString(errors) + (isStart ? "ooooooooooo" : "xxxxxxxxxx"));
        return isStart;
    }

    private float isStill() {
        boolean isStill = true;
        double[] minOfMax = new double[3];
        double[] maxOfMin = new double[3];
        boolean isInitialised = false;
        int i;
        for (i = 0; i < STILL_WINDOWS && isStill && states.size() - 1 - i >= 0; i++) {
            CountState s = states.get(states.size() - 1 - i);
            for (int sensor = 0; sensor < s.means.length && isStill; sensor++) {
                double min = s.means[sensor] - s.sd[sensor] * 4;
                double max = s.means[sensor] + s.sd[sensor] * 4;

                if (isInitialised) {
                    minOfMin[sensor] = Math.min(minOfMin[sensor], min);
                    maxOfMin[sensor] = Math.max(maxOfMin[sensor], min);
                    minOfMax[sensor] = Math.min(minOfMax[sensor], max);
                    maxOfMax[sensor] = Math.max(maxOfMax[sensor], max);
                    double overlap = minOfMax[sensor] - maxOfMin[sensor];
                    double maxDistance = maxOfMax[sensor] - minOfMin[sensor];
                    if (overlap < 0 || maxDistance > overlap * 30) {
                        isStill = false;
                    }
                } else {
                    minOfMin[sensor] = min;
                    maxOfMin[sensor] = min;
                    minOfMax[sensor] = max;
                    maxOfMax[sensor] = max;
                }
            }
            isInitialised = true;
        }
        while (states.size() > STILL_WINDOWS)
            states.remove(0);

//        String log = "";
//        for (int sensor = 0; sensor < maxOfMax.length; sensor++) {
//            double overlap = minOfMax[sensor] - maxOfMin[sensor];
//            double maxDistance = maxOfMax[sensor] - minOfMin[sensor];
//            log += String.format("%10.2f", maxDistance / overlap);
//        }
//        Log.d("my", log + "    " + (isStill ? "------------" : "+++++++"));
        return i / (float) STILL_WINDOWS;
//        return isStill;
    }

    public CountState getLastState() {
        return states.get(states.size() - 1);
    }

    public interface RecordingStatusListener {
        void onStartRecording(double[] startMin, double[] startMax);

        void onFinishedRecording();

        void onNewState(CountState state);

        void onIsStill(float stillness);
    }
}
