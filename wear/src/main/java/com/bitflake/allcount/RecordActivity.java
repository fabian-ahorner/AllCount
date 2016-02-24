package com.bitflake.allcount;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.bitflake.counter.HorizontalPicker;
import com.bitflake.counter.TextToSpeachActivity;
import com.bitflake.counter.services.RecordService;
import com.bitflake.counter.services.RecordServiceHelper;

public class RecordActivity extends TextToSpeachActivity implements RecordServiceHelper.Constants {

    private FloatingActionButton fab;
    private View progress;
    private Messenger incomingMessenger = new Messenger(new IncomingHandler());
    private int recordingStatus = RecordService.STATUS_NONE;
    private TextView tStatus;
    private View layoutSettings;
    private HorizontalPicker pickerDelay;
    private HorizontalPicker pickerDuration;
    private View bSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.activity_record);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCounting();
            }
        });
        tStatus = (TextView) findViewById(R.id.tMessage);
        layoutSettings = findViewById(R.id.recordSettings);
        bSkip = findViewById(R.id.skip);
        bSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipState();
            }
        });
        progress = findViewById(R.id.progress);
        pickerDelay = (HorizontalPicker) findViewById(R.id.pickerDelay);
        pickerDuration = (HorizontalPicker) findViewById(R.id.pickerDuration);
        Intent intent = new Intent(this, RecordService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void skipState() {
        RecordServiceHelper.skipState(serviceMessenger);
    }

    private void toggleCounting() {
        if (serviceMessenger == null)
            return;
        if (recordingStatus == RecordService.STATUS_NONE || recordingStatus == RecordService.STATUS_FINISHED) {
            startRecording();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            RecordServiceHelper.stopRecording(serviceMessenger);
            fab.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void startRecording() {
        int delay = getValueFromPicker(pickerDelay);
        int duration = getValueFromPicker(pickerDuration);
        RecordServiceHelper.startRecording(serviceMessenger, incomingMessenger, delay, duration);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            recordingStatus = msg.arg1;
            switch (msg.what) {
                case MSG_RESP_STOPPED_RECORDING:
                    resetUI();
                    break;
                case MSG_RESP_START_DELAY:
                    speak(R.string.get_ready);
                    tStatus.setText(R.string.get_ready);
                    tStatus.setVisibility(View.VISIBLE);
                    bSkip.setVisibility(View.VISIBLE);
                    layoutSettings.setVisibility(View.INVISIBLE);
                    animateProgress(0, 1, msg.arg2);
                    break;
                case MSG_RESP_START_RECORDING:
                    speak(R.string.recording);
                    tStatus.setVisibility(View.VISIBLE);
                    tStatus.setText(R.string.recording);
                    bSkip.setVisibility(View.VISIBLE);
                    layoutSettings.setVisibility(View.INVISIBLE);
                    animateProgress(0, 1, msg.arg2);
                    break;
                case MSG_RESP_FINISHED_RECORDING:
                    speak(R.string.stop_recording);
                    useStates(msg.getData());
                    resetUI();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void useStates(Bundle states) {
        Intent intent = CountActivity.getStartIntent(this, states, true, 1);
        startActivity(intent);
    }

    public void resetUI() {
        fab.setImageResource(android.R.drawable.ic_media_play);
        tStatus.setVisibility(View.INVISIBLE);
        bSkip.setVisibility(View.INVISIBLE);
        layoutSettings.setVisibility(View.VISIBLE);
        setProgressBar(0);
    }

    private Messenger serviceMessenger;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            RecordServiceHelper.startListening(serviceMessenger, incomingMessenger);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceMessenger = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }

    private static int getValueFromPicker(HorizontalPicker picker) {
        return Integer.valueOf(picker.getValues()[picker.getSelectedItem()].toString()) * 1000;
    }

    public void setProgressBar(float progress) {
        this.progress.animate().cancel();
        this.progress.setTranslationX(-(1 - progress) * this.progress.getWidth());
    }

    public void animateProgress(float progress, int duration) {
        this.progress.animate().translationX(-(1 - progress) * this.progress.getWidth()).setDuration(duration).setInterpolator(new LinearInterpolator());
    }

    public void animateProgress(float from, float to, int duration) {
        setProgressBar(from);
        animateProgress(to, duration);
    }
}
