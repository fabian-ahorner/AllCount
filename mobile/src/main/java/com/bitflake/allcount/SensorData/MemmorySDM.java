package com.bitflake.allcount.SensorData;

import android.util.SparseArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class MemmorySDM extends SensorDataManager {
    SparseArray<ByteArrayOutputStream> outs= new SparseArray<>();

    @Override
    protected InputStream openIn(int sensorIndex) {
        ByteArrayOutputStream out = outs.get(sensorIndex);
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    protected OutputStream openOut(int sensorIndex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        outs.put(sensorIndex,out);
        return out;
    }
}
