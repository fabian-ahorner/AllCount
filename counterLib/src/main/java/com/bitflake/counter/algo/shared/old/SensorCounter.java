package com.bitflake.counter.algo.shared.old;


import com.bitflake.counter.algo.shared.SlidingWindow;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.CountWindowAnalyser {
    public static final int PARTICLE_COUNT = 100;

    private List<CountState> states;
    private CountListener listener;
    private ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    private RouletteWheel<Particle> particleSelector = new RouletteWheel<>();
    private RouletteWheel.Selector<Particle> weakHistorySelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_WEAK_HISTORY);
    private RouletteWheel.Selector<Particle> strongHistorySelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG_HISTORY);
    private RouletteWheel.Selector<Particle> strongLikelihoodSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG_LIKELIHOOD);
    private RouletteWheel.Selector<Particle> weakLikelihoodSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_WEAK_LIKELIHOOD);
    private int count;
    private double maxStateDistance;
    private CountState firstState;
    private CountState currentState;
    private Particle startParticle;
    //    private Particle bestParticle;
    private double bestCumulatedError;
    private double worstCumulatedError;
    private CountState goalState;
    private CountState lastFullState;
    private CountState lastTransientState;

    public SensorCounter(List<CountState> states) {
        setStates(states);
    }

    public SensorCounter() {
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        if (states != null) {
            count = 0;
            maxStateDistance = 0;
//            for (CountState s : states) {
//                maxStateDistance = Math.max(maxStateDistance, s.getDistanceToNext());
//            }
            double averageDistance = 0;
            for (int i = 0; i < states.size(); i++) {
                CountState s = states.get(i);
                if (s.getNext() != null)
                    s.getNext().addPrevious(s);
                maxStateDistance = 0;
                for (int j = i + 1; j < states.size(); j++) {
                    maxStateDistance = Math.max(maxStateDistance, s.getDistance(states.get(j)));
                }
                averageDistance += maxStateDistance;
            }
            for (int i = 0; i < states.size(); i++) {
                CountState s = states.get(i);
                s.initRoulette();
            }
            averageDistance /= states.size();
            double var = 0;
            maxStateDistance = 0;
            for (CountState s : states) {
                maxStateDistance = Math.max(maxStateDistance, s.getDistanceToNext());
                var += Math.pow(averageDistance - s.getDistanceToNext(), 2);
            }
            var /= states.size();
//            maxStateDistance += Math.sqrt(var) * 2;
//            maxStateDistance = averageDistance;
            initParticles();
            this.firstState = states.get(0);
            this.goalState = states.get(states.size() - 1);
            this.lastFullState = states.get(states.size() - 2);
            this.lastTransientState = states.get(states.size() - 3);
            firstState.setGlobalRoulette();
            particleSelector.setElements(particles);

            for (CountState s : states) {
                s.setMaxStateDistance(maxStateDistance);
//                Particle p = used Particle(s);
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
    public void analyseValues(double[] values) {
        CountState window = new CountState(values, null);
        currentState = window;
        firstState.updateDistance(window);
        for (CountState s :
                states) {
            s.updateRoulette();
        }
//        Particle bestParticle = strongParticleSelector.getBest();
//        double bestScore = bestParticle.getCumulatedError();
//        bestScore = Math.min(bestScore, startParticle.getCumulatedError());
        for (Particle p : particles) {
            p.move();
//            p.setCumulatedError(p.getCumulatedError() - bestScore);
//            if (Math.random() > p.getState().getLikelihoodOfDistance()) {
//                p.setState(firstState);
//                p.setCumulatedError(bestScore + 1);
//            }
        }
        particleSelector.notifyValuesChanged();
//        bestParticle = strongParticleSelector.getBest();
//        Log.d("my", "Avg:" + bestParticle.getState().getDistanceToNext() / bestParticle.getState().getDistance());
//        int iBestParticleState = states.indexOf(bestParticle.getState());
        int iMostParticleState = getMostLikelyState();

//        if (states.get(iMostParticleState).equals(goalState)) {
        if (goalState.getParticleCount() >= 1.5 * PARTICLE_COUNT / states.size()) {
//            if (goalState.getParticleCount() > PARTICLE_COUNT / 3 && goalState.getLikelihoodInSequenze() > 1f / states.size()) {
//            if (goalState.getLikelihoodInSequenze() > 1f / states.size() && goalState.getLikelihoodOfDistance() >= 1) {
            if (lastFullState.getDistance() > goalState.getDistance() && lastTransientState.getDistance() > goalState.getDistance()) {
                performCount();
//                bestParticle.printPath();
            } else {
                resample(iMostParticleState);
            }
        } else {
            resample(iMostParticleState);
        }

//            CountState bestParticleState = states.get(iBestParticleState);
//        CountState mostParticleState = states.get(iMostParticleState);
//        if (mostParticleState.getNext() == null || (bestParticleState.getNext() == null && bestParticleState.getLikelihoodInSequenze() > 1f / states.size())) {
//            performCount();
//            bestParticle.printPath();
//        } else {
//            resample(iBestParticleState);
//        }
    }

    private void performCount() {
        count++;
        if (listener != null)
            listener.onCount(count);
        resetParticles();
    }

    private void resample(int iMostParticleState) {
        Particle bestParticle = strongLikelihoodSelector.getBest();
        CountState bestState = bestParticle.getState();
//        CountState bestState = states.get(iMostParticleState);

//        for (int i = 0; i < toResample; i++) {
//            Particle weak = weakParticleSelector.pickAndRemoveElement();
//            if (Math.random() > bestParticle.getState().getLikelihoodOfDistance()) {
//                weak.setState(firstState);
//                weak.setCumulatedError(bestParticle.getCumulatedError());
//            } else
//                weak.learnFrom(bestParticle);
//            particleSelector.addElement(weak);
//        }
//        while (bestParticle.getState().getParticleCount() < PARTICLE_COUNT / 2) {
//            Particle weak = weakHistorySelector.pickAndRemoveElement();
//            weak.learnFrom(bestParticle);
//            particleSelector.addElement(weak);
//        }

        int toResample = 2 * PARTICLE_COUNT / states.size() - strongLikelihoodSelector.getBest().getState().getParticleCount();
        for (int i = 0; i < toResample; i++) {
            Particle weak = weakHistorySelector.pickAndRemoveElement();
            Particle strong = strongHistorySelector.pick();
            weak.setState(strong.getState());
            particleSelector.addElement(weak);
        }


        int reset = 0;
        int first = 0;
        for (Particle particle : particles) {
            if (Math.random() > particle.getState().getLikelihoodOfDistance()) {
                if (Math.random() > bestState.getLikelihoodOfDistance()) {
                    particle.setState(firstState);
                    first++;
                } else {
                    particle.setState(bestState);
                }
                reset++;
            }
        }

//        int toResample = Math.max(0, PARTICLE_COUNT / 3 - bestParticle.getState().getParticleCount());
//        Log.d("my", toResample + " " + bestParticle.getState() + " " + bestParticle.getState().getParticleCount());
        String s = "(";
        for (CountState state : states) {
//            s += String.format("%5.2f", state.getDistance());
            s += state == bestState ? "x" : "_";
        }
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
            log2 += String.format("%6.2f", s.getDistance());
            countProgress += p * sIndex;
            sIndex++;
        }
        countProgress /= particles.size() * states.size();
        if (listener != null)
            listener.onCountProgress(countProgress, state);
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

    public float[] getStateDistances(float[] olds) {
        if (olds == null || olds.length != states.size())
            olds = new float[states.size()];
        for (int i = 0; i < states.size(); i++) {
            olds[i] = (float) states.get(i).getDistance();
        }
        return olds;
    }

    public interface CountListener {
        void onCount(int count);

        void onCountProgress(float progress, int mostLikelyState);
    }

    public CountState getCurrentState() {
        return currentState;
    }
}
