package com.bitflake.counter.algo.shared.current.tools;

import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.count.Particle;

import java.util.List;

public class ParticlePool extends Pool<Particle> implements Pool.Factory<Particle> {
    private static class ParticleFactory implements Factory<Particle> {
        private final List<CountState> states;

        public ParticleFactory(List<CountState> states) {
            this.states = states;
        }

        @Override
        public Particle createNew() {
            return new Particle(states);
        }
    }

    public ParticlePool(List<CountState> states) {
        super(new ParticleFactory(states));
        setOnRecycleListener(new RecycleListener<Particle>() {
            @Override
            public void onRecycle(Particle p) {
                p.disable();
            }
        });
    }

    @Override
    public Particle createNew() {
        return null;
    }
}
