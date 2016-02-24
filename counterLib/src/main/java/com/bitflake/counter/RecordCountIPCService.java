package com.bitflake.counter;

import android.os.Bundle;
import android.os.Message;

import com.bitflake.counter.services.MessengerService;

public abstract class RecordCountIPCService extends MessengerService {

    public static final int MSG_START_RECORDING = 0;
    public static final int MSG_STOP_RECORDING = 1;
    public static final int MSG_START_COUNTING = 2;
    public static final int MSG_STOP_COUNTING = 3;

    public static final String MSG_DATA_RECORD_DELAY = "delay";
    public static final String MSG_DATA_RECORD_DURATION = "delay";

    @Override
    public boolean handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch (msg.what) {
            case MSG_START_RECORDING:
                startRecording(data.getLong(MSG_DATA_RECORD_DELAY), data.getLong(MSG_DATA_RECORD_DURATION));
                return true;
            case MSG_STOP_RECORDING:
                stopRecording();
                return true;
            case MSG_START_COUNTING:
                startCounting(data);
                return true;
            case MSG_STOP_COUNTING:
                stopCounting();
                return true;
            default:
                return super.handleMessage(msg);
        }
    }

    protected abstract void stopCounting();

    protected abstract void startCounting(Bundle data);

    protected abstract void stopRecording();

    protected abstract void startRecording(long aLong, long aLong1);
}
