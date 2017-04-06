package com.bitflake.counter.algo.shared.old;

import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.tools.ArrayValueHelper;

import java.util.ArrayList;
import java.util.List;

public class EventExtractor implements SlidingWindow.WindowAnalyser {
    private static final int STILL_WINDOWS = 30;
    private final SlidingWindow.WindowAnalyser delegate;
    private final RecordingStatusListener listener;
    private List<CountState> states = new ArrayList<>();
    private boolean isRecording;
    private boolean hasMoved;
    private boolean wasNotStart;
    private ArrayValueHelper.Min minOfMax = new ArrayValueHelper.Min(new double[3]);
    private ArrayValueHelper.Min minOfMin = new ArrayValueHelper.Min(new double[3]);
    private ArrayValueHelper.Max maxOfMin = new ArrayValueHelper.Max(new double[3]);
    private ArrayValueHelper.Max maxOfMax = new ArrayValueHelper.Max(new double[3]);
    ArrayValueHelper.Min min = new ArrayValueHelper.Min(new double[3]);
    ArrayValueHelper.Max max = new ArrayValueHelper.Max(new double[3]);
    private boolean wasStartingPos;
    private int statesObserved;
    private double[] startMax = new double[3];
    private double[] startMin = new double[3];
    private int finalStates;
    private double minOverlap;


    public EventExtractor(SlidingWindow.WindowAnalyser delegate, RecordingStatusListener listener) {
        this.delegate = delegate;
        this.listener = listener;
        min.setBias(-0.2);
        max.setBias(+0.2);
    }

    public void clear() {
        states.clear();
        isRecording = false;
        hasMoved = false;
    }

    @Override
    public void analyseValues(double[] values) {
        CountState state = new CountState(values);
        states.add(state);
        statesObserved++;
        float stillness = isStill();
        boolean isStill = stillness >= 1;
        if (isRecording || isStill) {
            if (!isRecording) {
                startMax = maxOfMax.getValues(startMax);
                startMin = minOfMin.getValues(startMin);
                listener.onIsStill(1);
                listener.onStartRecording(startMax, startMin);
                isRecording = true;
//                states.clear();
            }
            delegate.analyseValues(values);
            listener.onNewState(state);
            boolean isStartingPos = isStartingPos(state);
            if (isStartingPos && wasNotStart && hasMoved) {
                finalStates++;
            } else if (finalStates != 0) {
                finalStates = 0;
            }
            float shownStillness = Math.min(stillness, finalStates / (float) STILL_WINDOWS);
            listener.onIsStill(1 - shownStillness);
//            statesObserved = 0;

            if (!isStartingPos) {
                wasNotStart = true;
            }
            if (isStill && finalStates > STILL_WINDOWS) {
                wasStartingPos = true;
                states.clear();
                listener.onFinishedRecording();
            } else if (!hasMoved && !isStill) {
                hasMoved = true;
                statesObserved = 0;
            }
        } else {
            listener.onIsStill(stillness);
//            Log.d("my", "Stillness from " + (int) (stillness * 100));
        }
    }

    private boolean isStartingPos(CountState s) {
        for (int sensor = 0; sensor < s.values.length; sensor++) {
            double m = s.values[sensor];
            double d = (startMax[sensor] - startMin[sensor]) / 2;
            double mean = startMax[sensor] - d;

            if (Math.abs(m - mean) / d > .4) {
                return false;
            }
        }
        return true;
    }

    private float isStill() {
        while (states.size() > STILL_WINDOWS)
            states.remove(0);

        min.clear();
        max.clear();
        minOfMax.clear();
        minOfMin.clear();
        maxOfMin.clear();
        maxOfMax.clear();

        double minWindowSize = Double.MAX_VALUE;

        minOverlap = Double.MAX_VALUE;
        for (int i = 0; i < states.size(); i++) {
            CountState state = states.get(states.size() - 1 - i);
            min.addValues(state.values);
            max.addValues(state.values);
            if ((i + 1) % 3 == 0) {
                minOfMax.addValues(max);
                maxOfMax.addValues(max);
                minOfMin.addValues(min);
                maxOfMin.addValues(min);

                for (int s = 0; s < 3; s++) {
                    double maxDistance = Math.max(0, maxOfMax.getValue(s) - minOfMin.getValue(s));
                    double minDistance = Math.max(0, minOfMax.getValue(s) - maxOfMin.getValue(s));
                    double overlap = minDistance / maxDistance;
                    minOverlap = Math.min(minOverlap, overlap);
                    if (overlap < 0.1) {
//                        Log.d("my", String.format("Overlap: %4.2f " + (overlap > 0.1), overlap));
                        return (i - 1f) / STILL_WINDOWS;
                    }
                }
                min.clear();
                max.clear();
            }
        }
//        Log.d("my", "Is still");
        return states.size() / (float) STILL_WINDOWS;
//        while (states.size() > STILL_WINDOWS)
//            states.remove(0);
//
//        mean.clear();
//
//        int i;
//        for (i = states.size() - 1; i >= 0; i--) {
//            mean.addValues(states.get(i).values);
//        }
//        meanValues = mean.getValues(meanValues);
//        sd.setMean(meanValues);
//        for (i = states.size() - 1; i >= 0; i--) {
//            CountState s = states.get(i);
//            sd.addValues(s.values);
//        }
//        sdValues = sd.getValues(sdValues);
//        double maxDevi = 0;
//        for (i = states.size() - 1; i >= 0; i--) {
//            CountState s = states.get(i);
//            for (int sensor = 0; sensor < meanValues.length; sensor++) {
//                double deviation = Math.abs((s.values[sensor] - meanValues[sensor]) / sdValues[sensor]);
//                maxDevi = Math.max(maxDevi, deviation);
//                if (deviation > MAX_SD) {
//                    Log.d("my", String.format("SD: %8.2f %8.2f %10.2f", (states.size() - i) / (float) STILL_WINDOWS, maxDevi, sdValues[sensor]));
//                    return (states.size() - i) / (float) STILL_WINDOWS;
//                }
//            }
//        }
//        Log.d("my", String.format("SD: %8.2f %8.2f ", (states.size() - i) / (float) STILL_WINDOWS, maxDevi));
//        return 0.5f * (states.size() - i) / (float) STILL_WINDOWS;
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
