package com.bitflake.counter.algo.shared.current.count;

import com.bitflake.counter.algo.shared.current.CountState;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.List;

/**
 * Created by fahor on 22/09/2016.
 */
public class RotatedCounterVersion {
    private static final int MINIMAL_STATE = 1;
    private final CounterRotation rotation;
    private final List<CountState> states;
    private Vector3D[] stateValues;

    private double[] likelihoods;
    private double[] stateScores;
    private double[] stateDistances;
    private double[] stateScoresCumulated;
    private double[] likelihoodsTmp;
    private int mostLikelyState;
    private double bestStateDistance;

    public RotatedCounterVersion(CounterRotation rotation, List<CountState> states) {
        this.rotation = rotation;
        this.states = states;
        initArrays(states.size());
        initStateRotations(rotation.getRotation(), states);
        likelihoods[MINIMAL_STATE] = 1;
    }

    private void initArrays(int s) {
        likelihoods = new double[s];
        likelihoodsTmp = new double[s];
        stateDistances = new double[s];
        stateScores = new double[s];
        stateScoresCumulated = new double[s];
        stateValues = new Vector3D[s];
    }

    private void initStateRotations(Rotation r, List<CountState> states) {
        for (int i = 0; i < stateValues.length; i++) {
            stateValues[i] = r.applyInverseTo(new Vector3D(states.get(i).values));
        }
    }

    public void updateLikelihoods(Vector3D values) {
        updateStateScores(values);
        for (int i = 0; i < likelihoods.length; i++) {
            CountState state = states.get(i);
            int reach = state.isTransientState() ? 1 : 2;
            int from = i;
//            int from = Math.max(0, i - reach);
            int to = Math.min(i + reach, likelihoods.length - 1);
            updateLikelihood(likelihoods[i], from, to);
        }
        swapTmpLikelihoods();
    }

    private void updateStateScores(Vector3D values) {
        double cumulated = 0;
        for (int i = 0; i < likelihoodsTmp.length; i++) {
            stateDistances[i] = getStateDistance(values, i);
            stateScores[i] = getStateScore(stateDistances[i]);
            cumulated += stateScores[i];
            stateScoresCumulated[i] = cumulated;
        }
    }

    private void updateLikelihood(double likelihood, int from, int to) {
//        double scoreSum = stateScoresCumulated[to] - stateScoresCumulated[from] + stateScores[from];
//        for (int i = from; i <= to; i++) {
//            likelihoodsTmp[i] += stateScores[i] / scoreSum * likelihood;
//        }
        double bestScore = 0;
        int bestScoreIndex = -1;
        for (int i = from; i <= to; i++) {
            if (bestScore < stateScores[i]) {
                bestScore = stateScores[i];
                bestScoreIndex = i;
            }
        }
        likelihoodsTmp[bestScoreIndex] += likelihood;
    }

    private void swapTmpLikelihoods() {
        bestStateDistance = Double.MAX_VALUE;

        for (int i = 0; i < likelihoods.length; i++) {
            likelihoods[i] = likelihoodsTmp[i];
            likelihoodsTmp[i] = 0;
            if (stateDistances[i] < bestStateDistance) {
                mostLikelyState = i;
                bestStateDistance = stateDistances[i];
            }
        }
    }

    private double getStateScore(double distance) {
        return Math.exp(-CountSettings.MOVE_DRIVE * distance);
    }

    private double getStateDistance(Vector3D values, int state) {
        return stateValues[state].distance(values);
    }

    public double getLikelihood(int state) {
        return likelihoods[state];
    }

    public double getGolaLikelihood() {
        return likelihoods[likelihoods.length - 1];
    }

    public int getMostLikelyState() {
        return mostLikelyState;
    }

    public double getBestDistance() {
        return bestStateDistance;
    }

    public void reset() {
        likelihoods[0] = 1;
        for (int i = 1; i < likelihoods.length; i++) {
            likelihoods[i] = 0;
        }
    }
}
