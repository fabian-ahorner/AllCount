package com.bitflake.counter;

import android.util.Log;

import com.bitflake.counter.tools.RouletteWheelSelection;
import com.bitflake.counter.tools.ScoreProviders;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.WindowAnalyser {
    public static final int PARTICLE_COUNT = 100;

    private List<StateWindow> states;
    private CountListener listener;
    private ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    private RouletteWheelSelection<Particle> particleSelector = new RouletteWheelSelection<>(ScoreProviders.PARTICLE);
    private RouletteWheelSelection<StateWindow> stateSelector = new RouletteWheelSelection<>(ScoreProviders.STATE);
    private int count;
    private double maxStateDistance;
    private StateWindow firstState;

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
            count = 0;
            maxStateDistance = 0;
            for (StateWindow s : states) {
                maxStateDistance = Math.max(maxStateDistance, s.getDistanceToNext());
            }
            maxStateDistance *= 2;
            this.firstState = states.get(0);
            particleSelector.setElements(particles);
            stateSelector.setElements(states);
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
            totalError += p.getCumulatedError();
        }
        int mostLikelyStateIndex = getMostLikelyState();
        StateWindow mostLikelyState = states.get(mostLikelyStateIndex);
        if (mostLikelyState.getNext() == null) {//&& mostLikelyState.getParticleCount() > particles.size() / 2
            count++;
            listener.onCount(count);
            resetParticles();
        } else {
            resample(mostLikelyStateIndex);
        }
    }

    private void resample(int mostLikelyStateIndex) {
        StateWindow mostLikelyState = states.get(mostLikelyStateIndex);

        boolean isDeadEnd = mostLikelyState.getDistance() > maxStateDistance;
        double totalError = 0;
        stateSelector.setFromTo(0, mostLikelyStateIndex + 1);

        for (Particle p : particles) {
            if (p.getCumulatedError() > maxStateDistance) {
                if (isDeadEnd)
                    p.setState(firstState);
                else
//                    p.setState(stateSelector.pickElement());
                    p.setState(mostLikelyState);
            } else
                totalError += p.getCumulatedError();
        }
        for (int i = 0; i < particles.size() / 5; i++) {
            double rand = Math.random() * totalError;
            double cum = 0;
            for (Particle p : particles) {
                cum += p.getCumulatedError();

                if (cum >= rand) {
                    totalError -= p.getCumulatedError();
//                    int newState = stateSelector.pickElement();
                    p.setState(stateSelector.pickElement());
//                    p.setState(mostLikelyState);
                    break;
                }
            }
            if (totalError == 0)
                break;
        }
//        for (int i = 0; i < particles.size() / 10; i++) {
//            Particle p = particleSelector.pickAndRemoveElement();
//            int newState = RouletteWheelSelection.getSelectedIndex(states, ScoreProviders.STATE, 0, mostLikelyStateIndex + 1);
//            p.setState(states.get(newState));
//            particleSelector.addElement(p);
//        }
    }

    private void resetParticles() {
        if (states != null) {
            StateWindow startState = states.get(0);
            for (Particle p : particles) {
                p.setState(startState);
            }
        }
    }

    public int getMostLikelyState() {
        int max = 0;
        int state = -1;
        String log = "";
        String log2 = "";
        int sIndex = 1;
        float countProgress = 0;
        for (int i = 0; i < states.size(); i++) {
            StateWindow s = states.get(i);
            int p = s.getParticleCount();
            if (p > max) {
                max = s.getParticleCount();
                state = i;
            }
            log += String.format("%6d", p);
            log2 += String.format("%6.1f", s.getScore());
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

    public float[] getParticleCounts(float[] olds) {
        if (olds == null || olds.length != states.size())
            olds = new float[states.size()];
        for (int i = 0; i < states.size(); i++) {
            olds[i] = (float) states.get(i).getParticleCount();
        }
        return olds;
    }

    public float[] getStateScores(float[] olds) {
        if (olds == null || olds.length != states.size())
            olds = new float[states.size()];
        for (int i = 0; i < states.size(); i++) {
            olds[i] = (float) states.get(i).getScore();
        }
        return olds;
    }

    public interface CountListener {
        void onCount(int count);

        void onCountProgress(float progress);
    }
}
