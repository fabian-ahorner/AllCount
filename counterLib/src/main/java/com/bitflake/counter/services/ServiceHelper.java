package com.bitflake.counter.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.bitflake.counter.Constances;

public class ServiceHelper {
    private final Context context;
    private final String intent;

    protected Intent createControlIntent() {
        return new Intent(intent);
    }

    public ServiceHelper(Context context, String intent) {
        this.intent = intent;
        this.context = context;
    }

    protected void sendBroadcast(Intent i) {
        context.sendBroadcast(i);
    }

    protected Context getContext() {
        return context;
    }


    protected void enableBroadCastListener(String intentAction) {
        context.registerReceiver(receiver, new IntentFilter(intentAction));
    }

    protected void disableBroadcastListener() {
        context.unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(intent);
        }
    };

    protected void onReceiveBroadcast(Intent intent) {

    }
}
