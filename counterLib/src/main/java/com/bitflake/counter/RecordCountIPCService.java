package com.bitflake.counter;

import android.os.Bundle;
import android.os.Message;

public abstract class RecordCountIPCService extends MessengerService {

    public static final int MSG_START_RECORDING = 0;
    public static final int MSG_STOP_RECORDING = 1;
    public static final int MSG_START_COUNTING = 2;
    public static final int MSG_STOP_COUNTING = 3;

    public static final String MSG_DATA_RECORD_DELAY = "delay";
    public static final String MSG_DATA_RECORD_DURATION = "delay";

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch (msg.what) {
            case MSG_START_RECORDING:
                startRecording(data.getLong(MSG_DATA_RECORD_DELAY), data.getLong(MSG_DATA_RECORD_DURATION));
                break;
            case MSG_STOP_RECORDING:
                stopRecording();
                break;
            case MSG_START_COUNTING:
                startCounting(data);
                break;
            case MSG_STOP_COUNTING:
                stopCounting();
                break;
        }
    }

    protected abstract void stopCounting();

    protected abstract void startCounting(Bundle data);

    protected abstract void stopRecording();

    protected abstract void startRecording(long aLong, long aLong1);
}
