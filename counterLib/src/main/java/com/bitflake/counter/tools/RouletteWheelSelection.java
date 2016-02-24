package com.bitflake.counter.tools;

import java.util.List;

public class RouletteWheelSelection<T> {
    private List<T> elements;
    private double total;
    private int from;
    private int to;

    public interface ScoreProvider<T> {
        double getScore(T element);
    }

    private ScoreProvider<T> scoreProvider;

    public RouletteWheelSelection(ScoreProvider<T> scoreProvider) {
        this.scoreProvider = scoreProvider;
    }

    public RouletteWheelSelection(ScoreProvider<T> scoreProvider, List<T> elements) {
        this.scoreProvider = scoreProvider;
        setElements(elements);
    }

    public T pickElement() {
        return elements.get(pickElementIndex());
    }

    public T pickAndRemoveElement() {
        int i = pickElementIndex();
        T el = elements.remove(i);
        total -= scoreProvider.getScore(el);
        return el;
    }

    public List<T> getRemainingElements() {
        return elements;
    }

    public void notifyValuesChanged() {
        total = getTotal(from, to);
    }

    private int pickElementIndex() {
        return pickElementIndex(total, 0, elements.size());
    }

    private int pickElementIndex(double total, int from, int to) {
        double rand = Math.random() * total;
        double cum = 0;
        for (int i = from; i < to; i++) {
            cum += scoreProvider.getScore(elements.get(i));
            if (cum >= rand) {
                return i;
            }
        }
        throw new AssertionError("Dead code");
    }

    private double getTotal(int from, int to) {
        double total = 0;
        for (int i = from; i < to; i++) {
            total += scoreProvider.getScore(elements.get(i));
        }
        return total;
    }

    /**
     * Adds a new element. If the full list was not used "to" will be extended to the end.
     *
     * @param element
     */
    public void addElement(T element) {
        elements.add(element);

        // Check if full list was used
        to++;
        if (to == elements.size()) {
            total += scoreProvider.getScore(element);
        } else {
            to = elements.size();
            notifyValuesChanged();
        }
    }

    public void setFromTo(int from, int to) {
        assertFromTo(from, to);
        this.from = from;
        this.to = to;
        notifyValuesChanged();
    }

    public void setElements(List<T> elements, int from, int to) {
        assertFromTo(from, to);
        this.elements = elements;
        this.from = from;
        this.to = to;
        notifyValuesChanged();
    }

    private void assertFromTo(int from, int to) {
        if (from < 0)
            throw new AssertionError("from must be >=0");
        if (to > elements.size())
            throw new AssertionError("to must be <= elements.size()");
        if (from > to)
            throw new AssertionError("from must be < to");
    }

    public void setElements(List<T> elements) {
        this.elements = elements;
        this.from = 0;
        this.to = elements.size();
        notifyValuesChanged();
    }
}
