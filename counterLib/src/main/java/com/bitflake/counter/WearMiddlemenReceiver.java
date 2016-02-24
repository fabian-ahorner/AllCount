package com.bitflake.counter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bitflake.counter.services.WearConnectionManager;

public class WearMiddlemenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WearConnectionManager.getInstance().sendBroadcast(intent);
    }
}
