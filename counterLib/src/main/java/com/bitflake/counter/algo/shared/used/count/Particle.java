package com.bitflake.counter.algo.shared.used.count;


import com.bitflake.counter.algo.shared.used.CountSettings;
import com.bitflake.counter.algo.shared.used.CountState;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.Arrays;
import java.util.List;

public class Particle {
    private double[] stateDistances;
    private CountState state;
    private double likelihood = 0;
    private RealVector[] stateValues;
    private boolean[] isTransient;
    private List<CountState> states;
    private int currentState;
    private double[] stateScores;
    private int count = 0;

    public Particle(List<CountState> states) {
        this.state = states.get(0);
        this.states = states;
        this.isTransient = new boolean[states.size()];
        this.stateScores = new double[states.size()];
        this.stateDistances = new double[states.size()];
        this.stateValues = new RealVector[states.size()];
        for (int i = 0; i < this.stateValues.length; i++) {
            CountState s = states.get(i);
            isTransient[i] = s.isTransientState();
            stateValues[i] = new ArrayRealVector(s.means);
        }
        state.addParticle();
    }

    public void moveToNextCount() {
        count++;
        setState(0);
    }

    public void reduceCount() {
//        if (count > 0)
//            count--;
        count = 0;
    }

    public int getCount() {
        return count;
    }

    public void move(ArrayRealVector values) {
//        setState(state.pickNext().getId());
        setState(pickNextState(values));
    }

    public void setState(int state) {
        currentState = state;
        this.state.removeParticle();
        this.state = states.get(state);
        this.state.addParticle();
        this.likelihood = 0;
        double moveLikelihood = this.state.getLikelihoodInNeighbours(this.state);
        likelihood += Math.log(moveLikelihood);
        likelihood += Math.log(this.state.getLikelihoodOfDistance());
//        path.add(Arrays.toString(Thread.currentThread().getStackTrace()));
//        path.add(String.valueOf(cumulatedError));
    }

    public int pickNextState(RealVector value) {
//        stateScores[currentState] = getDistance(value, currentState);
        double total = 0;
        double best = 0;
        int bestId = -1;
        for (int i = 0; i < stateValues.length; i++) {
            stateDistances[i] = value.getDistance(stateValues[i]);
            stateScores[i] = getScore(stateDistances[i]);
            if (i >= currentState) {
                total += stateScores[i];
                if (bestId < 0 || best > stateDistances[i]) {
                    best = stateDistances[i];
                    bestId = i;
                }
            }
            if (i > currentState && !isTransient[i])
                break;
        }
//        return bestId;
        double rand = Math.random() * total;
        double cur = 0;

        for (int i = currentState; i < stateValues.length; i++) {
            cur += stateScores[i];
            if (cur >= rand)
                return i;
        }
        return -1;
    }

    private double getScore(double distance) {
        return Math.exp(-CountSettings.MOVE_DRIVE / 3 * distance);
    }

    public void setInitials(RealVector[] stateValues) {
        this.stateValues = stateValues;
        setState(0);
    }

    public double getDistance() {
        return stateDistances[currentState];
    }

    public double getDistance(int index) {
        return stateDistances[index];
    }

    public CountState getState() {
        return state;
    }

    public void learnFrom(Particle teacher) {
        this.setState(teacher.currentState);
        this.likelihood = teacher.likelihood;
        this.stateScores = Arrays.copyOf(teacher.stateScores, teacher.stateScores.length);
        this.stateValues = teacher.stateValues;
        this.count = teacher.count;
//        this.path.clear();
    }

    public String toString() {
        return String.valueOf(state.getId()) + " " + getDistance();
    }

    public double getLikelihood() {
        return likelihood;
    }

    public boolean isValid() {
        double curDistance = stateDistances[currentState];
        for (int i = currentState - 1; i >= 0; i--) {
            if (curDistance > stateDistances[i])
                return false;
            if (!isTransient[i])
                return true;
        }
        return true;
    }


    public int getStateIndex() {
        return currentState;
    }
}
