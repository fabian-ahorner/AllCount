package com.bitflake.counter.algo.shared.used.tools;


import com.bitflake.counter.algo.shared.used.CountSettings;
import com.bitflake.counter.algo.shared.used.CountState;
import com.bitflake.counter.algo.shared.used.count.Particle;

public class ScoreProviders {
    //    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_WEAK_HISTORY = used RouletteWheel.ScoreProvider<Particle>() {
//        @Override
//        public double getScore(Particle p) {
//            return Math.pow(p.getCumulatedError(), 2);
////            return Math.pow(-p.getLikelihood(), 2);
//        }
//    };
//    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_STRONG_HISTORY = used RouletteWheel.ScoreProvider<Particle>() {
//        @Override
//        public double getScore(Particle p) {
//            return Math.pow(1 + p.getCumulatedError(), -CountSettings.STRONG_PARTICLE_HISTORY_DRIVE);
//
////            return 1 / (0.0001 - p.getLikelihood());
//        }
//    };
    //
    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_STRONG_LIKELIHOOD = new RouletteWheel.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
            return Math.max(0, 10 - p.getDistance());
//            return Math.exp(-p.getDistance() * CountSettings.STRONG_PARTICLE_LIKELIHOOD_DRIVE);
        }
    };

//    public static final RouletteWheel.ScoreProvider<Particle> PARTICLE_WEAK_LIKELIHOOD = used RouletteWheel.ScoreProvider<Particle>() {
//        @Override
//        public double getScore(Particle p) {
//            return Math.pow(-p.getLikelihood(), 2);
//        }
//    };

    //    public static final RouletteWheel.ScoreProvider<CountState> STATE_WEAK = used RouletteWheel.ScoreProvider<CountState>() {
//        @Override
//        public double getScore(CountState s) {
//            // 0.00001: To avoid zero scores
//            return Math.exp(s.getScore());//
//        }
//    };
//    public static final RouletteWheel.ScoreProvider<CountState> STATE_STRONG = used RouletteWheel.ScoreProvider<CountState>() {
//        @Override
//        public double getScore(CountState s) {
//            // 0.00001: To avoid zero scores
//            return Math.exp(-s.getScore() * CountSettings.STRONG_STATE_DRIVE);//
//        }
//    };

    public static class NextStateProvider implements RouletteWheel.ScoreProvider<CountState> {
        private final int id;

        public NextStateProvider(CountState state) {
            this.id = state.getId();
        }

        @Override
        public double getScore(CountState s) {
//            return Math.pow(s.getLikelihood(), CountSettings.NEXT_POWER);
            return Math.exp(-s.getDistance() * CountSettings.MOVE_DRIVE);//* (id > s.getId() ? CountSettings.BACK_STEP_PROBABILITY : (1 - CountSettings.BACK_STEP_PROBABILITY) / 2);
        }
    }
}
