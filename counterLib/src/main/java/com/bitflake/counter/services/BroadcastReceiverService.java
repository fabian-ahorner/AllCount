package com.bitflake.counter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public abstract class BroadcastReceiverService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void registerReceiver(IntentFilter... filters) {
        for (IntentFilter filter : filters) {
            registerReceiver(receiver, filter);
        }
    }

    public void unregisterReceiver() {
        super.unregisterReceiver(receiver);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BroadcastReceiverService.this.onReceive(intent);
        }
    };

    private void onReceive(Intent intent) {

    }
}
