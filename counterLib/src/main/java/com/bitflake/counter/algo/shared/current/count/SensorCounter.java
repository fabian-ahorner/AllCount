package com.bitflake.counter.algo.shared.current.count;

import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.tools.ParticlePool;
import com.bitflake.counter.algo.shared.current.tools.Pool;
import com.bitflake.counter.algo.shared.current.tools.ScoreProviders;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

public class SensorCounter implements SlidingWindow.CountWindowAnalyser {
    private List<CountState> states;
    private CountListener listener;
    public ArrayList<Particle> startParticles = new ArrayList<>(CountSettings.PARTICLE_COUNT);
    public ArrayList<Particle> movingParticles = new ArrayList<>(CountSettings.PARTICLE_COUNT);
    private ScoreProviders.Roulette particleSelector = new ScoreProviders.Roulette();
    private ScoreProviders.Roulette movingSelector = new ScoreProviders.Roulette();
    //    private RouletteWheel.Selector<Particle> strongLikelihoodSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_STRONG_LIKELIHOOD);
//    private RouletteWheel.Selector<Particle> distanceSelector = particleSelector.addScoreProvider(ScoreProviders.PARTICLE_DISTANCE);
    private int count;
    public CountState firstState;
    private ArrayRealVector currentValues;
    private double bestCumulatedError;
    private double worstCumulatedError;
    private CountState finalState;
    private CountState lastFullState;
    private CountState lastTransientState;
    private double countProgress;
    public int finalParticles;
    private List<Particle> toResample = new ArrayList<>();
    private RealVector[] stateValues;
    private double maxDistance;
    private ArrayRealVector lastStartParticleUpdate;
    private Pool<Particle> pool;

    private Random rand = new Random();
    private double avgStateDistance;
    private double minDistance;
    public double varState;
    private List<CounterVersion> counterVersions= new ArrayList<>();
    public double mostParticleState;
    private List<CounterVersion> specialVersions = new ArrayList<>();


    public SensorCounter() {
    }

    public SensorCounter(List<CountState> states) {
        setStates(states);
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        if (states == null)
            return;

        count = 0;
        this.maxDistance = 0;
        this.minDistance = Double.MAX_VALUE;
        this.avgStateDistance = 0;
        for (int i = 0; i < states.size(); i++) {
            CountState s = states.get(i);
            maxDistance = Math.max(maxDistance, s.getDistanceToNext());
            if (i < states.size() - 1)
                minDistance = Math.min(minDistance, s.getDistanceToNext());
            if (s.getNext() != null)
                s.getNext().addPrevious(s);
            avgStateDistance += s.getDistanceToNext();
        }
        avgStateDistance /= 2 * states.size();
        for (int i = 0; i < states.size(); i++) {
            CountState s = states.get(i);
            s.resetParticleCount();
            s.initRoulette();
        }
        this.firstState = states.get(0);
        this.finalState = states.get(states.size() - 1);
        this.lastFullState = states.get(states.size() - 2);
        this.lastTransientState = states.get(states.size() - 3);

        pool = new ParticlePool(states);
        firstState.setGlobalRoulette();
        particleSelector.clear();
        startParticles.clear();
        stateValues = new RealVector[states.size()];
        counterVersions.addAll(Arrays.asList(CounterHelper.createCounterVersions(states)));
//        specialVersions.add(CounterHelper.getDefaultVersion(states));
        counterVersions.addAll(specialVersions);
        for (CounterVersion v : counterVersions) {
            Particle p = pool.take();
            startParticles.add(p);
            p.setState(v, 0);
        }
        initParticles();
    }

    private void initParticles() {
        movingParticles.clear();
    }

    private void resetParticles(double[] values) {
        pool.recycleAll(movingParticles);
        movingParticles.clear();
        lastStartParticleUpdate = null;
    }

    public void setCountListener(CountListener listener) {
        this.listener = listener;
    }

    public void reset() {
        count = 0;
        setStates(states);
    }

    @Override
    public void analyseValues(double[] means) {
        ArrayRealVector values = new ArrayRealVector(means);//
        values.unitize();
        if (currentValues == null || values.getDistance(currentValues) > minDistance / CountSettings.MIN_UPDATE_DISTANCE) {
            currentValues = values;
            updateStates();
            moveParticles(values);

            if (shouldCount()) {
                performCount();
            } else {
                resample();
                notifyCountProgress();
            }
        }
    }

    private boolean shouldCount() {
        return finalParticles >= CountSettings.PARTICLE_COUNT * CountSettings.PARTICLE_COUNT_THRESHOLD;
    }

    private void moveParticles(ArrayRealVector values) {
        toResample.clear();
        particleSelector.clear();
        movingSelector.clear();
        finalParticles = 0;
        countProgress = 0;
        for (CounterVersion v : counterVersions) {
            v.updateDistances(values);
        }
        for (Particle p : movingParticles) {
            p.move(values);
            particleSelector.addElement(p);
            movingSelector.addElement(p);
            double d = p.getDistance();
            if (p.isFinalState()
                    && d < p.getDistance(states.size() - 2)
                    && d < p.getDistance(states.size() - 3)
                    ) {
                finalParticles++;
//                if (states.size() > 7)
//                    p.moveToNextCount();
            } else if (p.getCount() > 0) {
                finalParticles++;
            }
            countProgress += p.getCount() > 0 ? states.size() - 1 : p.getStateIndex();
        }
        if (!movingParticles.isEmpty()) {
            countProgress /= movingParticles.size() * (states.size() - 1);
        }
    }

