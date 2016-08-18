package com.bitflake.counter.algo.shared.old;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RouletteWheel<T> {
    private ScoreProvider<T>[] scoreProviders;
    private T[] best;
    private T[] worst;
    private double[] bestScore;
    private double[] worstScore;
    private List<T> elements;
    private double[] totals;
    private List<Selector<T>> selectors = new ArrayList<>();
    private boolean isDirty;

    public RouletteWheel(ScoreProvider<T>... scoreProviders) {
        init(scoreProviders);
        this.elements = new ArrayList<>();
    }

    private void init(ScoreProvider<T>... scoreProviders) {
        this.scoreProviders = scoreProviders;
        this.totals = new double[scoreProviders.length];
        for (int i = 0; i < scoreProviders.length; i++) {
            selectors.add(new Selector<>(this, i));
        }
    }

    public RouletteWheel(List<T> elements, ScoreProvider<T>... scoreProviders) {
        init(scoreProviders);
        setElements(elements);
    }

    public Selector<T> getSelector(int scoreProviderIndex) {
        return selectors.get(scoreProviderIndex);
    }

    public Selector<T> addScoreProvider(ScoreProvider<T> scoreProvider) {
        int newIndex = scoreProviders.length;

        this.scoreProviders = Arrays.copyOf(scoreProviders, newIndex + 1);
        this.best = (T[]) new Object[scoreProviders.length];
        this.worst = (T[]) new Object[scoreProviders.length];
        bestScore = new double[scoreProviders.length];
        worstScore = new double[scoreProviders.length];
        this.scoreProviders[newIndex] = scoreProvider;
        this.totals = Arrays.copyOf(totals, newIndex + 1);
        notifyValuesChanged();

        Selector<T> selector = new Selector<>(this, newIndex);
        selectors.add(selector);
        return selector;
    }

    private double getTotal(int scoreProviderIndex) {
        return totals[scoreProviderIndex];
    }

    public double getAverage(int scoreProviderIndex) {
        return totals[scoreProviderIndex] / elements.size();
    }

    public void clear() {
        this.elements.clear();
        for (int i = 0; i < totals.length; i++) {
            totals[i] = 0;
        }
    }

    public interface ScoreProvider<T> {
        double getScore(T element);
    }

    public T pickElement(int scoreProviderIndex) {
        return elements.get(pickElementIndex(scoreProviderIndex));
    }

    private T pickAndRemoveElement(int scoreProviderIndex) {
        int i = pickElementIndex(scoreProviderIndex);
        return removeElementByIndex(i);
    }

    private T removeElementByIndex(int index) {
        T element = elements.remove(index);
        removeFromTotals(element);
        isDirty = true;
        return element;
    }

    public void removeElement(T element) {
        elements.remove(element);
        removeFromTotals(element);
        isDirty = true;
    }

    private void removeFromTotals(T element) {
        for (int i = 0; i < totals.length; i++) {
            totals[i] -= scoreProviders[i].getScore(element);
        }
    }

    private void addToTotals(T element) {
        for (int i = 0; i < totals.length; i++) {
            double score = scoreProviders[i].getScore(element);
            if (score < 0)
                score = 0;
            totals[i] += score;
            if (best[i] == null || score > bestScore[i]) {
                bestScore[i] = score;
                best[i] = element;
            }
            if (worst[i] == null || score < worstScore[i]) {
                worstScore[i] = score;
                worst[i] = element;
            }
        }
    }

    public List<T> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public void notifyValuesChanged() {
        for (int i = 0; i < totals.length; i++) {
            totals[i] = 0;
            best[i] = null;
            worst[i] = null;
        }
        for (T element : elements) {
            addToTotals(element);
        }
    }

    private int pickElementIndex(int scoreProviderIndex) {
        double total = totals[scoreProviderIndex];
        ScoreProvider<T> scoreProvider = scoreProviders[scoreProviderIndex];
        double rand = Math.random() * total;
        double cum = 0;
        for (int i = 0; i < elements.size(); i++) {
            double score = scoreProvider.getScore(elements.get(i));
            if (score < 0)
                score = 0;
            cum += score;
            if (cum >= rand) {
                return i;
            }
        }
        return elements.size() - 1;
    }

    public void addElement(T element) {
        elements.add(element);
        addToTotals(element);
    }

    public void addElements(T... elements) {
        for (T el :
                elements) {
            addElement(el);
        }
    }

    public void setElements(List<T> elements) {
        this.elements = new ArrayList<>(elements);
        notifyValuesChanged();
    }

    private String toString(int scoreProviderIndex) {
        double total = totals[scoreProviderIndex];
        StringBuilder sb = new StringBuilder("(");
        ScoreProvider<T> scoreProvider = scoreProviders[scoreProviderIndex];
        for (int i = 0; i < elements.size(); i++) {
            sb.append((int) (scoreProvider.getScore(elements.get(i)) * 100 / total));
            sb.append("  ");
        }
        sb.append(")");
        return sb.toString();
    }

    public static class Selector<T> {
        private final int scoreProviderIndex;
        private RouletteWheel<T> roulette;

        public Selector(RouletteWheel<T> roulette, int scoreProviderIndex) {
            this.scoreProviderIndex = scoreProviderIndex;
            this.roulette = roulette;
        }

        public T pickAndRemoveElement() {
            return roulette.pickAndRemoveElement(scoreProviderIndex);
        }

        public T pick() {
            return roulette.pickElement(scoreProviderIndex);
        }

        public double getAverage() {
            return roulette.getAverage(scoreProviderIndex);
        }

        public double getTotal() {
            return roulette.totals[scoreProviderIndex];
        }

        public double getScore(T element) {
            return roulette.scoreProviders[scoreProviderIndex].getScore(element);
        }

        public double getLikelihood(T element) {
            return getScore(element) / getTotal();
        }

        public String toString() {
            return roulette.toString(scoreProviderIndex);
        }

        public T getBest() {
            return roulette.getBest(scoreProviderIndex);
        }

        public T getWorst() {
            return roulette.getBest(scoreProviderIndex);
        }
    }

    private T getBest(int scoreProviderIndex) {
        return best[scoreProviderIndex];
    }

    private T getWorst(int scoreProviderIndex) {
        return worst[scoreProviderIndex];
    }
}
