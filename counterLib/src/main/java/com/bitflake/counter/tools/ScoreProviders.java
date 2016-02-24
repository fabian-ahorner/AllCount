package com.bitflake.counter.tools;

import com.bitflake.counter.Particle;
import com.bitflake.counter.StateWindow;

public class ScoreProviders {
    public static final RouletteWheelSelection.ScoreProvider<Particle> PARTICLE = new RouletteWheelSelection.ScoreProvider<Particle>() {
        @Override
        public double getScore(Particle p) {
            return p.getDistance();
        }
    };
    public static final RouletteWheelSelection.ScoreProvider<StateWindow> STATE = new RouletteWheelSelection.ScoreProvider<StateWindow>() {
        @Override
        public double getScore(StateWindow s) {
            return s.getParticleCount() * s.getScore();
        }
    };
}
