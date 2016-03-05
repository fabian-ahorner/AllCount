package com.bitflake.counter.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

public class BroadcastReceiverService extends Service {
    private IBinder binder=new Binder();

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

    public void onReceive(Intent intent) {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
