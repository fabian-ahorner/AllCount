package com.bitflake.counter.algo.shared.current.count;


import com.bitflake.counter.algo.shared.current.CountState;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.List;

public class Particle {
    private CountState state = null;
    private double likelihood = 0;
    private boolean[] isTransient;
    private List<CountState> states;
    private int currentState;
    private double[] stateScores;
    private int count = 0;
    private CounterVersion counter;

    public Particle(List<CountState> states) {
        this.states = states;
        this.isTransient = new boolean[states.size()];
        this.stateScores = new double[states.size()];
        for (int i = 0; i < this.isTransient.length; i++) {
            CountState s = states.get(i);
            isTransient[i] = s.isTransientState();
        }
    }

    public void moveToNextCount() {
        count++;
        setState(counter, 0);
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
        setState(counter, pickNextState(values));
    }

    public void setState(CounterVersion counter, int state) {
        if (this.state != null)
            this.state.removeParticle(this.counter);
        this.counter = counter;
        currentState = state;
        this.state = states.get(state);
        this.state.addParticle(this.counter);
        this.likelihood = 0;
        double moveLikelihood = this.state.getLikelihoodInNeighbours(this.state);
        likelihood += Math.log(moveLikelihood);
        likelihood += Math.log(this.state.getLikelihoodOfDistance());
//        path.add(Arrays.toString(Thread.currentThread().getStackTrace()));
//        path.add(String.valueOf(cumulatedError));
    }

    public void disable() {
        this.state.removeParticle(counter);
        this.state = null;
    }

    public int pickNextState(RealVector value) {
//        stateScores[currentState] = getDistance(value, currentState);
        double forwardTotal = 0;
        double backTotal = 0;
        double best = 0;
        int bestId = -1;
        int bestFrowardI = -1;
        int bestBackwardI = -1;

        double bestBackward = Double.MAX_VALUE;
        double bestForward = Double.MAX_VALUE;
        int selFrom = isTransient[currentState] ? currentState - 1 : currentState - 2;
        selFrom = Math.max(0, selFrom);
        int selTo = isTransient[currentState] ? currentState + 1 : currentState + 2;
        for (int i = 0; i < stateScores.length; i++) {
            stateScores[i] = getScore(counter.stateDistances[i]);
            if (i < currentState)
                stateScores[i] *= CountSettings.BACK_PANANLTY;
            if (i >= selFrom && i <= selTo) {
                if (i > currentState) {
                    forwardTotal += stateScores[i];
                    if (counter.stateDistances[i] < bestForward) {
                        bestForward = counter.stateDistances[i];
                        bestFrowardI = i;
                    }
                } else if (i < currentState) {
                    backTotal += stateScores[i];
                    if (counter.stateDistances[i] < bestBackward) {
                        bestBackward = counter.stateDistances[i];
                        bestBackwardI = i;
                    }
                }
                if (bestId < 0 || best > counter.stateDistances[i]) {
                    best = counter.stateDistances[i];
                    bestId = i;
                }
            }
        }
        double total = stateScores[currentState] + forwardTotal;
        if (isFinalState() || bestBackward < counter.stateDistances[currentState] && counter.stateDistances[currentState] < bestForward) {
            total += backTotal;
            bestId = bestBackwardI;
        } else {
            selFrom = currentState;
            if (counter.stateDistances[currentState] < bestForward)
                bestId = currentState;
            else
                bestId = bestFrowardI;
        }
        if (CountSettings.MOVE_DRIVE <= 0)
            return bestId;
        double rand = Math.random() * total;
        double cur = 0;

        for (int i = selFrom; i < stateScores.length; i++) {
            cur += stateScores[i];
            if (cur >= rand)
                return i;
        }
        throw new InvalidParameterException("Invalid scores");
    }

    private double getScore(double distance) {
//        return Math.max(0.1, 30 - distance);
        return Math.exp(-CountSettings.MOVE_DRIVE * distance);
    }

    public double getDistance() {
//        if (counter == null || counter.stateDistances == null)
//            throw new NullPointerException();
        return counter.stateDistances[currentState];
    }

    public double getDistance(int index) {
        return counter.stateDistances[index];
    }

    public CountState getState() {
        return state;
    }

    public void learnFrom(Particle teacher) {
        this.likelihood = teacher.likelihood;
        this.stateScores = Arrays.copyOf(teacher.stateScores, teacher.stateScores.length);
        this.count = teacher.count;
        this.setState(teacher.counter, teacher.currentState);
    }

    public String toString() {
        return String.valueOf(currentState) + " " + getDistance();
    }

    public double getLikelihood() {
        return likelihood;
    }

    public boolean isValid() {
        double curDistance = counter.stateDistances[currentState];
        for (int i = currentState - 1; i >= 0; i--) {
            if (curDistance > counter.stateDistances[i])
                return false;
            if (!isTransient[i])
                return true;
        }
        return true;
    }

    public int getStateIndex() {
        return currentState;
    }

    public boolean isFinalState() {
        return currentState == states.size() - 1;
    }

    public boolean hasCounted() {
        return isFinalState() || count > 0;
    }

    public CounterVersion getCounterVersion() {
        return counter;
    }

    public void reset() {
        setState(counter, 0);
        count = 0;
    }
}
