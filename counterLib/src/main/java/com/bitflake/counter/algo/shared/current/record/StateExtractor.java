package com.bitflake.counter.algo.shared.current.record;

import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.count.CountSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StateExtractor {
    public static List<CountState> compressStates(List<CountState> states) {
        if (states.isEmpty())
            return new ArrayList<>();
        List<CountState> newStates = LandmarkExtractor.getLandmarks(states);
        removeSimilarStates(newStates);
        equalizeFirstAndFinalState(newStates);
        addTransientStates(newStates);
        return newStates;
    }

    private static void equalizeFirstAndFinalState(List<CountState> newStates) {
        CountState firstState = newStates.get(0);
        CountState finalState = newStates.get(newStates.size() - 1);
        for (int j = 0; j < finalState.values.length; j++) {
            firstState.values[j] = finalState.values[j] = (firstState.values[j] + finalState.values[j]) / 2;
        }
    }

    private static void removeSimilarStates(List<CountState> states) {
        double minDistance = computeSimilarityBoundary(states);
        Iterator<CountState> it = states.iterator();
        CountState last = it.next();
        while (it.hasNext()) {
            CountState next = it.next();
            if (last.getDistance(next) > minDistance) {
                last.setNext(next);
                last = next;
            } else {
                it.remove();
            }
        }
        last.setNext(null);
    }

    private static void addTransientStates(List<CountState> newStates) {
        int i = 0;
        int id = 0;
        CountState last = newStates.get(i++);
        while (i < newStates.size()) {
            CountState next = newStates.get(i);
            last.setId(id++);
            CountState l = last;
            for (int j = 0; j < CountSettings.TRANSIENT_STATES; j++) {
                double p = 1. / (1 + CountSettings.TRANSIENT_STATES) * (j + 1);
                CountState transientState = new CountState(last, next, id++, p);
                l.setNext(transientState);
                newStates.add(i++, transientState);
                l = transientState;
            }
            l.setNext(next);
            last = next;
            i++;
        }
        last.setId(id);
    }

    private static double computeSimilarityBoundary(List<CountState> states) {
        double distSum = 0;
        for (int i = 0; i < states.size() - 1; i++) {
            CountState current = states.get(i);
            current.setNext(states.get(i + 1));
            distSum += current.getDistanceToNext();
        }
        states.get(states.size() - 1).setNext(null);

        double distMean = distSum / states.size();
        double sd = 0;
        for (int i = 0; i < states.size() - 1; i++) {
            CountState current = states.get(i);
            sd += Math.pow(current.getDistanceToNext() - distMean, 2);
        }
        return distMean - Math.sqrt(sd / states.size()) / 3;
    }
}
