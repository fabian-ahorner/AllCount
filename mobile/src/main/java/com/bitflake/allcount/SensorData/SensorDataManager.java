package com.bitflake.allcount.SensorData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class SensorDataManager {
    private int sensorCount;

    public final DataOutputStream openSensorOutputStream(int sensorIndex) {
        this.sensorCount = Math.max(sensorCount, sensorIndex);
        return new DataOutputStream(openOut(sensorIndex));
    }

    public final DataInputStream openSensorInputStream(int sensorIndex) {
        return new DataInputStream(openIn(sensorIndex));
    }

    protected abstract InputStream openIn(int sensorIndex);

    protected abstract OutputStream openOut(int sensorIndex);

    public int getSensorCount() {
        return sensorCount;
    }
}
