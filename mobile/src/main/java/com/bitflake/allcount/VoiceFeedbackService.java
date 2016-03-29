package com.bitflake.allcount;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;

import com.bitflake.counter.Constances;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.RecordConstants;

public class VoiceFeedbackService extends Service implements TextToSpeech.OnInitListener {

    private static final String DATA_SAY = "say";
    private RecordReceiver recordReceiver = new RecordReceiver();
    private CountReceiver countReceiver = new CountReceiver();

    private static final int ONGOING_NOTIFICATION_ID = 1;
    protected NotificationManager notificationManager;
    private TextToSpeech tts;
    private boolean ttsInit;
    private IBinder binder = new Binder();

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(NOTIFICATION_SERVICE);
        startTalking();
        registerReceiver(countReceiver, new IntentFilter(Constances.INTENT_COUNT_STATUS));
        registerReceiver(recordReceiver, new IntentFilter(RecordConstants.INTENT_RECORD_STATUS));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String toSay = intent.getStringExtra(DATA_SAY);
            if (toSay != null)
                speak(toSay);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class RecordReceiver extends BroadcastReceiver implements RecordConstants {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(DATA_EVENT_TYPE);
            if (event == null)
                return;
            switch (event) {
                case EVENT_START_DELAY:
                    speak(R.string.get_ready);
                    break;
                case EVENT_START_CALIBRATING:
                    speak(R.string.start_calibrating);
                    break;
                case EVENT_FINISHED_RECORDING:
                    speak(R.string.stop_recording);
                    break;
                case EVENT_START_MOVING:
                    speak(R.string.start_recording);
                    break;
            }
        }
    }

    private class CountReceiver extends BroadcastReceiver implements CountConstants {
        @Override
        public void onReceive(Context context, Intent intent) {
            String event = intent.getStringExtra(DATA_EVENT_TYPE);
            if (event == null)
                return;
            int count = intent.getIntExtra(DATA_COUNT, 0);
            switch (event) {
                case EVENT_COUNT:
//                    showNotification(count, getTarget(intent));
                    speak(String.valueOf(count));
                    break;
                case EVENT_START_COUNTING:
                    speak(R.string.counting);
//                    showNotification(count, getTarget(intent));
                    break;
                case EVENT_STOP_COUNTING:
                    notificationManager.cancel(ONGOING_NOTIFICATION_ID);
                    stopSelf();
                    break;
            }
        }
    }

//    private Notification createNotification(int count, String target) {
////        NotificationCompat.Builder mBuilder =
////                new NotificationCompat.Builder(this)
////                        .setSmallIcon(com.bitflake.counter.R.drawable.ic_media_pause)
////                        .setContentTitle(getString(com.bitflake.counter.R.string.counting))
////                        .setContentText(String.valueOf(count))
////                        .setOngoing(true);
//        Intent resultIntent = CountActivity.getStartIntent(this, target);
//
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(CountActivity.class);
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
////        mBuilder.setContentIntent(resultPendingIntent);
//
//        Intent actionIntentPause = new Intent(Constances.INTENT_COUNT_CONTROL + Constances.INTENT_TARGET_MOBILE);
//        actionIntentPause.putExtra(CountConstants.DATA_COMMAND, CountConstants.CMD_STOP_COUNTING);
//        PendingIntent actionPendingIntent =
//                PendingIntent.getBroadcast(this, 0, actionIntentPause,
//                        PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Create the action
//        NotificationCompat.Action actionPause =
//                new NotificationCompat.Action.Builder(android.R.drawable.ic_media_pause,
//                        getString(R.string.pause), actionPendingIntent)
//                        .build();
//
//        // Create builder for the main notification
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(com.bitflake.counter.R.drawable.ic_media_pause)
//                        .setContentTitle(String.valueOf(count))
//                        .setContentText(getString(com.bitflake.counter.R.string.counting))
//                        .setOngoing(true)
//                        .setContentIntent(resultPendingIntent);
//
//// Create a big text style for the second page
//        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
//        secondPageStyle.setBigContentTitle("Page 2")
//                .bigText("A lot of text...");
//
//// Create second page notification
//        Notification secondPageNotification =
//                new NotificationCompat.Builder(this)
//                        .setStyle(secondPageStyle)
//                        .build();
//        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
////        extender.addPage(secondPageNotification).addAction(actionPause);
//
//// Extend the notification builder with the second page
//        Notification notification = notificationBuilder
//                .extend(extender
//                )
//                .build();
//
//        return notification;
//
//
////        return mBuilder.build();
//    }

//    public void showNotification(int count, String target) {
//        Notification notification = createNotification(count, target);
//        notificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
//    }

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
        unregisterReceiver(countReceiver);
        unregisterReceiver(recordReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }
}
