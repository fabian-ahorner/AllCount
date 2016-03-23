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
    private RouletteWheelSelection<Particle> particleSelector = new RouletteWheelSelection<>();
    private RouletteWheelSelection.Selector<Particle> weakParticleSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_WEAK);
    ;
    private RouletteWheelSelection.Selector<Particle> strongParticleSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG);
    private int count;
    private double maxStateDistance;
    private CountState firstState;
    private CountState lastState;
    private Particle startParticle;
    private Particle bestParticle;
    private double bestCumulatedError;
    private double worstCumulatedError;

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
        double avgError = weakParticleSelector.getAverage();
        if (avgError == 0)
            avgError = 1;
        for (Particle p : particles) {
            p.move();
//            p.setCumulatedError(p.getCumulatedError() / avgError);
        }
        int mostLikelyStateIndex = getBestParticlStateIndex();
        int countProgress = particles.size() * states.size();
        listener.onCountProgress(countProgress, getMostLikelyState());

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
        startParticle.setCumulatedError(startParticle.getCumulatedError() + 1);
        particleSelector.notifyValuesChanged();

        Particle worst = null;
        for (int i = 0; i < particles.size() / 8; i++) {
            Particle weak = weakParticleSelector.pickAndRemoveElement();
            Particle strong = getStrongParticle();
            weak.learnFrom(strong);
            particleSelector.addElement(weak);

            if (worst == null || weak.getCumulatedError() > worst.getCumulatedError())
                worst = weak;
        }
//        Log.d("my", "best=" + bestCumulatedError + " " + weakParticleSelector.getAverage());
        while (firstState.getParticleCount() < particles.size() / 20) {
            Particle weak = weakParticleSelector.pickAndRemoveElement();
            weak.setState(firstState);
            weak.setCumulatedError(worstCumulatedError);
            particleSelector.addElement(weak);
        }
        startParticle.setCumulatedError(Math.min(weakParticleSelector.getAverage(), startParticle.getCumulatedError()));
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

    public int getBestParticlStateIndex() {
        bestParticle = particles.get(0);
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
//        Log.d("my", String.format("%10.4f %10.5f", startScore / strongParticleSelector.getAverage(), +startScore / total));
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
