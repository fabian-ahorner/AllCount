package com.bitflake.counter.algo.shared.current;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class SensorDataProvider {
    private SensorValueListener listener;
    private boolean isListening;
    private File file;
    private DataOutputStream out;
    private long time;

    public void setFile(File file) {
        this.file = file;
    }

    public final void startListening() {
        if (isListening)
            return;
        isListening = true;
        if (file != null) {
            try {
                file.getParentFile().mkdirs();
                out = new DataOutputStream(new FileOutputStream(file));
                time = System.currentTimeMillis();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        start();
    }

    public abstract void start();

    public final void stopListening() {
        if (!isListening)
            return;
        isListening = false;
        if (out != null) {
            try {
                out.close();
                out = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        stop();
    }

    public abstract void stop();

    public final void setValueListener(SensorValueListener l) {
        this.listener = l;
    }

    protected final void notifyValueChanged(int time, float[] values) {
        if (isListening) listener.onValueChanged(time, values);
        if (out != null) {
            try {
                out.writeInt((int) (System.currentTimeMillis() - this.time));
                for (float value : values) {
                    out.writeFloat(value);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isListening() {
        return isListening;
    }

    public void destroy() {
        stopListening();
    }
}
