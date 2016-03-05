package com.bitflake.counter.sensors;

public abstract class SensorDataProvider {
    private SensorValueListener listener;
    private boolean isListening;

    public final void startListening() {
        if (isListening)
            return;
        isListening = true;
        start();
    }

    abstract void start();

    public final void stopListening() {
        if (!isListening)
            return;
        isListening = false;
        stop();
    }

    abstract void stop();

    public final void setValueListener(SensorValueListener l) {
        this.listener = l;
    }

    protected final void notifyValueChanged(float[] values) {
        listener.onValueChanged(values);
    }

    public boolean isListening() {
        return isListening;
    }

    public void destroy() {
        stopListening();
    }
}
