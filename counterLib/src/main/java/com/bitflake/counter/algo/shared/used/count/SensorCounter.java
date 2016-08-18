package com.bitflake.counter.algo.shared.used.count;


import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.algo.shared.used.CountSettings;
import com.bitflake.counter.algo.shared.used.CountState;
import com.bitflake.counter.algo.shared.used.tools.RouletteWheel;
import com.bitflake.counter.algo.shared.used.tools.ScoreProviders;
import com.bitflake.counter.algo.shared.used.tools.VecHelper;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

public class SensorCounter implements SlidingWindow.CountWindowAnalyser {
    private List<CountState> states;
    private CountListener listener;
    public ArrayList<Particle> particles = new ArrayList<>(CountSettings.PARTICLE_COUNT);
    private RouletteWheel<Particle> particleSelector = new RouletteWheel<>();
    private RouletteWheel.Selector<Particle> strongLikelihoodSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG_LIKELIHOOD);
    private int count;
    //    private double maxStateDistance;
    public CountState firstState;
    private ArrayRealVector currentState;
    //    private Particle bestParticle;
    private double bestCumulatedError;
    private double worstCumulatedError;
    private CountState finalState;
    private CountState lastFullState;
    private CountState lastTransientState;
    private float countProgress;
    public int finalParticles;
    private List<Particle> toResample = new ArrayList<>();
    private RealVector[] stateValues;
    public double avgDist;
    private double maxDistance;

    public SensorCounter() {
    }

    public SensorCounter(List<CountState> states) {
        setStates(states);
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        if (states != null) {
            count = 0;
            this.maxDistance = 0;
            for (int i = 0; i < states.size(); i++) {
                CountState s = states.get(i);
                maxDistance = Math.max(maxDistance, s.getDistanceToNext());
                if (s.getNext() != null)
                    s.getNext().addPrevious(s);
            }
            for (int i = 0; i < states.size(); i++) {
                CountState s = states.get(i);
                s.resetParticleCount();
                s.initRoulette();
            }
            double var = 0;
            this.firstState = states.get(0);
            this.finalState = states.get(states.size() - 1);
            this.lastFullState = states.get(states.size() - 2);
            this.lastTransientState = states.get(states.size() - 3);
            firstState.setGlobalRoulette();
            particleSelector.setElements(particles);
            particles.clear();
//            for (CountState s : states) {
//                s.setMaxStateDistance(maxStateDistance);
//            }
            stateValues = new RealVector[states.size()];
        }
    }

    private void initParticles(double[] values) {
        particles.clear();
        for (int i = 0; i < CountSettings.PARTICLE_COUNT; i++) {
            Particle p = new Particle(states);
//            p.setInitials(values);
            particles.add(p);
        }
    }

    private void resetParticles(double[] values) {
        for (Particle particle : particles) {
            if (particle.getCount() == 0 || particle.getStateIndex() > states.size() - 1)
                particle.setState(0);
            particle.reduceCount();
//            particle.setInitials(stateValues);
        }
    }

    public void setCountListener(CountListener listener) {
        this.listener = listener;
    }

    public void reset() {
        count = 0;
        setStates(states);
    }

    private int totalUpdates = 0;
    private int executedUpdates = 0;

    @Override
    public void analyseWindow(double[] means) {
        totalUpdates++;

        ArrayRealVector values = new ArrayRealVector(means);//
        if (currentState == null || values.getDistance(currentState) > CountSettings.MIN_UPDATE_DISTANCE) {
            if (particles.isEmpty()) {
                initParticles(means);
            }
            executedUpdates++;
            currentState = values;

            updateStates();
            moveParticles(values);

            if (shouldCount()) {
                performCount();
            } else {
                notifyCountProgress();
                resample();
            }
        }
    }

    private boolean shouldCount() {
        return (finalParticles >= CountSettings.PARTICLE_COUNT * CountSettings.PARTICLE_COUNT_THRESHOLD//CountSettings.PARTICLE_COUNT_THRESHOLD  / Math.pow(states.size(), 1)
//                || finalParticles > 30 && lastTransientState.getParticleCount() > 20
        )
//                && lastFullState.getDistance() > finalState.getDistance()
//                && lastTransientState.getDistance() > finalState.getDistance()
                ;
    }

    private void moveParticles(ArrayRealVector values) {
        toResample.clear();
        particleSelector.clear();
        finalParticles = 0;
        avgDist = 0;
        for (Particle p : particles) {
            p.move(values);
            double d = p.getDistance();
            double nextDistance = p.getState().getDistanceToNext() * 3;
            if (p.getState() == finalState)
                nextDistance = firstState.getDistanceToNext();
            double relD = (d - nextDistance) / nextDistance;

            if (p.getState() == finalState
                    && p.getDistance() < p.getDistance(states.size() - 2)
                    && p.getDistance() < p.getDistance(states.size() - 3)
                    ) {
                finalParticles++;
                if (states.size() > 7)
                    p.moveToNextCount();
            } else if (p.getCount() > 0) {
                finalParticles++;
            }


            if (p.getState() == firstState) {
                avgDist += d;
//                if (d > firstState.getDistance() * 3) {
//                    if (Math.random() > 0.5)
//                        p.setInitials(stateValues);
//                } else
                if (Math.random() < sigmoid((relD - 0) * 4)) {
//                    if (Math.random() > 0.5)
                    p.setInitials(stateValues);
//                    else
//                        toResample.add(p);
                }
//            } else if (Math.random() < sigmoid((CountSettings.RESET_DISTANCNE-0.25)*CountSettings.RESET_STRICTNESS) && !p.isValid()) {
            } else if (Math.random() < sigmoid((d - CountSettings.RESET_DISTANCNE - nextDistance) * CountSettings.RESET_STRICTNESS) && !p.isValid()) {
                toResample.add(p);
            } else {
                particleSelector.addElement(p);
            }
        }
        if (firstState.getParticleCount() == 0)
            avgDist = -10;
        else
            avgDist /= firstState.getParticleCount();
        avgDist = particles.get(0).getDistance(0);


        if (!particleSelector.isEmpty()) {
            for (Particle p : toResample) {
                if (firstState.getParticleCount() < CountSettings.PARTICLE_COUNT * CountSettings.START_PARTICLES) {
                    p.setInitials(stateValues);
                } else {
                    p.learnFrom(strongLikelihoodSelector.pick());
                }
            }
        }
    }

    private void updateStates() {

//        updateStateValues();
        updateStateValues();
        firstState.updateDistance(currentState.getDataRef());
//        for (CountState s :
//                states) {
//            s.updateRoulette();
//        }
    }

    public void updateStateValues() {
        RealMatrix rotationMatrix = VecHelper.getRotMatrix(firstState.means, currentState.getDataRef());
        stateValues = new RealVector[stateValues.length];
        for (int i = 0; i < stateValues.length; i++) {
            CountState s = states.get(i);
            RealVector vec = new ArrayRealVector(s.means);
            stateValues[i] = rotationMatrix.preMultiply(vec);
        }
    }

    private void performCount() {
        count++;
        if (listener != null) listener.onCount(count);
        resetParticles(currentState.getDataRef());
//        System.out.println("Cor: " + (firstState.getDistance(currentState.getDataRef())));
    }

    public static double sigmoid(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    private void resample() {
    }

    public void notifyCountProgress() {
        int max = 0;
        int state = -1;
        String log = "";
        String log2 = "";
        int sIndex = 0;
        countProgress = 0;
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
//        countProgress /= particles.size() * (states.size() - 1);
        countProgress = state / (float) states.size();
//        System.out.println(log + " | " + log2 + " - " + countProgress);
        if (listener != null)
            listener.onCountProgress(countProgress, state);
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

    public float getCountProcess() {
        return countProgress;
    }

    public List<CountState> getStates() {
        return states;
    }

    public interface CountListener {
        void onCount(int count);

        void onCountProgress(float progress, int mostLikelyState);

    }

    public CountState getCurrentState() {
        if (currentState == null)
            return null;
        return new CountState(currentState.getDataRef());
    }
}
