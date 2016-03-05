package com.bitflake.counter.services;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.bitflake.counter.Constances;
import com.bitflake.counter.WearConnection;
import com.bitflake.counter.tools.BundleHelper;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.HashMap;

public class WearBroadcastProxyService extends WearableListenerService implements WearConnection {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        boolean isRunningOnWatch = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            isRunningOnWatch = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
        }
        String processId = "/" + (isRunningOnWatch ? "w" : "m");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                Bundle extras = dataMap.toBundle();
                String path = item.getUri().getPath();
                Log.d("my", this.toString() + " onDataChanged(" + item.getUri().getPath() + ")");
                if (!path.startsWith(processId)) {
                    path = path.substring(path.indexOf('/', 1));
                    if (path.startsWith(PREFIX_BROADCAST)) {
                        String action = path.substring(PREFIX_BROADCAST.length());
                        Intent intent = new Intent(action);
                        intent.putExtras(extras);
                        sendBroadcast(intent);
                    } else if (path.startsWith(PREFIX_START_SERVICE)) {
                        String service = path.substring(PREFIX_START_SERVICE.length());
                        try {
                            Class serviceClass = Class.forName(service);
                            Intent intent = new Intent(this, serviceClass);
                            intent.putExtras(extras);
                            startService(intent);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (path.startsWith("/sensor/acceleration")) {
                    Log.d("my", "received values");
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        String path = messageEvent.getPath();

        Log.d("my", this.toString() + " onMessageReceived(" + path + ")");

        if (path.startsWith("/w/") || path.startsWith("/m/")) {
            boolean isRunningOnWatch = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
                isRunningOnWatch = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
            }

            String processId = isRunningOnWatch ? "/w" : "/m";

            if (!path.startsWith(processId)) {
                Log.d("my", this + " " + path);
                Bundle bundle = BundleHelper.toBundle(messageEvent.getData());
                int i = path.indexOf('/', 1);
                if (i < 0)
                    return;
                path = path.substring(i);
                if (path.startsWith(PREFIX_BROADCAST)) {
                    String action = path.substring(PREFIX_BROADCAST.length());
                    Intent intent = new Intent(action);
                    intent.putExtras(bundle);
                    sendBroadcast(intent);
                    Log.d("my", this.toString() + " onDataChanged(" + messageEvent.getPath() + ")");
                } else if (path.startsWith(PREFIX_START_SERVICE)) {
                    String service = path.substring(PREFIX_START_SERVICE.length());
                    try {
                        Class serviceClass = Class.forName(service);
                        Intent intent = new Intent(this, serviceClass);
                        intent.putExtras(bundle);
                        startService(intent);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (path.startsWith(PREFIX_STOP_SERVICE)) {
                    String service = path.substring(PREFIX_STOP_SERVICE.length());
                    try {
                        Class serviceClass = Class.forName(service);
                        Intent intent = new Intent(this, serviceClass);
                        stopService(intent);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
