package com.bitflake.counter;

import com.bitflake.counter.tools.Stats;

public class Particle {
    private double cumulatedError;
    private CountState state;
    private double smoothedError;

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
            if (d2 < 1 && Math.random() > Stats.sigmoidal((d1 - d2) * 15)) {
                state.removeParticle();
                this.state = state.getNext();
                state.addParticle();
            }
//            }
            cumulatedError += state.getDistance();
            smoothedError = smoothedError * 0.5 + state.getDistance() * 0.5;
//            state.removeParticle();
//            int depth = 2;
//            double total = 0;
//            CountState s = state;
//            for (int i = 0; i < depth; i++) {
//                total += s.getScore();
//                s = s.getNext();
//                if (s == null)
//                    break;
//            }
//            double rand = Math.random() * total;
//            double cum = state.getScore();
//            while (cum < rand) {
//                state = state.getNext();
//                cum += state.getScore();
//            }
//            cumulatedError += state.getDistance();
//            state.addParticle();
        }
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
    }

    public void setCumulatedError(double cumulatedError) {
        this.cumulatedError = cumulatedError;
    }
}
