package com.bitflake.counter.algo.shared.used.tools;

import java.util.Arrays;

public abstract class ArrayValueHelper {
    private final double[] values;
    private boolean hasValue;

    public ArrayValueHelper(double... values) {
        this.values = values;
    }

    public void addValues(double... values) {
        for (int i = 0; i < this.values.length; i++) {
            addValue(values[i], i);
        }
        hasValue = true;
    }

    protected void addValue(double value, int i) {
        if (hasValue) {
            this.values[i] = calculate(this.values[i], value);
        } else {
            this.values[i] = value;
        }
    }

    public void clear() {
        for (int i = 0; i < this.values.length; i++) {
            this.values[i] = 0;
        }
        hasValue = false;
    }

    public double[] getValues(double[] values) {
        for (int i = 0; i < values.length; i++) {
            values[i] = getValue(i);
        }
        return values;
    }

    public double getValue(int i) {
        return values[i];
    }

    protected abstract double calculate(double oldValue, double newValue);

    public void addValues(ArrayValueHelper values) {
        addValues(values.values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }

    public static class Min extends ArrayValueHelper {
        private double bias;

        public Min(double... values) {
            super(values);
        }

        public void setBias(double bias) {
            this.bias = bias;
        }

        @Override
        protected double calculate(double oldValue, double newValue) {
            return Math.min(oldValue, newValue + bias);
        }
    }

    public static class Max extends ArrayValueHelper {
        private double bias;

        public Max(double... values) {
            super(values);
        }

        public void setBias(double bias) {
            this.bias = bias;
        }

        @Override
        protected double calculate(double oldValue, double newValue) {
            return Math.max(oldValue, newValue + bias);
        }
    }

    public static class Sum extends ArrayValueHelper {
        public Sum(double... values) {
            super(values);
        }

        @Override
        protected double calculate(double oldValue, double newValue) {
            return oldValue + newValue;
        }
    }

    public static class Mean extends Sum {
        private int valueCount;

        public Mean(double... values) {
            super(values);
        }

        @Override
        public void addValues(double... value) {
            super.addValues(value);
            valueCount++;
        }

        @Override
        public void clear() {
            super.clear();
            valueCount = 0;
        }

        @Override
        public double getValue(int i) {
            return super.getValue(i) / valueCount;
        }
    }

    public static class Variance extends Mean {
        private double[] mean;

        public Variance(double... values) {
            super(values);
        }

        public void setMean(double... mean) {
            clear();
            this.mean = mean;
        }

        @Override
        protected void addValue(double value, int i) {
            super.addValue(Math.pow(value - mean[i], 2), i);
        }
    }

    public static class StandardDeviation extends Variance {
        public StandardDeviation(double... values) {
            super(values);
        }

        @Override
        public double getValue(int i) {
            return Math.sqrt(super.getValue(i));
        }
    }

    public static double[] add(double[] values, double value) {
        double[] result = new double[values.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = values[i] + value;
        }
        return result;
    }
}
