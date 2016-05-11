package com.bitflake.counter;

import android.util.Log;

import com.bitflake.counter.tools.RouletteWheel;
import com.bitflake.counter.tools.ScoreProviders;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.WindowAnalyser {
    public static final int PARTICLE_COUNT = 100;

    private List<CountState> states;
    private CountListener listener;
    private ArrayList<Particle> particles = new ArrayList<>(PARTICLE_COUNT);
    private RouletteWheel<Particle> particleSelector = new RouletteWheel<>();
    private RouletteWheel.Selector<Particle> weakParticleSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_WEAK);
    private RouletteWheel.Selector<Particle> strongParticleSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG);
    private int count;
    private double maxStateDistance;
    private CountState firstState;
    private CountState lastState;
    private Particle startParticle;
    //    private Particle bestParticle;
    private double bestCumulatedError;
    private double worstCumulatedError;

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
            particleSelector.setElements(particles);

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
        firstState.updateDistance(window);
        for (CountState s :
                states) {
            s.updateRoulette();
        }
        Particle bestParticle = strongParticleSelector.getBest();
        double bestScore = bestParticle.getCumulatedError();

        bestScore = Math.min(bestScore, startParticle.getCumulatedError());
        for (Particle p : particles) {
            p.move();
            p.setCumulatedError(p.getCumulatedError() - bestScore);
        }
        particleSelector.notifyValuesChanged();
        int iBestParticleState = states.indexOf(bestParticle.getState());
        int iMostParticleState = getMostLikelyState();

        CountState bestParticleState = states.get(iBestParticleState);
        CountState mostParticleState = states.get(iMostParticleState);
        if (mostParticleState.getNext() == null || (bestParticleState.getNext() == null && bestParticleState.getDistance() <= mostParticleState.getDistance())) {//&& mostLikelyState.getParticleCount() > particles.size() / 2
            performCount();
            bestParticle.printPath();
        } else {
            resample(iBestParticleState);
        }
    }

    private void performCount() {
        count++;
        listener.onCount(count);
        resetParticles();
    }

    private void resample(int mostLikelyStateIndex) {
//        startParticle.setCumulatedError(startParticle.getCumulatedError() + 1);

        double worstCummulated = weakParticleSelector.getWorst().getCumulatedError();

        double averageError = weakParticleSelector.getAverage();
        for (int i = 0; i < particles.size() / 10; i++) {
            Particle weak = weakParticleSelector.pickAndRemoveElement();
            Particle strong = getStrongParticle();
            weak.learnFrom(strong);
            particleSelector.addElement(weak);
        }
        startParticle.setCumulatedError(Math.min(worstCummulated, startParticle.getCumulatedError() + 1));
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
        listener.onCountProgress(countProgress, state);
//        Log.d("bitflake", log + " | " + log2);
        return state;
    }

    public int getBestParticleStateIndex() {
        Particle bestParticle = particles.get(0);
        worstCumulatedError = 0;
        for (Particle p :
                particles) {
            if (p.getCumulatedError() < bestParticle.getCumulatedError())
                bestParticle = p;
            worstCumulatedError = Math.max(worstCumulatedError, p.getCumulatedError());
        }
        bestCumulatedError = bestParticle.getCumulatedError();
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

    public float[] getStateDistances(float[] olds) {
        if (olds == null || olds.length != states.size())
            olds = new float[states.size()];
        for (int i = 0; i < states.size(); i++) {
            olds[i] = (float) states.get(i).getDistance();
        }
        return olds;
    }

    /**
     * Return a strong particle or the start particle
     *
     * @return
     */
    public Particle getStrongParticle() {
        double startScore = strongParticleSelector.getScore(startParticle);
        double total = startScore + strongParticleSelector.getTotal();
        if (Math.random() * total > startScore)
            return strongParticleSelector.pickElement();
        return startParticle;
    }

    public interface CountListener {
        void onCount(int count);

        void onCountProgress(float progress, int mostLikelyState);
    }

    public CountState getLastState() {
        return lastState;
    }
}
