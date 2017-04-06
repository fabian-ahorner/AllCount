package com.bitflake.allcount;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.bitflake.counter.Constances;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.RecordConstants;
import com.bitflake.counter.services.WearBroadcastProxyService;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;

public class NotificationReceiver extends WearBroadcastProxyService {

    private static final int ONGOING_NOTIFICATION_ID = 1;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);


        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                String path = item.getUri().getPath();
                if (CountConstants.WEAR_STATUS_PATH.equals(path)) {
                    updateCountNotification(dataMap);
                } else if (RecordConstants.WEAR_STATUS_PATH.equals(path)) {
                    updateRecordNotification(dataMap);
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    private void updateRecordNotification(DataMap dataMap) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        int status = dataMap.getInt(RecordConstants.DATA_STATUS);
        if (status == RecordConstants.STATUS_DELAY || status == RecordConstants.STATUS_RECORDING) {
            notificationManager.notify(ONGOING_NOTIFICATION_ID, getRecordNotification(status));
        } else {
            notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        }
    }

    private Notification getRecordNotification(int status) {

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_media_pause)
                        .setContentTitle(getString(R.string.recording))
                        .setContentText(getString(com.bitflake.counter.R.string.counting))
                        .setOngoing(true);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        extender.addAction(getCountAction(CountConstants.CMD_STOP_COUNTING, android.R.drawable.ic_media_pause, getString(R.string.stop_counting)));

        switch (status) {
            case RecordConstants.STATUS_DELAY:
                notificationBuilder.setContentText(getString(R.string.get_ready));
                break;
            case RecordConstants.STATUS_RECORDING:
                notificationBuilder.setContentText(getString(R.string.get_ready));
                break;
            case RecordConstants.STATUS_FINISHED:
            case RecordConstants.STATUS_NONE:
                return null;
        }
        extender.addAction(getRecordAction(RecordConstants.CMD_SKIP, android.R.drawable.ic_media_next, getString(R.string.skip)));
        extender.addAction(getRecordAction(RecordConstants.CMD_STOP_RECORDING, android.R.drawable.ic_media_pause, getString(R.string.stop_recording)));
        return notificationBuilder
                .extend(extender)
                .build();
    }

    private void updateCountNotification(DataMap dataMap) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        boolean isCounting = dataMap.getBoolean(CountConstants.DATA_IS_COUNTING);
        int count = dataMap.getInt(CountConstants.DATA_COUNT);
        if (isCounting) {
            Notification not = getCountingNotification(count);
            notificationManager.notify(ONGOING_NOTIFICATION_ID, not);
        } else {
            notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        }
    }

    private Notification getCountingNotification(int count) {

        // Create builder for the main notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.bitflake.counter.R.drawable.ic_media_pause)
                        .setContentTitle(String.valueOf(count))
                        .setContentText(getString(com.bitflake.counter.R.string.counting))
                        .setOngoing(true);

        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        extender.addAction(getCountAction(CountConstants.CMD_STOP_COUNTING, android.R.drawable.ic_media_pause, "Stop counting"));

// Extend the notification builder with the second page
        return notificationBuilder
                .extend(extender)
                .build();
    }

    public NotificationCompat.Action getCountAction(String command, int icon, String title) {
        Intent actionIntentPause = new Intent(Constances.INTENT_COUNT_CONTROL);
        actionIntentPause.putExtra(CountConstants.DATA_COMMAND, command);
        PendingIntent actionPendingIntent =
                PendingIntent.getBroadcast(this, 0, actionIntentPause,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return
                new NotificationCompat.Action.Builder(icon,
                        title, actionPendingIntent)
                        .build();
    }

    public NotificationCompat.Action getRecordAction(String command, int icon, String title) {
        Intent actionIntentPause = new Intent(RecordConstants.INTENT_RECORD_CONTROL);
        actionIntentPause.putExtra(RecordConstants.DATA_COMMAND, command);
        PendingIntent actionPendingIntent =
                PendingIntent.getBroadcast(this, 0, actionIntentPause,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        return
                new NotificationCompat.Action.Builder(icon,
                        title, actionPendingIntent)
                        .build();
    }
}
