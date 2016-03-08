package com.bitflake.counter.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Messenger;

import com.bitflake.counter.Constances;
import com.bitflake.counter.StateExtractor;
import com.bitflake.counter.StateWindow;

import java.util.HashSet;
import java.util.Set;

public class RecordService extends SensorService implements RecordConstants {
    private Handler handler = new Handler();
    private StateExtractor stateExtractor = new StateExtractor();
    protected int status = STATUS_NONE;
    int delay;
    int duration;
    private Runnable rStartRecording = new Runnable() {
        @Override
        public void run() {
            startRecording();
        }
    };
    private Runnable rFinishRecording = new Runnable() {
        @Override
        public void run() {
            finishRecording();
        }
    };
    private Bundle states;

    @Override
    public void onCreate() {
        super.onCreate();
        setAnalyser(stateExtractor);
        super.registerReceiver(new IntentFilter(Constances.INTENT_RECORD_CONTROL));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            onReceive(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onReceive(Intent intent) {
        Bundle data = intent.getExtras();
        if (data == null)
            return;
        String cmd = data.getString(DATA_COMMAND);
        switch (cmd) {
            case CMD_START_RECORDING:
                startDelay(data);
                break;
            case CMD_STOP_RECORDING:
                stopRecording();
                stopSelf();
                break;
            case CMD_SKIP:
                skipState();
                break;
            case CMD_REQUEST_UPDATE:
                broadcastStatus(EVENT_STATUS);
                break;
            default:
                break;
        }
    }

    private void skipState() {
        if (status == STATUS_DELAY) {
            handler.removeCallbacks(rStartRecording);
            startRecording();
        } else if (status == STATUS_RECORDING) {
            handler.removeCallbacks(rStartRecording);
            finishRecording();
        }
    }

    private void stopRecording() {
        stopListening();
        this.status = STATUS_NONE;
        this.states = null;
        broadcastStatus(EVENT_STOP_RECORDING);
        handler.removeCallbacks(rStartRecording);
        handler.removeCallbacks(rFinishRecording);
    }

    private void startDelay(Bundle data) {
        if (status == STATUS_NONE || status == STATUS_FINISHED) {
            this.delay = data.getInt(DATA_DELAY_MS);
            this.duration = data.getInt(DATA_DURATION_MS);
            states = null;
            this.status = STATUS_DELAY;
            if (delay == 0) {
                startRecording();
            } else {
                broadcastStatus(EVENT_START_DELAY, delay);
                handler.postDelayed(rStartRecording, delay);
            }
        }
    }

    void startRecording() {
        if (status == STATUS_RECORDING)
            return;
        states = null;
        status = STATUS_RECORDING;
        stateExtractor.clear();
        broadcastStatus(EVENT_START_RECORDING, duration);
        handler.postDelayed(rFinishRecording, duration);
        startListening();
    }

    void finishRecording() {
        if (status != STATUS_RECORDING)
            return;
        stopListening();
        status = STATUS_FINISHED;
        stateExtractor.compressStates();
        this.states = StateWindow.toBundle(stateExtractor.getStates());
        broadcastStates();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
        unregisterReceiver();
    }

    public void broadcastStatus(String eventType) {
        sendBroadcast(createIntent(eventType));
    }

    public void broadcastStatus(String eventType, int duration) {
        Intent i = createIntent(eventType);
        i.putExtra(DATA_DURATION_MS, duration);
        sendBroadcast(i);
    }

    public void broadcastStates() {
        Intent i = createIntent(EVENT_FINISHED_RECORDING);
        i.putExtra(DATA_STATES, states);
        sendBroadcast(i);
    }

    public Intent createIntent(String eventType) {
        Intent i = new Intent(Constances.INTENT_RECORD_STATUS);
        i.putExtra(DATA_EVENT_TYPE, eventType);
        i.putExtra(DATA_STATUS, status);
        return i;
    }
}
