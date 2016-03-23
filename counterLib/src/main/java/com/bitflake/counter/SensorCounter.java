package com.bitflake.counter;

import android.util.Log;

import com.bitflake.counter.tools.RouletteWheelSelection;
import com.bitflake.counter.tools.ScoreProviders;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.WindowAnalyser {
    public static final int PARTICLE_COUNT = 100;

    private List<CountState> states;
    private CountListener listener;
    private ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    private RouletteWheelSelection<Particle> weakParticleSelector = new RouletteWheelSelection<>(ScoreProviders.PARTICLE_WEAK);
    private RouletteWheelSelection<Particle> strongParticleSelector = new RouletteWheelSelection<>(ScoreProviders.PARTICLE_STRONG);
    private RouletteWheelSelection<CountState> stateSelector = new RouletteWheelSelection<>(ScoreProviders.STATE);
    private int count;
    private double maxStateDistance;
    private CountState firstState;
    private CountState lastState;
    private Particle startParticle;
    private Particle bestParticle;

    public SensorCounter() {
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        if (states != null) {
            count = 0;
            maxStateDistance = 0;
            for (CountState s : states) {
                maxStateDistance = Math.max(maxStateDistance, s.getDistanceToNext());
            }
            initParticles();
            this.firstState = states.get(0);
            weakParticleSelector.setElements(particles);
            strongParticleSelector.setElements(particles);
            stateSelector.setElements(states);


            for (CountState s : states) {
                s.setMaxStateDistance(maxStateDistance);
//                Particle p = new Particle(s);
//                strongParticleSelector.addElement(p);
            }
            startParticle = new Particle(firstState);
//            strongParticleSelector.addElement(startParticle);
        }
    }

    private void initParticles() {
        if (particles.isEmpty()) {
            CountState startState = states.get(0);
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                particles.add(new Particle(startState));
            }
        } else {
            resetParticles();
        }
    }

    public void setCountListener(CountListener listener) {
        this.listener = listener;
    }

    public void reset() {
        count = 0;
        resetParticles();
    }

    @Override
    public void analyseWindow(CountState window) {
        lastState = window;
        CountState startState = states.get(0);
        startState.updateDistance(window);
        double avgError = weakParticleSelector.getAverage();
        if (avgError == 0)
            avgError = 1;
        for (Particle p : particles) {
            p.move();
            p.setCumulatedError(p.getCumulatedError() / avgError);
        }
//        int mostLikelyStateIndex = getMostLikelyState();
        int mostLikelyStateIndex = getBestParticlStateIndex();
        int countProgress = particles.size() * states.size();
        listener.onCountProgress(countProgress, mostLikelyStateIndex);

        CountState mostLikelyState = states.get(mostLikelyStateIndex);
        if (mostLikelyState.getNext() == null) {//&& mostLikelyState.getParticleCount() > particles.size() / 2
            performCount();
        } else {
            resample(mostLikelyStateIndex);
        }
    }

    private void performCount() {
        count++;
        listener.onCount(count);
        resetParticles();
    }

    private void resample(int mostLikelyStateIndex) {
        CountState mostLikelyState = states.get(mostLikelyStateIndex);

        boolean isDeadEnd = mostLikelyState.getDistance() > 1;
        double totalError = 0;
        stateSelector.setFromTo(0, mostLikelyStateIndex + 1);

//        for (Particle p : particles) {
//            if (p.getDistance() > maxStateDistance * Math.random()) {
////                if (isDeadEnd)
////                    p.setState(firstState);
////                else
//                p.setState(stateSelector.pickElement());
////                p.setState(mostLikelyState);
//            } else
//                totalError += p.getCumulatedError();
//        }
        startParticle.setCumulatedError(bestParticle.getCumulatedError() + firstState.getDistance());
        weakParticleSelector.notifyValuesChanged();
        strongParticleSelector.notifyValuesChanged();

//        if (weakParticleSelector.getAverage() > 1) {
//        for (Particle p : particles) {
//            double error = p.getSmoothedError();
//            if (error > 1 && Math.random() < (1 - 1 / error)) {
//                p.setState(firstState);
//            }
//            }
//        }
        double averageError = weakParticleSelector.getAverage();
        for (int i = 0; i < particles.size() / 1000; i++) {
            Particle weak = weakParticleSelector.pickAndRemoveElement();
            strongParticleSelector.removeElement(weak);

//            double error = weak.getSmoothedError();
//            if (error > 1) {
//                if (isDeadEnd) {
//                    weak.setState(stateSelector.pickElement());
//                } else if (Math.random() < (1 - 2 / error)) {
            Particle strong = strongParticleSelector.pickElement();
            weak.learnFrom(strong);
//                }
//            }

            weakParticleSelector.addElement(weak);
            strongParticleSelector.addElement(weak);

//            }
        }

//        for (int i = 0; i < particles.size() / 5; i++) {
//            double rand = Math.random() * totalError;
//            double cum = 0;
//            for (Particle p : particles) {
//                cum += p.getCumulatedError();
//
//                if (cum >= rand) {
//                    totalError -= p.getCumulatedError();
////                    int newState = stateSelector.pickElement();
////                    p.setState(stateSelector.pickElement());
////                    p.setState(mostLikelyState);
//                    if (isDeadEnd)
//                        p.setState(firstState);
//                    else
////                    p.setState(stateSelector.pickElement());
//                        p.setState(mostLikelyState);
//                    break;
//                }
//            }
//            if (totalError == 0)
//                break;
//        }
//        for (int i = 0; i < particles.size() / 10; i++) {
//            Particle p = weakParticleSelector.pickAndRemoveElement();
//            int newState = RouletteWheelSelection.getSelectedIndex(states, ScoreProviders.STATE, 0, mostLikelyStateIndex + 1);
//            p.setState(states.get(newState));
//            weakParticleSelector.addElement(p);
//        }
    }

    private void resetParticles() {
        if (states != null) {
            CountState startState = states.get(0);
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
            CountState s = states.get(i);
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
        listener.onCountProgress(countProgress, state);
        Log.d("bitflake", log + " | " + log2);
        return state;
    }

    public int getBestParticlStateIndex() {
        bestParticle = particles.get(0);
        for (Particle p :
                particles) {
            if (p.getCumulatedError() < bestParticle.getCumulatedError())
                bestParticle = p;
        }
        return states.indexOf(bestParticle.getState());
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

        void onCountProgress(float progress, int mostLikelyState);
    }

    public CountState getLastState() {
        return lastState;
    }
}
