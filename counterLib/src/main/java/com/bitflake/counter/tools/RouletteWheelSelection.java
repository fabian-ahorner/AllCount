package com.bitflake.counter.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RouletteWheelSelection<T> {
    private ScoreProvider<T>[] scoreProviders;
    private List<T> elements;
    private double[] totals;
    private List<Selector<T>> selectors = new ArrayList<>();

    public RouletteWheelSelection(ScoreProvider<T>... scoreProviders) {
        this.scoreProviders = scoreProviders;
        this.totals = new double[scoreProviders.length];
        for (int i = 0; i < scoreProviders.length; i++) {
            selectors.add(new Selector<>(this, i));
        }
    }

    public RouletteWheelSelection(List<T> elements, ScoreProvider<T>... scoreProviders) {
        this(scoreProviders);
        setElements(elements);
    }

    public Selector<T> getSelector(int scoreProviderIndex) {
        return selectors.get(scoreProviderIndex);
    }

    public Selector<T> addScoreProvider(ScoreProvider<T> scoreProvider) {
        int newIndex = scoreProviders.length;

        this.scoreProviders = Arrays.copyOf(scoreProviders, newIndex + 1);
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

    public interface ScoreProvider<T> {
        double getScore(T element);
    }

    private T pickElement(int scoreProviderIndex) {
        return elements.get(pickElementIndex(scoreProviderIndex));
    }

    private T pickAndRemoveElement(int scoreProviderIndex) {
        int i = pickElementIndex(scoreProviderIndex);
        return removeElementByIndex(i);
    }

    private T removeElementByIndex(int index) {
        T element = elements.remove(index);
        removeFromTotals(element);
        return element;
    }

    public void removeElement(T element) {
        elements.remove(element);
        removeFromTotals(element);
    }

    private void removeFromTotals(T element) {
        for (int i = 0; i < totals.length; i++) {
            totals[i] -= scoreProviders[i].getScore(element);
        }
    }

    private void addToTotals(T element) {
        for (int i = 0; i < totals.length; i++) {
            totals[i] += scoreProviders[i].getScore(element);
        }
    }

    public List<T> getElements() {
        return Collections.unmodifiableList(elements);
    }

    public void notifyValuesChanged() {
        if (elements != null) {
            for (int i = 0; i < totals.length; i++) {
                totals[i] = 0;
            }
            for (T element : elements) {
                for (int scoreProviderIndex = 0; scoreProviderIndex < totals.length; scoreProviderIndex++) {
                    totals[scoreProviderIndex] += scoreProviders[scoreProviderIndex].getScore(element);
                }
            }
        }
    }

    private int pickElementIndex(int scoreProviderIndex) {
        double total = totals[scoreProviderIndex];
        ScoreProvider<T> scoreProvider = scoreProviders[scoreProviderIndex];
        double rand = Math.random() * total;
        double cum = 0;
        for (int i = 0; i < elements.size(); i++) {
            cum += scoreProvider.getScore(elements.get(i));
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

    public void setElements(List<T> elements) {
        this.elements = new ArrayList<>(elements);
        notifyValuesChanged();
    }

    public static class Selector<T> {
        private final int scoreProviderIndex;
        private RouletteWheelSelection<T> roulette;

        public Selector(RouletteWheelSelection<T> roulette, int scoreProviderIndex) {
            this.scoreProviderIndex = scoreProviderIndex;
            this.roulette = roulette;
        }

        public T pickAndRemoveElement() {
            return roulette.pickAndRemoveElement(scoreProviderIndex);
        }

        public T pickElement() {
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
    }
}
