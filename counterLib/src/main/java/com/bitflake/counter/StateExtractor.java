package com.bitflake.counter;

import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class StateExtractor implements SlidingWindow.WindowAnalyser {
    public static final int STILL_SIZE = 20;
    /**
     * The most similar states are stored first
     */
    private static Comparator<CountState> mergeComparator = new Comparator<CountState>() {
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
        if (states.size() > STILL_SIZE) {
            double[] minOfMax = new double[3];
            double[] maxOfMax = new double[3];
            double[] minOfMin = new double[3];
            double[] maxOfMin = new double[3];
            boolean isStill = true;
            boolean isInitialised = false;
            for (int iS = states.size() - STILL_SIZE; iS < states.size() && isStill; iS++) {
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


    public void compressStates() {
        states = compressStates(states);
    }

    public static List<CountState> compressStates(List<CountState> states) {
        return compressStates(states, false, Integer.MAX_VALUE);
    }

    /**
     * Merges similar neighbouring states
     */
    public static List<CountState> compressStates(List<CountState> states, boolean fake, int depth) {
//        double minDistance = 0.2;
        double minDistance = getMaxStartDistance(states) / 10;
        Log.d("my", "minDistance=" + minDistance);
//        double minDistance = getStillBoundary(states);//computeSimilarityBoundary(states);
//        double minDistance = 0.1;//computeSimilarityBoundary(states);
        List<CountState> newStates = getCompressedStates(states, 0, states.size() - 1, minDistance * 3, depth);
        if (states.size() == 0)
            return newStates;
        newStates.add(0, states.get(0));
        newStates.add(newStates.size(), states.get(states.size() - 1));
        CountState last = newStates.get(0);
        for (int i = 1; i < newStates.size(); i++) {
            CountState current = newStates.get(i);
//            current.setId(i);

            last.setNext(current);
            last = current;
        }

        minDistance = computeSimilarityBoundary(newStates);
        int i = 0;
        int id = 0;
        last = newStates.get(i++);
        while (i < newStates.size()) {
            CountState next = newStates.get(i);
            double distance = last.getDistance(next);
            if (distance > minDistance) {
                if (fake) {
                    last.setNext(next);
                } else {
                    last.setId(id++);
                    CountState transientState = new CountState(last, next, id++);
                    last.setNext(transientState);
                    newStates.add(i++, transientState);
                }
                last = next;
                i++;
            } else {
                newStates.remove(i);
            }
        }

        last.setNext(null);
        if (!fake)
            last.setId(id);
        Log.d("my", "Compressed:" + newStates.size() + "/" + states.size());
        return newStates;
    }

    private static double getMaxStartDistance(List<CountState> states) {
        if (states.size() == 0)
            return 0;
        CountState firstState = states.get(0);
        double distance = 0;
        for (int i = 1; i < states.size(); i++) {
            distance = Math.max(distance, firstState.getDistance(states.get(i)));
        }
        return distance;
    }

    public static List<CountState> getCompressedStates(List<CountState> states, int from, int to, double minDistance, int depth) {
        ArrayList<CountState> compressedStates = new ArrayList<>();
        if (depth <= 0)
            return compressedStates;
        double maxDistance = 0;
        int landmark = -1;
        CountState sFrom = states.get(from);
        CountState sTo = states.get(to);

        CountState lastState = sFrom;
        double gradient = 0;
        double interpolatedGradient = 0;
        CountState interpolated = new CountState(new double[sFrom.means.length], new double[sFrom.means.length]);
        for (int i = from + 1; i < to; i++) {
            CountState state = states.get(i);
            double percentage = (i - from) / (double) (to - from);
            for (int j = 0; j < sFrom.means.length; j++) {
                interpolated.means[j] = sFrom.means[j] + (sTo.means[j] - sFrom.means[j]) * percentage;
//                interpolated.sd[j] = sFrom.sd[j] + (sTo.sd[j] - sFrom.sd[j]) * percentage;
                gradient += Math.abs(state.means[j] - lastState.means[j]);
//                gradient += Math.abs(state.sd[j] - lastState.sd[j])/2;
            }
            lastState = state;
            double distance = interpolated.getDistance(state);
            if (distance > maxDistance) {
                maxDistance = distance;
                landmark = i;
            }
        }
        for (int j = 0; j < sFrom.means.length; j++) {
            gradient += Math.abs(sTo.means[j] - lastState.means[j]);
//            gradient += Math.abs(sTo.sd[j] - lastState.sd[j])/2;
            interpolatedGradient += Math.abs(sTo.means[j] - sFrom.means[j]);
//            interpolatedGradient += Math.abs(sTo.sd[j] - sFrom.sd[j])/2;
        }
//        double gradientSimilarity = 2 * gradient * interpolatedGradient / (gradient * gradient + interpolatedGradient * interpolatedGradient);
        double gradientSimilarity = gradient / interpolatedGradient;
        double maxSimilarity = 1.2;
        if ((gradientSimilarity > maxSimilarity || gradientSimilarity < 1 / maxSimilarity) && maxDistance > minDistance && landmark != -1) {
            compressedStates.addAll(getCompressedStates(states, from, landmark, minDistance, depth - 1));
            compressedStates.add(states.get(landmark));
            compressedStates.addAll(getCompressedStates(states, landmark, to, minDistance, depth - 1));
        }
        return compressedStates;
    }


    private static void mergeStates(CountState state, CountState toMerge) {
        int originalDistance = toMerge.getId() - state.getId();
        for (int i = 0; i < state.means.length; i++) {
            state.means[i] = (state.means[i] * originalDistance + toMerge.means[i]) / (originalDistance + 1);
            state.sd[i] = (state.sd[i] * originalDistance + toMerge.sd[i]) / (originalDistance + 1);
        }
        state.setNext(toMerge.getNext());
    }

    /**
     * Returns mean neighbour similarity + standard deviation
     *
     * @return
     */
    private static double computeSimilarityBoundary(List<CountState> states) {
        double distSum = 0;
        for (CountState s :
                states) {
            if (s.hasNext())
                distSum += s.getDistanceToNext();
        }
        double distMean = distSum / states.size();
        double sd = 0;
        for (CountState s :
                states) {
            if (s.getNext() != null)
                sd += Math.pow(s.getDistanceToNext() - distMean, 2);
        }
        return distMean - Math.sqrt(sd / states.size()) / 3;
    }

    public static double getStillBoundary(List<CountState> states) {
        double maxDistance = 0;
        for (int i = states.size() - 1; i >= 0 && i > states.size() - STILL_SIZE / 2; i--) {
            CountState state = states.get(i);
            for (int j = i + 1; j < states.size(); j++) {
                maxDistance = Math.max(maxDistance, states.get(j).getDistance(state));
            }
        }
        return maxDistance;
//        return states.get(0).getDistance(states.get(states.size() - 1));
    }

    public List<CountState> getStates() {
        return states;
    }
}
