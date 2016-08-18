package com.bitflake.counter.algo.shared.old;

import android.util.Log;

import java.util.ArrayList;

public class Particle {
    private static final double JITTER = 0.1;
    private double cumulatedError;
    private CountState state;
    ArrayList<String> path = new ArrayList<>();
    private double likelihood = 0;

    public Particle(CountState state) {
        this.state = state;
//        this.cumulatedError = 0;
        state.addParticle();
    }

    public Particle(CountState state, double cumulatedError) {
        this.state = state;
//        this.cumulatedError = cumulatedError;
        state.addParticle();
    }

    public void move() {
        likelihood = 0;
        state.removeParticle();
//        int lastId = state.getId();
//        CountState lastState = state;
        CountState newState;
        if (Math.random() < 0.9)
            newState = state.getPossibleNext();
        else
            newState = state;
        likelihood += Math.log(down(state.getLikelihoodInNeighbours(newState)));
//        likelihood += Math.log(state.getLikelihoodInSequenze());
//        if (state.getId() != 0)
        likelihood += Math.log(down(newState.getLikelihoodOfDistance()));

        newState.addParticle();
        cumulatedError += Math.pow(newState.getDistance(), 2);
        path.add(newState.toString());
        state = newState;
    }

    private double down(double val) {
        return val;//Math.max(0.01, Math.round(val * 10) / 10);
    }

    private void moveToNext() {
        state.removeParticle();
        this.state = state.getNext();
        state.addParticle();
    }

//    public double getSmoothedError() {
//        return smoothedError;
//    }

    public void setState(CountState state) {
        this.state.removeParticle();
        this.state = state;
        this.cumulatedError = Math.pow(state.getDistance(), 2);
        this.likelihood = 0;
        state.addParticle();
        path.clear();
        likelihood += Math.log(down(state.getLikelihoodInNeighbours(state)));
        likelihood += Math.log(down(state.getLikelihoodOfDistance()));
//        path.add(Arrays.toString(Thread.currentThread().getStackTrace()));
//        path.add(String.valueOf(cumulatedError));
    }

    public double getCumulatedError() {
        return cumulatedError;
    }

    public double getDistance() {
        return state.getDistance();
    }

    public CountState getState() {
        return state;
    }

    public void learnFrom(Particle teacher) {
        this.setState(teacher.getState());
        this.cumulatedError = teacher.cumulatedError;
        this.likelihood = teacher.likelihood;
//        this.path.clear();
        this.path.addAll(teacher.path);
    }

    public void setCumulatedError(double cumulatedError) {
//        this.cumulatedError = cumulatedError;
        this.path.add("Reset cummulated:" + cumulatedError);
        this.likelihood = cumulatedError;
    }

    public void printPath() {
        Log.d("my", path.toString());
    }

    public String toString() {
        return String.format(("%8.3f   " + state.getId()), getCumulatedError());
    }

    public double getLikelihood() {
        return likelihood;
    }
}
