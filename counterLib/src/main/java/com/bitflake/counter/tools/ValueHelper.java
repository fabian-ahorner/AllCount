package com.bitflake.counter.tools;

public abstract class ValueHelper {
    private double value;
    private boolean hasValue = false;

    public void addValue(double value) {
        if (hasValue) {
            this.value = value;
            hasValue = true;
        } else
            this.value = calculate(this.value, value);
    }

    public void clear() {
        this.value = 0;
        this.hasValue = false;
    }

    public float getFloat() {
        return (float) value;
    }

    public double getValue() {
        return value;
    }

    protected abstract double calculate(double oldValue, double newValue);

    public void addValue(ValueHelper value) {
        if (value.hasValue)
            addValue(value.value);
    }

    public void setValue(float value) {
        this.value = value;
        hasValue = true;
    }

    public static class Min extends ValueHelper {
        @Override
        protected double calculate(double oldValue, double newValue) {
            return Math.min(oldValue, newValue);
        }
    }

    public static class Max extends ValueHelper {
        @Override
        protected double calculate(double oldValue, double newValue) {
            return Math.max(oldValue, newValue);
        }
    }
}
