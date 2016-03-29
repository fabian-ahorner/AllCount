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
        if (state.getNext() != null) {
            double d1 = state.getDistance();
            double d2 = state.getNext().getDistance();
//            if (d2 < d1 || Math.random() > 0.95) {
            if (Math.random() > JITTER / 2 + Stats.sigmoidal((d1 - d2) * 10) * (1 - JITTER)) {//d2 < 1 &&
                moveToNext();
            }
//            }
            smoothedError = smoothedError * 0.5 + state.getDistance() * 0.5;
        }
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

    public void learnFrom(Particle strong) {
        this.setState(strong.getState());
        this.cumulatedError = strong.cumulatedError;
        this.path.clear();
        this.path.addAll(strong.path);
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
