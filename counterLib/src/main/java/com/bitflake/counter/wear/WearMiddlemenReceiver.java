package com.bitflake.counter.wear;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WearMiddlemenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("my", this.toString() + " WearMiddlemenReceiver(" + intent.getAction() + ")");
        WearConnectionManager.getInstance().sendBroadcast(intent);
    }
}
