package com.bitflake.counter.algo.shared.current.record;


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
    private double[] startingPosition = new double[3];
    private int finalStates;
    private double minOverlap;
    private boolean hasRecording;


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
        hasRecording = false;
    }

    @Override
    public void analyseValues(double[] means) {
        CountState state = new CountState(means);
        if (hasRecording)
            return;
        states.add(state);
        statesObserved++;
        float stillness = isStill();
        boolean isStill = stillness >= 1;
        if (isRecording || isStill) {
            if (!isRecording) {
                startMax = maxOfMax.getValues(startMax);
                startMin = minOfMin.getValues(startMin);
                startingPosition = means;
                if (listener != null) {
                    listener.onIsStill(1);
                    listener.onStartRecording(startingPosition);
                }
                isRecording = true;
//                elements.clear();
            }
            delegate.analyseValues(means);
            if (listener != null) listener.onNewState(state);
            boolean isStartingPos = isStartingPos(state);
            if (isStartingPos && wasNotStart && hasMoved) {
                finalStates++;
            } else if (finalStates != 0) {
                finalStates = 0;
            }
            float shownStillness = Math.min(stillness, finalStates / (float) STILL_WINDOWS);
            if (listener != null) listener.onIsStill(1 - shownStillness);
//            statesObserved = 0;

//            Log.d("my", String.format("Stillness: %5.3f Overlap: %5.3f wasNotStart: " + wasNotStart + " isStartingPos: " + isStartingPos + " hasMoved: " + hasMoved + " statesObserved: " + statesObserved, stillness, minOverlap));

            if (!isStartingPos) {
                wasNotStart = true;
            }
            if (isStill && finalStates > STILL_WINDOWS) {
                wasStartingPos = true;
                states.clear();
                hasRecording = true;
                if (listener != null) listener.onFinishedRecording();
            } else if (!hasMoved && !isStill) {
                hasMoved = true;
                statesObserved = 0;
            }
        } else {
            if (listener != null) listener.onIsStill(stillness);
//            Log.d("my", "Stillness from " + (int) (stillness * 100));
        }
    }

    private boolean isStartingPos(CountState s) {
        double d = 0;
        for (int sensor = 0; sensor < s.values.length; sensor++) {
            double m = s.values[sensor];
            d += Math.pow(startingPosition[sensor] - m, 2);
//            if (Math.abs(m - center) > .4 * width) {
//                return false;
//            }
        }
        return Math.sqrt(d) < 0.2;
//        return true;
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
//        while (elements.size() > STILL_WINDOWS)
//            elements.remove(0);
//
//        mean.clear();
//
//        int i;
//        for (i = elements.size() - 1; i >= 0; i--) {
//            mean.addValues(elements.get(i).values);
//        }
//        meanValues = mean.getValues(meanValues);
//        sd.setMean(meanValues);
//        for (i = elements.size() - 1; i >= 0; i--) {
//            CountState s = elements.get(i);
//            sd.addValues(s.values);
//        }
//        sdValues = sd.getValues(sdValues);
//        double maxDevi = 0;
//        for (i = elements.size() - 1; i >= 0; i--) {
//            CountState s = elements.get(i);
//            for (int sensor = 0; sensor < meanValues.length; sensor++) {
//                double deviation = Math.abs((s.values[sensor] - meanValues[sensor]) / sdValues[sensor]);
//                maxDevi = Math.max(maxDevi, deviation);
//                if (deviation > MAX_SD) {
//                    Log.d("my", String.format("SD: %8.2f %8.2f %10.2f", (elements.size() - i) / (float) STILL_WINDOWS, maxDevi, sdValues[sensor]));
//                    return (elements.size() - i) / (float) STILL_WINDOWS;
//                }
//            }
//        }
//        Log.d("my", String.format("SD: %8.2f %8.2f ", (elements.size() - i) / (float) STILL_WINDOWS, maxDevi));
//        return 0.5f * (elements.size() - i) / (float) STILL_WINDOWS;
    }

    public CountState getLastState() {
        return states.get(states.size() - 1);
    }

    public interface RecordingStatusListener {
        void onStartRecording(double[] startingPos);

        void onFinishedRecording();

        void onNewState(CountState state);

        void onIsStill(float stillness);
    }

    public boolean hasRecording() {
        return hasRecording;
    }
}
