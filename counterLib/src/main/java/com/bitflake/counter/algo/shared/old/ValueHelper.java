package com.bitflake.counter.algo.shared.old;

public abstract class ValueHelper {
    private double value;
    private boolean hasValue = false;

    public void addValue(double value) {
        if (hasValue) {
            this.value = calculate(this.value, value);
        } else {
            this.value = value;
            hasValue = true;
        }
    }

    public void clear() {
        this.value = 0;
        hasValue = false;
    }

//    public float getFloat() {
//        return (float) value;
//    }

    public double getValue() {
        return value;
    }

    protected abstract double calculate(double oldValue, double newValue);

//    public void addValue(ValueHelper value) {
//        if (value.hasValue)
//            addValue(value.value);
//    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean hasValue() {
        return hasValue;
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

    public static class Sum extends ValueHelper {
        @Override
        protected double calculate(double oldValue, double newValue) {
            return oldValue + newValue;
        }
    }

    public static class Mean extends Sum {
        private int valueCount;

        @Override
        public void addValue(double value) {
            super.addValue(value);
            valueCount++;
        }

        @Override
        public void clear() {
            super.clear();
            valueCount = 0;
        }

        @Override
        public double getValue() {
            return super.getValue() / valueCount;
        }
    }

    public static class Variance extends Mean {
        private double mean;

        public void setMean(double mean) {
            clear();
            this.mean = mean;
        }

        @Override
        public void addValue(double value) {
            super.addValue(Math.pow(value - mean, 2));
        }
    }

    public static class StandardDeviation extends Variance {
        @Override
        public double getValue() {
            return Math.sqrt(super.getValue());
        }
    }


}
