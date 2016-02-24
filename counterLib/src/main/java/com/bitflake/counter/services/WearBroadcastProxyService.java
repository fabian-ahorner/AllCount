package com.bitflake.counter.services;

import android.content.Intent;
import android.os.Bundle;

import com.bitflake.counter.WearConnection;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.HashMap;

public class WearBroadcastProxyService extends WearableListenerService implements WearConnection {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                Bundle extras = dataMap.toBundle();
                String path = item.getUri().getPath();
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
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }
}
