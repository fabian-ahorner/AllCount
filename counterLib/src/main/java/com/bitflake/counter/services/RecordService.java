package com.bitflake.counter.services;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import com.bitflake.counter.Constances;
import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.algo.shared.current.record.EventExtractor;
import com.bitflake.counter.algo.shared.current.record.StateCollector;
import com.bitflake.counter.algo.shared.current.record.StateExtractor;
import com.bitflake.counter.tools.CountStateHelper;

import java.io.File;
import java.util.List;

public class RecordService extends SensorService implements RecordConstants, EventExtractor.RecordingStatusListener {
    private Handler handler = new Handler();
    //    private StateExtractor stateExtractor = new StateExtractor();
    private StateCollector stateCollector = new StateCollector();
    private EventExtractor eventExtractor = new EventExtractor(stateCollector, this);
    protected int status = STATUS_NONE;
    long delay;
    long duration;
    private Runnable rStartRecording = new Runnable() {
        @Override
        public void run() {
            startRecording();
        }
    };
    private Runnable rFinishRecording = new Runnable() {
        @Override
        public void run() {
            onFinishedRecording();
        }
    };
    private Bundle states;
    private long timestamp;

    @Override
    public void onCreate() {
        super.onCreate();
        setAnalyser(stateCollector);
        super.registerReceiver(new IntentFilter(INTENT_RECORD_CONTROL));
        sensor.setFile(new File(getCacheDir(), Constances.DATA_FILE_COUNT));
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
            onFinishedRecording();
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
                broadcastStatus(EVENT_START_DELAY);
                handler.postDelayed(rStartRecording, delay);
                updateTimestamp(delay);
            }
        }
    }

    private void updateTimestamp(long duration) {
        timestamp = System.currentTimeMillis() + duration;
    }

    void startRecording() {
        if (status == STATUS_RECORDING)
            return;
        states = null;
        status = STATUS_RECORDING;
        stateCollector.clear();
        eventExtractor.clear();
        setAnalyser(eventExtractor);
        window.resetWindow();
        broadcastStatus(EVENT_START_CALIBRATING);
        updateTimestamp(duration);
        startListening();
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

    public void broadcastStates() {
        Intent i = createIntent(EVENT_FINISHED_RECORDING);
        i.putExtra(DATA_STATES, states);
        sendBroadcast(i);
    }

    public Intent createIntent(String eventType) {
        Intent i = new Intent(INTENT_RECORD_STATUS);
        i.putExtra(DATA_EVENT_TYPE, eventType);
        i.putExtra(DATA_STATUS, status);
        if (status == STATUS_DELAY || status == STATUS_RECORDING) {
            i.putExtra(DATA_REMAINING_TIME, getRemainingTime());
            i.putExtra(DATA_DURATION_MS, status == STATUS_DELAY ? delay : duration);
        }
        return i;
    }

    public long getRemainingTime() {
        return Math.max(0, timestamp - System.currentTimeMillis());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        broadcastStatus(EVENT_STATUS);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStartRecording(double[] startingPos) {
        broadcastStatus(EVENT_START_MOVING);
    }

    @Override
    public void onFinishedRecording() {
        if (status != STATUS_RECORDING)
            return;
        stopListening();
        status = STATUS_FINISHED;
        List<CountState> compressedStates = StateExtractor.compressStates(stateCollector.getStates());
        this.states = CountStateHelper.toBundle(compressedStates);
        broadcastStates();
    }

    @Override
    public void onNewState(CountState states) {
        Intent i = new Intent(INTENT_RECORD_PROGRESS);
        i.putExtra(DATA_LAST_STATE, CountStateHelper.toJSON(states));
        sendBroadcast(i);
    }

    @Override
    public void onIsStill(float stillness) {
        Intent i = new Intent(INTENT_RECORD_PROGRESS);
        i.putExtra(DATA_STILLNESS, stillness);
        sendBroadcast(i);
    }

    public Bundle getStates() {
        return states;
    }
}
