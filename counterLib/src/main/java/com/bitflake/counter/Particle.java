package com.bitflake.counter;

import android.util.Log;

import com.bitflake.counter.tools.Stats;

import java.util.ArrayList;
import java.util.Arrays;

public class Particle {
    private static final double JITTER = 0.1;
    private double cumulatedError;
    private CountState state;
    private double smoothedError;
    ArrayList<String> path = new ArrayList<>();
    private int backwardSteps;

    public Particle(CountState state) {
        this.state = state;
        this.cumulatedError = 0;
        state.addParticle();
    }

    public Particle(CountState state, double cumulatedError) {
        this.state = state;
        this.cumulatedError = cumulatedError;
        state.addParticle();
    }

    public void move() {
        state.removeParticle();
        int lastId = state.getId();
        state = state.getPossibleNext();
        if (state.getId() < lastId)
            cumulatedError++;
//        state = state.getPossibleNext();

        state.addParticle();
        smoothedError = smoothedError * 0.5 + state.getDistance() * 0.5;
        cumulatedError += Math.pow(state.getDistance(), 1);
        path.add(toString());
    }

    private void moveToNext() {
        state.removeParticle();
        this.state = state.getNext();
        state.addParticle();
    }

    public double getSmoothedError() {
        return smoothedError;
    }

    public void setState(CountState state) {
        this.state.removeParticle();
        this.smoothedError = state.getDistance();
        this.state = state;
        this.cumulatedError = state.getDistance();
        this.backwardSteps = 0;
        state.addParticle();
        path.clear();
        path.add(Arrays.toString(Thread.currentThread().getStackTrace()));
        path.add(String.valueOf(cumulatedError));
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
        this.backwardSteps = teacher.backwardSteps;
        this.path.clear();
        this.path.addAll(teacher.path);
    }

    public void setCumulatedError(double cumulatedError) {
        this.cumulatedError = cumulatedError;
        this.path.add("Reset cummulated:" + cumulatedError);
    }

    public void printPath() {
        Log.d("my", path.toString());
    }

    public String toString() {
        return String.format(("%.3f %10.3f \t " + state.getId()), cumulatedError, state.getDistance());
    }
}
