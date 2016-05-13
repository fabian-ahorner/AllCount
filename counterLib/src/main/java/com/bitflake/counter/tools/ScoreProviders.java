package com.bitflake.counter.tools;

import com.bitflake.counter.CountState;
import com.bitflake.counter.Particle;

public class ScoreProviders {
    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_WEAK = new RouletteWheel.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
//            return p.getCumulatedError();

            return Math.pow(-p.getLikelihood(), 2);
        }
    };
    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_STRONG = new RouletteWheel.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
//            return 1 / Math.pow(1 + p.getCumulatedError(), 3);

            return 1 / (0.0001 - p.getLikelihood());
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
            return Math.exp(-s.getDistance() * 2);//
        }
    };

    public static class NextStateProvider implements RouletteWheel.ScoreProvider<CountState> {
        private static final double BACK_STEP_PROBABILITY = 0.01;
        private final int id;

        public NextStateProvider(CountState state) {
            this.id = state.getId();
        }

        @Override
        public double getScore(CountState s) {
            return Math.exp(-s.getDistance() * 4) * (id > s.getId() ? BACK_STEP_PROBABILITY : (1 - BACK_STEP_PROBABILITY) / 2);
        }
    }
}
