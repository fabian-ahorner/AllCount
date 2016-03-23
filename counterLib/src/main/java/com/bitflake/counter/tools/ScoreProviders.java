package com.bitflake.counter.tools;

import com.bitflake.counter.CountState;
import com.bitflake.counter.Particle;
import com.bitflake.counter.SensorCounter;

public class ScoreProviders {
    public static final RouletteWheelSelection.ScoreProvider<Particle> PARTICLE_WEAK = new RouletteWheelSelection.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
            return p.getCumulatedError();
        }
    };
    public static final RouletteWheelSelection.ScoreProvider<Particle> PARTICLE_STRONG = new RouletteWheelSelection.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
            return 1 / (1 + p.getCumulatedError());
        }
    };
    public static final RouletteWheelSelection.ScoreProvider<CountState> STATE = new RouletteWheelSelection.ScoreProvider<CountState>() {
        @Override
        public double getScore(CountState s) {
            // 0.00001: To avoid zero scores
            return Math.exp(s.getDistance());//
        }
    };
}
