package com.bitflake.counter.algo.shared.current.count;

import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.tools.VecHelper;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RotationCollection implements SlidingWindow.CountWindowAnalyser {
    private Vector3D[] stateValues;
    private List<CountState> states;
    private Vector3D[] flippedStartVersions;
    private List<CounterRotation> potentialRotations = new ArrayList<>();
    private List<RotatedCounterVersion> activeRotations = new ArrayList<>();
    private int count;
    private Vector3D currentValues;
    private CountListener countListener;
    private double bestStartAngle;

    public RotationCollection(List<CountState> states) {
        setStates(states);
    }

    public void setStates(List<CountState> states) {
        reset();
        stateValues = new Vector3D[states.size()];
        this.states = states;
        for (int i = 0; i < stateValues.length; i++) {
            stateValues[i] = new Vector3D(states.get(i).values);
        }
        flippedStartVersions = VecHelper.createFlippedVersions(stateValues[0]);
    }

    @Override
    public void analyseValues(double[] values) {
        currentValues = new Vector3D(values).normalize();
        updatePotentialRotations(currentValues);
        addPotentialRotation(currentValues);
        updateLikelihoods(currentValues);
        sortOutRotations();
        checkCount();
        if (countListener != null)
            countListener.onCountProgress(0);
    }

    private void updatePotentialRotations(Vector3D v) {
        Iterator<CounterRotation> it = potentialRotations.iterator();
        while (it.hasNext()) {
            CounterRotation rotation = it.next();
            double originalAngle = rotation.getOriginalAngle();
            double currentAngle = rotation.getCurrentAngle(v);
            if (currentAngle > originalAngle * CountSettings.MIN_FIRST_ANGLE) {
                rotation.rotateSecondState(v);
                double angle = Math.abs(rotation.getSecondRotation());
                if (angle < CountSettings.VALID_START_ANGLE || angle > Math.PI - CountSettings.VALID_START_ANGLE) {
                    activeRotations.add(new RotatedCounterVersion(rotation, states));
                }
                it.remove();
            }
        }
    }

    private void addPotentialRotation(Vector3D v) {
        if (isValidStartingPoint(v) && isNewStartingPoint(v)) {
            CounterRotation rotation = new CounterRotation(stateValues, v);
            potentialRotations.add(rotation);
        }
    }

    private boolean isValidStartingPoint(Vector3D v) {
        bestStartAngle = Double.MAX_VALUE;
        for (int i = 0; i < flippedStartVersions.length; i++) {
            double angle = Math.abs(Math.acos(v.dotProduct(flippedStartVersions[i])));
            bestStartAngle = Math.min(bestStartAngle, angle);
            if (angle < CountSettings.VALID_START_ANGLE) {
                return System.currentTimeMillis() > 0;
            }
        }
        return false;
    }

    private boolean isNewStartingPoint(Vector3D v) {
        for (CounterRotation pR : potentialRotations) {
            double d = pR.getFirstStateDistance(v);
            if (d < CountSettings.MIN_START_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    private void updateLikelihoods(Vector3D v) {
        for (RotatedCounterVersion version : activeRotations) {
            version.updateLikelihoods(v);
        }
    }

    private void sortOutRotations() {
        Iterator<RotatedCounterVersion> it = activeRotations.iterator();
        while (it.hasNext()) {
            RotatedCounterVersion version = it.next();
            double d = version.getBestDistance();
            if (d > CountSettings.SORT_OUT_DISTANCE) {
                it.remove();
            }
        }
    }

    private void checkCount() {
        RotatedCounterVersion bestVersion = null;
        double bestLikelihood = 0;
        for (RotatedCounterVersion version : activeRotations) {
            if (bestVersion == null || version.getGolaLikelihood() > bestLikelihood) {
                bestVersion = version;
                bestLikelihood = version.getGolaLikelihood();
            }
        }
        if (bestLikelihood > CountSettings.MIN_GOAL_LIKELIHOOD) {
            performCount(bestVersion);
        }
    }

    private void performCount(RotatedCounterVersion bestVersion) {
        bestVersion.reset();
        activeRotations.clear();
        potentialRotations.clear();
        count++;
        if (countListener != null)
            countListener.onCount(count);
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void reset() {
        potentialRotations.clear();
        activeRotations.clear();
        this.count = 0;
    }

    public Vector3D getCurrentValues() {
        return currentValues;
    }

    public double getCountProgress() {
        if (activeRotations.isEmpty())
            return 0;
        double progress = 0;
        for (int i = 0; i < states.size(); i++) {
            double v = i / (states.size() - 1.);
            for (RotatedCounterVersion version : activeRotations) {
                progress += v * version.getLikelihood(i);
            }
        }
        return progress / activeRotations.size();
    }


    //Debug methods


    public RotationCollection() {
    }

    public int getActiveRotations() {
        return activeRotations.size();
    }

    public boolean isValidStartingPoint() {
        return isValidStartingPoint(currentValues);
    }

    public boolean isNewStartingPoint() {
        return isNewStartingPoint(currentValues);
    }

    public void setCountListener(CountListener countListener) {
        this.countListener = countListener;
    }

    public float[] getParticleCounts(float[] particleCounts) {
        if (particleCounts == null || particleCounts.length != states.size())
            particleCounts = new float[states.size()];
        float scale = CountSettings.PARTICLE_COUNT / Math.max(1, activeRotations.size());
        for (int i = 0; i < particleCounts.length; i++) {
            particleCounts[i] = 0;
            for (RotatedCounterVersion activeRotation : activeRotations) {
                particleCounts[i] += (float) activeRotation.getLikelihood(i) * scale;
            }
        }
        return particleCounts;
    }

    public float[] getStateDistances(float[] stateScores) {
        if (stateScores == null || stateScores.length != states.size())
            stateScores = new float[states.size()];
        for (int i = 0; i < stateValues.length; i++) {
            stateScores[i] = (float) currentValues.distance(stateValues[i]);
        }
        return stateScores;
    }

    public double getBestStartAngle() {
        return bestStartAngle;
    }
}
