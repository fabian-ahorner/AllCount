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
    private Comparator<StateWindow> mergeComparator = new Comparator<StateWindow>() {
        @Override
        public int compare(StateWindow lhs, StateWindow rhs) {
            return Double.compare(lhs.getDistanceToNext(), rhs.getDistanceToNext());
        }
    };
    private PriorityQueue<StateWindow> mergeList = new PriorityQueue<>(20, mergeComparator);
    private List<StateWindow> states = new ArrayList<>();
    private StateWindow lastState;
    private double distSumm;
    private int lastStateId;

    public StateExtractor() {
//        this.activity = activity;
    }

    public void clear() {
        states.clear();
        mergeList.clear();
        lastState = null;
        distSumm = 0;
        lastStateId = 0;
    }

    @Override
    public void analyseWindow(StateWindow state) {
        state.setId(lastStateId++);
        states.add(state);
        if (lastState != null) {
            lastState.setNext(state);
            mergeList.add(lastState);
            distSumm += lastState.getDistanceToNext();
        }
        lastState = state;
//        this.activity.addState(state);
    }

    /**
     * Merges similar neighbouring states
     */
    public void compressStates() {
        double minDistance = computeSimilarityBoundary();
        int stateCount = states.size();
        while (mergeList.peek().getDistanceToNext() < minDistance && mergeList.size() > 1) {
            StateWindow state = mergeList.poll();
            //Check if state was already merged
            StateWindow toMerge = state.getNext();
            state.setNext(toMerge.getNext());
            states.remove(toMerge);
            mergeList.remove(toMerge);
            if (state.getNext() != null)
                mergeList.add(state);
        }
        Log.d("bitflake", "Remaining states:" + states.size() + "/" + stateCount + "(Boundary=" + minDistance + ")");
        for (StateWindow s :
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
        double distMean = distSumm / mergeList.size();
        double sd = 0;
        for (StateWindow s :
                states) {
            if (s.getNext() != null)
                sd += Math.pow(s.getDistanceToNext() - distMean, 2);
        }
        return distMean + Math.sqrt(sd / mergeList.size());
    }

    public List<StateWindow> getStates() {
        return states;
    }

//    public void extractFeatures() {
//        int sensorCount = dataManager.getSensorCount();
//        DataInputStream[] is = new DataInputStream[sensorCount];
//        double[][] values = new double[windowSize][sensorCount];
//        double[] sum = new double[sensorCount];
//        for (int i = 0; i < sensorCount; i++) {
//            is[i] = dataManager.openSensorInputStream(i);
//        }
//        try {
//            int w = 0;
//            while (is[0].available()>0) {
//                for (int i = 0; i < sensorCount; i++) {
//                    values[w][i] = is[i].readDouble();
//                    sum[i] += values[w][i];
//                }
//                w++;
//                if (w >= windowSize) {
//                    w = 0;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
