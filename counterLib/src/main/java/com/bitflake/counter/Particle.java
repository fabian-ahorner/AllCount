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
                total += getScore(s);
                s = s.getNext();
                if (s == null)
                    break;
            }
            double rand = Math.random() * total;
            double cum = getScore(state);
            while (cum < rand) {
                state = state.getNext();
                cum += getScore(state);
            }
            error += state.getDistance();
            state.addParticle();
        }
    }

    public double getScore(StateWindow s) {
        return 1 / Math.pow((1 + s.getDistance()), 4);
    }

    public void setState(StateWindow state) {
        this.error = 0;
        this.state.removeParticle();
        this.state = state;
        state.addParticle();
    }

    public double getError() {
        return error;
    }
}
