package com.bitflake.counter.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import com.bitflake.counter.StateExtractor;
import com.bitflake.counter.StateWindow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RecordService extends SensorService implements RecordServiceHelper.Constants {
    private Handler handler = new Handler();
    private StateExtractor stateExtractor = new StateExtractor();
    private Set<Messenger> statusListeners = new HashSet<>();
    private int status = STATUS_NONE;
    private int delay;
    private int duration;
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
    }

    @Override
    public boolean handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch (msg.what) {
            case MSG_START_RECORDING:
                startDelay(data);
                statusListeners.add(msg.replyTo);
                return true;
            case MSG_STOP_RECORDING:
                stopRecording();
                return true;
            case MSG_SKIP_STATE:
                skipState();
                return true;
            case MSG_START_LISTENING:
                sendStatus(MSG_RESP_STATUS, msg.replyTo, 0);
                statusListeners.add(msg.replyTo);
                return true;
            case MSG_STOP_LISTENING:
                statusListeners.remove(msg.replyTo);
                return true;
            default:
                return super.handleMessage(msg);
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
        sendStatus(MSG_RESP_STOPPED_RECORDING, 0);
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
                sendStatus(MSG_RESP_START_DELAY, delay);
                handler.postDelayed(rStartRecording, delay);
            }
        }
    }

    private void startRecording() {
        if (status == STATUS_RECORDING)
            return;
        states = null;
        status = STATUS_RECORDING;
        stateExtractor.clear();
        sendStatus(MSG_RESP_START_RECORDING, duration);
        handler.postDelayed(rFinishRecording, duration);
        startListening();
    }

    private void finishRecording() {
        if (status != STATUS_RECORDING)
            return;
        stopListening();
        status = STATUS_FINISHED;
        stateExtractor.compressStates();
        this.states = StateWindow.toBundle(stateExtractor.getStates());
        sendStatus(MSG_RESP_FINISHED_RECORDING, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording();
    }

    public void sendStatus(int what, int duration) {
        Iterator<Messenger> it = statusListeners.iterator();
        while (it.hasNext()) {
            if (!sendStatus(what, it.next(), duration))
                it.remove();
        }
    }

    public boolean sendStatus(int what, Messenger m, int duration) {
        Message msg = Message.obtain(null,
                what, status, duration);
        msg.setData(states);
        return sendMessage(m, msg);
    }
}
