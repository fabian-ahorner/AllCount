package com.bitflake.counter;

public class Particle {
    private double error;
    private StateWindow state;

    public Particle(StateWindow state) {
        this.state = state;
        this.error = 0;
        state.addParticle();
    }

    public void move() {
        if (state.getNext() != null) {
            state.removeParticle();
            int depth = 2;
            double total = 0;
            StateWindow s = state;
            for (int i = 0; i < depth; i++) {
                total += s.getScore();
                s = s.getNext();
                if (s == null)
                    break;
            }
            double rand = Math.random() * total;
            double cum = state.getScore();
            while (cum < rand) {
                state = state.getNext();
                cum += state.getScore();
            }
            error += state.getDistance();
            state.addParticle();
        }
    }


    public void setState(StateWindow state) {
        this.error = 0;
        this.state.removeParticle();
        this.state = state;
        state.addParticle();
    }

    public double getCumulatedError() {
        return error;
    }

    public double getDistance() {
        return state.getDistance();
    }

    public StateWindow getState() {
        return state;
    }
}
