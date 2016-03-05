package com.bitflake.counter.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bitflake.counter.Constances;

public class RecordServiceHelper extends ServiceHelper implements RecordConstants {
    private RecordEventListener listener;

    public RecordServiceHelper(Context context) {
        super(context, Constances.INTENT_RECORD_CONTROL);
    }

    public void startRecording(int delay, int duration) {
        Intent i = createControlIntent();
        i.putExtra(DATA_COMMAND, CMD_START_RECORDING);
        i.putExtra(DATA_DELAY_MS, delay);
        i.putExtra(DATA_DURATION_MS, duration);
        sendBroadcast(i);
    }

    private void sendControlSignal(String cmd) {
        Intent i = createControlIntent();
        i.putExtra(DATA_COMMAND, cmd);
        sendBroadcast(i);
    }

    public void stopRecording() {
        sendControlSignal(CMD_STOP_RECORDING);
    }

    public void skipState() {
        sendControlSignal(CMD_SKIP);
    }

    public void enableEventListener(RecordEventListener listener) {
        this.listener = listener;
        super.enableBroadCastListener(Constances.INTENT_RECORD_STATUS);
    }

    public void disableEventListener() {
        listener = null;
        super.disableBroadcastListener();
    }

    @Override
    protected void onReceiveBroadcast(Intent intent) {
        if (listener == null)
            return;
        Bundle data = intent.getExtras();
        listener.onBroadcastReceived(data);
        String event = data.getString(DATA_EVENT_TYPE);
        switch (event) {
            case EVENT_FINISHED_RECORDING:
                listener.onFinishedRecording(data);
                break;
            case EVENT_START_DELAY:
                listener.onStartDelay(data);
                break;
            case EVENT_START_RECORDING:
                listener.onStartRecording(data);
                break;
            case EVENT_STATUS:
                listener.onStatusReceived(data);
                break;
            case EVENT_STOP_RECORDING:
                listener.onStopRecording(data);
                break;
        }
        super.onReceiveBroadcast(intent);
    }

    public interface RecordEventListener {

        void onStatusReceived(Bundle data);

        void onFinishedRecording(Bundle data);

        void onStartDelay(Bundle data);

        void onStartRecording(Bundle data);

        void onStopRecording(Bundle data);

        void onBroadcastReceived(Bundle data);
    }
}
