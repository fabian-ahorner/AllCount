package com.bitflake.counter;

import android.util.Log;

import com.bitflake.counter.tools.Stats;

import java.util.ArrayList;
import java.util.Arrays;

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
        state.removeParticle();
        int lastId = state.getId();
        state = state.getPossibleNext();
//        likelihood *= state.getLikelihood(state);
//        if (state.getId() != lastId)
        likelihood += Math.log(state.getLikelihood(state));
//        if (state.getId() < lastId)
//            cumulatedError++;
//            likelihood += Math.log(0.1);
//        else
//            likelihood += Math.log(0.45);
//        if (state.getId() != 0)
        likelihood += Math.log(state.getLikelihood());

        state.addParticle();
        cumulatedError += Math.pow(state.getDistance(), 2);
        path.add(state.toString());
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
        this.cumulatedError = state.getDistance();
        this.likelihood = 0;
        state.addParticle();
        path.clear();
//        path.add(Arrays.toString(Thread.currentThread().getStackTrace()));
//        path.add(String.valueOf(cumulatedError));
    }

    public double getCumulatedError() {
        return likelihood;
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
