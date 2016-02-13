package com.bitflake.counter.services;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.bitflake.counter.SensorCounter;
import com.bitflake.counter.SensorService;
import com.bitflake.counter.StateWindow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CountService extends SensorService implements SensorCounter.CountListener, CountServiceHelper.Constants {

    private SensorCounter counter = new SensorCounter();
    private Set<Messenger> statusListeners = new HashSet<>();
    private Bundle statusBundle = new Bundle();

    @Override
    public void onCreate() {
        super.onCreate();
        setAnalyser(counter);
        counter.setCountListener(this);
        statusBundle.putFloat(DATA_COUNT_PROGRESS, 0);
        statusBundle.putInt(DATA_COUNT, 0);
        statusBundle.putBoolean(DATA_IS_COUNTING, false);
    }

    @Override
    public void handleMessage(Message msg) {
        Bundle data = msg.getData();
        switch (msg.what) {
            case MSG_START_COUNTING:
                startCounting(data);
                sendStatusBundle(MSG_RESP_STATUS, msg.replyTo);
                statusListeners.add(msg.replyTo);
                break;
            case MSG_STOP_COUNTING:
                stopCounting();
                break;
            case MSG_START_LISTENING:
                sendStatusBundle(MSG_RESP_STATUS, msg.replyTo);
                statusListeners.add(msg.replyTo);
                break;
            case MSG_STOP_LISTENING:
                statusListeners.remove(msg.replyTo);
                break;
            case MSG_RESET_COUNTER:
                counter.reset();
                break;
        }
    }

    private void stopCounting() {
        stopListening();
        statusBundle.putBoolean(DATA_IS_COUNTING, false);
        sendStatusBundle(MSG_RESP_STATUS);
    }

    private void startCounting(Bundle data) {
        if (isListening())
            return;
        statusBundle.putBoolean(DATA_IS_COUNTING, true);
        sendStatusBundle(MSG_RESP_STATUS);
        List<StateWindow> states = StateWindow.fromBundles(data.getBundle(DATA_STATES));
        counter.setStates(states);
        startListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCounting();
    }

    @Override
    public void onCount(int count) {
        statusBundle.putInt(DATA_COUNT, count);
        statusBundle.putFloat(DATA_COUNT_PROGRESS, 0.5f);
        sendStatusBundle(MSG_RESP_COUNT);
    }

    @Override
    public void onCountProgress(float progress) {
        statusBundle.putFloat(DATA_COUNT_PROGRESS, progress);
        sendStatusBundle(MSG_RESP_COUNT_PROGRESS);
    }

    public void sendStatusBundle(int what) {
        Iterator<Messenger> it = statusListeners.iterator();
        while (it.hasNext()) {
            if (!sendStatusBundle(what, it.next()))
                it.remove();
        }
    }

    public boolean sendStatusBundle(int what, Messenger m) {
        Message msg = Message.obtain(null,
                what, 0, 0);
        msg.setData(statusBundle);
        return sendMessage(m, msg);
    }
}
