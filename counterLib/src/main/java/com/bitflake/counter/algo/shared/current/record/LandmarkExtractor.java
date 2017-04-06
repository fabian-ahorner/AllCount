package com.bitflake.counter.algo.shared.current.record;

import com.bitflake.counter.algo.shared.current.CountState;

import java.util.ArrayList;
import java.util.List;

public class LandmarkExtractor {
    private final List<CountState> originals;
    private final List<CountState> landmarks;
    private final double minDistance;

    public LandmarkExtractor(List<CountState> originals) {
        this.originals = originals;
        this.landmarks = new ArrayList<>();
        this.minDistance = getMaxStartDistance(originals) * 0.3;
    }

    private static double getMaxStartDistance(List<CountState> states) {
        if (states.size() == 0)
            return 0;
        CountState firstState = states.get(0);
        double distance = 0;
        for (int i = 1; i < states.size(); i++) {
            distance = Math.max(distance, firstState.getDistance(states.get(i).values));
        }
        return distance;
    }

    public void extractLandmarks() {
        landmarks.add(originals.get(0));
        addLandmarks(0, originals.size() - 1);
        landmarks.add(originals.get(originals.size() - 1));
    }

    public void addLandmarks(int from, int to) {
        double[] start = originals.get(from).values;
        double[] end = originals.get(to).values;
        double[] lastState = start;
        double totalGradient = 0;
        double[] interpolated = new double[start.length];
        double maxDistance = 0;
        int newLandmark = -1;

        for (int i = from + 1; i < to; i++) {
            CountState state = originals.get(i);

            // Find the state with the maximum distance to the interpolation of the start and the end
            double percentage = (i - from) / (double) (to - from);
            interpolate(interpolated, start, end, percentage);
            double distance = state.getDistance(interpolated);
            if (distance > maxDistance) {
                maxDistance = distance;
                newLandmark = i;
            }

            //Sum up the gradients of all states between the start and the end
            totalGradient += getGradient(lastState, state.values);
            lastState = state.values;
        }

        totalGradient += getGradient(lastState, end);
        double interpolatedGradient = getGradient(start, end);

        // Calculate how similar the gradient between the start
        // and end state is to the real gradient of all states in between
        double gradientSimilarity = Math.abs(totalGradient / interpolatedGradient - 1);

        if (maxDistance > minDistance && gradientSimilarity > 0.2) {
            addLandmarks(from, newLandmark);
            landmarks.add(originals.get(newLandmark));
            addLandmarks(newLandmark, to);
        }
    }

    private static double getGradient(double[] last, double[] current) {
        double gradient = 0;
        for (int j = 0; j < current.length; j++) {
            gradient += Math.abs(current[j] - last[j]);
        }
        return gradient;
    }

    private static void interpolate(double[] result, double[] start, double[] end, double percentage) {
        for (int j = 0; j < start.length; j++) {
            result[j] = start[j] + (end[j] - start[j]) * percentage;
        }
    }

    public List<CountState> getLandmarks() {
        return landmarks;
    }

    public static List<CountState> getLandmarks(List<CountState> states) {
        LandmarkExtractor extractor = new LandmarkExtractor(states);
        extractor.extractLandmarks();
        return extractor.getLandmarks();
    }
}