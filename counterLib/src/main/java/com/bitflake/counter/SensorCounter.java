package com.bitflake.counter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.WindowAnalyser {
    private static final int PARTICLE_COUNT = 100;

    private List<StateWindow> states;
    private CountListener listener;
    private SlidingWindow slidingWindow;
    private ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    private int count;

    public SensorCounter() {

    }

    public void setStates(List<StateWindow> states) {
        this.states = states;
        if (states != null) {
            if (particles.isEmpty()) {
                StateWindow startState = states.get(0);
                for (int i = 0; i < PARTICLE_COUNT; i++) {
                    particles.add(new Particle(startState));
                }
            } else {
                resetParticles();
            }
        }
    }

    public void setCountListener(CountListener listener) {
        this.listener = listener;
    }

    private void createParticles(int particleCount) {
        StateWindow startState = states.get(0);
        if (particles == null) {
            this.particles = new ArrayList<>();
            for (int i = 0; i < particleCount; i++) {
                particles.add(new Particle(startState));
            }
        } else {
            resetParticles();
        }
    }

    public void reset() {
        count = 0;
        resetParticles();
    }

    @Override
    public void analyseWindow(StateWindow window) {
        StateWindow startState = states.get(0);
        startState.updateDistance(window);
        double totalError = 0;
        for (Particle p : particles) {
            p.move();
            totalError += p.getError();
        }
        StateWindow mostLikelyState = getMostLikelyState();
        if (mostLikelyState.getNext() == null) {
            count++;
            listener.onCount(count);
            resetParticles();
        } else {
            for (int i = 0; i < particles.size() / 5; i++) {
                double rand = Math.random() * totalError;
                double cum = 0;
                for (Particle p : particles) {
                    cum += p.getError();
                    if (cum >= rand) {
                        totalError -= p.getError();
                        p.setState(mostLikelyState);
                        break;
                    }
                }
            }
        }
    }

    private void resetParticles() {
        if (states != null) {
            StateWindow startState = states.get(0);
            for (Particle p : particles) {
                p.setState(startState);
            }
        }
    }

    public StateWindow getMostLikelyState() {
        int max = 0;
        StateWindow state = null;
        String log = "";
        String log2 = "";
        int sIndex = 1;
        float countProgress = 0;
        for (StateWindow s : states) {
            int p = s.getParticleCount();
            if (p > max) {
                max = s.getParticleCount();
                state = s;
            }
            log += String.format("%8d", p);
            log2 += String.format("%8.1f", 1 / (s.getDistance() + 1));
            countProgress += p * sIndex;
            sIndex++;
        }
        countProgress /= particles.size() * states.size();
        listener.onCountProgress(countProgress);
        Log.d("bitflake", log + " | " + log2);
        return state;
    }

    public boolean hasStates() {
        return states != null;
    }

    public int getCount() {
        return count;
    }

    public interface CountListener {
        void onCount(int count);

        void onCountProgress(float progress);
    }
}
