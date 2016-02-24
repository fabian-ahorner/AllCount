package com.bitflake.allcount;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bitflake.counter.services.CountService;

public class MobileCountService extends CountService implements TextToSpeech.OnInitListener {
    private static final int ONGOING_NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;
    private TextToSpeech tts;
    private boolean ttsInit;

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
        if (data.getBoolean(DATA_SHOULD_TALK)) {
            startTalking();
        }
    }

    private Notification createNotification(int count) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(com.bitflake.counter.R.drawable.ic_media_pause)
                        .setContentTitle(getString(com.bitflake.counter.R.string.counting))
                        .setContentText(String.valueOf(count));
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
        speak(String.valueOf(count));
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START_TALKING:
                startTalking();
                return true;
            case MSG_STOP_TALKING:
                stopTalking();
                return true;
            default:
                return super.handleMessage(msg);
        }
    }

    private void stopTalking() {
        ttsInit = false;
        tts.shutdown();
        tts = null;
    }

    private void startTalking() {
        if (!ttsInit)
            tts = new TextToSpeech(this, this);
    }

    @Override
    public void onInit(int status) {
        ttsInit = status == TextToSpeech.SUCCESS && tts != null;
        if (ttsInit && isListening())
            speak(com.bitflake.counter.R.string.counting);
    }

    public void speak(String text) {
        if (ttsInit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, text);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void speak(int text) {
        speak(getString(text));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTalking();
    }
}
