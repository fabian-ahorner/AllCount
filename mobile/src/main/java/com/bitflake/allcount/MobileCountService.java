package com.bitflake.allcount;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bitflake.counter.services.WearCountService;

public class MobileCountService extends WearCountService {
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void startCounting(Bundle data) {
        super.startCounting(data);
        startForeground(ONGOING_NOTIFICATION_ID, createNotification(countOffset));
    }

    @Override
    protected void stopCounting() {
        super.stopCounting();
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
    }

    private Notification createNotification(int count) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.bitflake.counter.R.drawable.ic_media_pause)
                        .setContentTitle(String.valueOf(count))
                        .setOngoing(true)
                        .setContentText(getString(com.bitflake.counter.R.string.counting));
        Intent resultIntent = CountActivity.getStartIntent(this, currentStates, false, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(CountActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        return mBuilder.build();
    }

    @Override
    public void onCount(int count) {
        super.onCount(count);
        count += countOffset;
        notificationManager.notify(ONGOING_NOTIFICATION_ID, createNotification(count));
    }
}
