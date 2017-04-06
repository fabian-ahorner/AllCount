package com.bitflake.counter.algo.shared.current.count;

import org.apache.commons.math3.linear.RealVector;

public class CounterVersion {
    public RealVector[] states;
    public double[] stateDistances;

    public CounterVersion(RealVector[] states) {
        this.states = states;
        this.stateDistances = new double[states.length];
    }

    public void updateDistances(RealVector value) {
        for (int i = 0; i < states.length; i++) {
            stateDistances[i] = value.getDistance(states[i]);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterVersion that = (CounterVersion) o;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return states.equals(that.states);
    }

    @Override
    public int hashCode() {
        return states[0].hashCode();
    }

    public void update(CounterVersion v) {
        this.states = v.states;
    }
}
