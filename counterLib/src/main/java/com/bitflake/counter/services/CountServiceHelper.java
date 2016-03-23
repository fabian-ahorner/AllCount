package com.bitflake.counter.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bitflake.counter.Constances;

public class CountServiceHelper extends ServiceHelper implements CountConstants {
    private CountEventListener listener;

    public CountServiceHelper(Context context) {
        super(context, Constances.INTENT_COUNT_CONTROL);
    }

//    public void startServiceAndCounting(Bundle states, int countOffset, boolean shouldTalk) {
//        Intent i = new Intent(getContext(),
//                WearCountService.class);
//        i.putExtra(DATA_COMMAND, CMD_START_COUNTING);
//        i.putExtra(DATA_STATES, states);
//        i.putExtra(DATA_COUNT_OFFSET, countOffset);
//        i.putExtra(DATA_SHOULD_TALK, shouldTalk);
//        getContext().startService(i);
//    }


    public void stopCounting() {
        sendControlSignal(CMD_STOP_COUNTING);
    }

    public void requestUpdate() {
        sendControlSignal(CMD_REQUEST_UPDATE);
    }

    private void sendControlSignal(String cmd) {
        Intent i = createControlIntent();
        i.putExtra(DATA_COMMAND, cmd);
        sendBroadcast(i);
    }

    public void startCounting(String baseIntent, Bundle states) {
        startCounting(states, 0);
    }

    public void startCounting(Bundle states, int countOffset) {
        Intent i = createControlIntent();//new Intent(getContext(), CountService.class);//
        i.putExtra(DATA_COMMAND, CMD_START_COUNTING);
        i.putExtra(DATA_STATES, states);
        i.putExtra(DATA_COUNT_OFFSET, countOffset);
//        getContext().startService(i);
        sendBroadcast(i);
    }

    public void enabelEventListener(CountEventListener listener) {
        this.listener = listener;
        super.enableBroadCastListener(Constances.INTENT_COUNT_STATUS);
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
        String event = data.getString(DATA_EVENT_TYPE);
        switch (event) {
            case EVENT_COUNT:
                listener.onCount(data);
                break;
            case EVENT_RESET:
                listener.onReset(data);
                break;
            case EVENT_START_COUNTING:
                listener.onStartCounting(data);
                break;
            case EVENT_STATUS:
                listener.onStatusReceived(data);
                break;
            case EVENT_STOP_COUNTING:
                listener.onStopCounting(data);
                break;
        }
        super.onReceiveBroadcast(intent);
    }

    public interface CountEventListener {

        void onCount(Bundle data);

        void onReset(Bundle data);

        void onStartCounting(Bundle data);

        void onStatusReceived(Bundle data);

        void onStopCounting(Bundle data);
    }
}
