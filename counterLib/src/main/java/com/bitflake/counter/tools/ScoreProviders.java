package com.bitflake.counter.tools;

import com.bitflake.counter.CountState;
import com.bitflake.counter.Particle;

public class ScoreProviders {
    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_WEAK = new RouletteWheel.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
            return p.getCumulatedError();
        }
    };
    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_STRONG = new RouletteWheel.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
//            return Math.exp(-s.getDistance()*2);//

            return 1 / Math.pow(1 + p.getCumulatedError(), 3);
        }
    };
    public static final RouletteWheel.ScoreProvider<CountState> STATE_WEAK = new RouletteWheel.ScoreProvider<CountState>() {
        @Override
        public double getScore(CountState s) {
            // 0.00001: To avoid zero scores
            return Math.exp(s.getDistance());//
        }
    };
    public static final RouletteWheel.ScoreProvider<CountState> STATE_STRONG = new RouletteWheel.ScoreProvider<CountState>() {
        @Override
        public double getScore(CountState s) {
            // 0.00001: To avoid zero scores
            return Math.exp(-s.getDistance()*2);//
        }
    };
}