    private void updateStates() {
        firstState.updateDistance(currentValues.getDataRef());
    }

    private void performCount() {
        count++;
        if (listener != null) listener.onCount(count);
        resetParticles(currentValues.getDataRef());
//        specialVersions.get(0).update(CounterHelper.getCurrentVersion(states, currentValues));
    }

    private void resample() {
        double distance = -1;
        if (!particleSelector.isEmpty()) {
            distance = particleSelector.weak.getAverage();
        }
        for (int i = 0; i < startParticles.size(); i++) {
            particleSelector.addElement(startParticles.get(i));
        }
        if (distance < 0) {
            distance = particleSelector.weak.getWorst().getDistance();
        }
//        double avgDiff = 1 - Math.max(0, distance - 1) / (CountSettings.RESAMPLE_STEEPNESS);
        double movingFraction = getMovingFraction(distance);
//        double avgDiff = 1 - distance * CountSettings.MOVING_FRACTION;
//        avgDiff = Math.min(Math.max(0, avgDiff), 1);


        int wantedMoving = (int) (CountSettings.PARTICLE_COUNT * movingFraction);

        Iterator<Particle> particleIt = movingParticles.iterator();
        while (particleIt.hasNext()) {
            Particle p = particleIt.next();
//            if (p.getDistance() > 0.5) {
            if (p.getDistance() > CountSettings.RESET_DISTANCE) {
                movingSelector.removeElement(p);
                particleIt.remove();
                pool.recycle(p);
            }
        }

//        if (bestStartDistance < distance) {
//            for (Particle p : movingParticles) {
//                if (p.getDistance() > bestStartDistance) {
//                    p.setInitials(counterVersions[bestCounterVersion]);
//                }
//            }
//        }
//        if (movingParticles.isEmpty() && wantedMoving > 0) {
//            for (int i = 0; i < wantedMoving; i++) {
//                Particle newParticle = particleCache.poll();
//                newParticle.learnFrom(startSelector.strong.pick());
//                particleSelector.addElement(newParticle);
//                movingParticles.add(newParticle);
//            }
//        }
        while (movingParticles.size() > wantedMoving) {
            Particle p = movingSelector.weak.pickAndRemoveElement();
            movingParticles.remove(p);
            pool.recycle(p);
        }
        while (movingParticles.size() < wantedMoving) {
            Particle newParticle = pool.take();
            newParticle.learnFrom(particleSelector.strong.pick());
            movingParticles.add(newParticle);
        }
//        restructure();
    }

    public void restructure() {
        particleSelector.clear();
        for (int s = 0; s < states.size(); s++) {
            Collection<CountState.ParticleInfo> ps = states.get(s).getParticles();
            for (CountState.ParticleInfo pi : ps) {
                if (pi.hasParticles()) {
                    Particle p = pool.take();
                    p.setState(pi.getCounterVersion(), s);
                    particleSelector.addElement(p);
                }
            }
        }
        for (Particle p : movingParticles) {
            p.learnFrom(particleSelector.strong.pick());
        }
        for (Particle p : particleSelector.getElements()) {
            pool.recycle(p);
        }
        particleSelector.clear();
    }

    private void notifyCountProgress() {
        int max = 0;
        int state = -1;
        String log = "";
        String log2 = "";
        double meanState = 0;
        double var = 0;
        for (int i = 1; i < states.size(); i++) {
            meanState += i * states.get(i).getParticleCount();
        }
        meanState /= movingParticles.size();
        for (int i = 1; i < states.size(); i++) {
            var += i * Math.pow(meanState - i, 2) * states.get(i).getParticleCount();
        }
        for (int i = 0; i < states.size(); i++) {
            CountState s = states.get(i);
            int p = s.getParticleCount();
            if (p > max) {
                max = s.getParticleCount();
                state = i;
            }
            log += String.format("%6d", p);
            log2 += String.format("%6.2f", s.getDistance());
        }
        varState = Math.sqrt(var / movingParticles.size()) /
                states.size();
        mostParticleState = state / (states.size() - 1.);
//        Log.d("my", log + " | " + log2 + " - " + countProgress);
        if (listener != null)
            listener.onCountProgress((float) countProgress, state);
    }

    public boolean hasStates() {
        return states != null;
    }

    public int getCount() {
        return count;
    }

    public double getCountProcess() {
        return countProgress;
    }

    public List<CountState> getStates() {
        return states;
    }

    public double getMovingFraction(double distance) {
//        double movingFraction = 1 - distance * CountSettings.MOVING_FRACTION;
        double movingFraction = 1 - Math.max(0, distance - 1) / (CountSettings.RESAMPLE_STEEPNESS);

        if (movingFraction > 1)
            return 1;
        if (movingFraction < 0)
            return 0;
        return movingFraction;
    }

//    public int getBestCounterVersionIndex() {
//        int bestIndex = -1;
//        double bestDistance = Double.MAX_VALUE;
//        for (int i = 0; i < counterVersions.length; i++) {
//            counterVersionDistances[i] = counterVersions[i][0].getDistance(currentValues);
//            counterVersionDistances[i] /= states.get(0).distanceMax;
//            if (counterVersionDistances[i] < bestDistance) {
//                bestDistance = counterVersionDistances[i];
//                bestIndex = i;
//            }
//        }
//        return bestIndex;
//    }

    public interface CountListener {
        void onCount(int count);
        void onCountProgress(float progress, int mostLikelyState);
    }

    public CountState getCurrentValues() {
        if (currentValues == null)
            return null;
        return new CountState(currentValues.getDataRef());
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
}
