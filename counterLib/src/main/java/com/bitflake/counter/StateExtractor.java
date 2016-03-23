package com.bitflake.counter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class StateExtractor implements SlidingWindow.WindowAnalyser {
//    private final MainActivity activity;
    /**
     * The most similar states are stored first
     */
    private Comparator<CountState> mergeComparator = new Comparator<CountState>() {
        @Override
        public int compare(CountState lhs, CountState rhs) {
            return Double.compare(lhs.getDistanceToNext(), rhs.getDistanceToNext());
        }
    };
    private PriorityQueue<CountState> mergeList = new PriorityQueue<>(20, mergeComparator);
    private List<CountState> states = new ArrayList<>();
    private CountState lastState;
    private double distSum;
    private int lastStateId;

    public StateExtractor() {
//        this.activity = activity;
    }

    public void clear() {
        states.clear();
        mergeList.clear();
        lastState = null;
        distSum = 0;
        lastStateId = 0;
    }

    @Override
    public void analyseWindow(CountState state) {
        state.setId(lastStateId++);
        states.add(state);

        checkIfStill();

        if (lastState != null) {
            lastState.setNext(state);
            mergeList.add(lastState);
            distSum += lastState.getDistanceToNext();
        }
        lastState = state;
    }

    private void checkIfStill() {
        if (states.size() > 20) {
            double[] minOfMax = new double[3];
            double[] maxOfMax = new double[3];
            double[] minOfMin = new double[3];
            double[] maxOfMin = new double[3];
            boolean isStill = true;
            boolean isInitialised = false;
            for (int iS = states.size() - 20; iS < states.size() && isStill; iS++) {
                CountState s = states.get(iS);
                for (int sensor = 0; sensor < s.means.length && isStill; sensor++) {
                    double min = s.means[sensor] - s.sd[sensor] * 2.5;
                    double max = s.means[sensor] + s.sd[sensor] * 2.5;

                    if (isInitialised) {
                        minOfMin[sensor] = Math.min(minOfMin[sensor], min);
                        maxOfMin[sensor] = Math.max(maxOfMin[sensor], min);
                        minOfMax[sensor] = Math.min(minOfMax[sensor], max);
                        maxOfMax[sensor] = Math.max(maxOfMax[sensor], max);
                        double overlap = minOfMax[sensor] - maxOfMin[sensor];
                        double maxDistance = maxOfMax[sensor] - minOfMin[sensor];
                        isStill &= overlap > 0 && maxDistance < overlap * 30;
                    } else {
                        minOfMin[sensor] = min;
                        maxOfMin[sensor] = min;
                        minOfMax[sensor] = max;
                        maxOfMax[sensor] = max;
                    }
                }
                isInitialised = true;
            }
            String log = "";
            for (int sensor = 0; sensor < maxOfMax.length; sensor++) {
                double overlap = minOfMax[sensor] - maxOfMin[sensor];
                double maxDistance = maxOfMax[sensor] - minOfMin[sensor];
                log += String.format("%10.2f", maxDistance / overlap);
            }
            Log.d("my", log + "    " + (isStill ? "------------" : "+++++++"));
            if (isStill)
                startRecording();
        }
    }

    private void startRecording() {
        Log.d("my", "--------------is still");
    }

    /**
     * Merges similar neighbouring states
     */
    public void compressStates() {
        double minDistance = computeSimilarityBoundary();
        int stateCount = states.size();
        while (mergeList.size() > 1 && mergeList.peek().getDistanceToNext() < minDistance) {
            CountState state = mergeList.poll();
            //Check if state was already merged
            CountState toMerge = state.getNext();
            state.setNext(toMerge.getNext());
            states.remove(toMerge);
            mergeList.remove(toMerge);
            if (state.getNext() != null)
                mergeList.add(state);
        }
        Log.d("bitflake", "Remaining states:" + states.size() + "/" + stateCount + "(Boundary=" + minDistance + ")");
        for (CountState s :
                states) {
            Log.d("bitflake", s.toString());
        }
    }

    /**
     * Returns mean neighbour similarity + standard deviation
     *
     * @return
     */
    private double computeSimilarityBoundary() {
        double distMean = distSum / mergeList.size();
        double sd = 0;
        for (CountState s :
                states) {
            if (s.getNext() != null)
                sd += Math.pow(s.getDistanceToNext() - distMean, 2);
        }
        return distMean + Math.sqrt(sd / mergeList.size());
    }

    public List<CountState> getStates() {
        return states;
    }
}
