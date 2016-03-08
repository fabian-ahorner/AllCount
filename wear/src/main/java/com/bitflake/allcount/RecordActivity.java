package com.bitflake.allcount;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.bitflake.counter.HorizontalPicker;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.services.WearRecordService;
import com.bitflake.counter.wear.WearConnectionManager;
import com.bitflake.counter.services.RecordConstants;
import com.bitflake.counter.services.RecordServiceHelper;

public class RecordActivity extends ServiceConnectedActivity implements RecordConstants, RecordServiceHelper.RecordEventListener {
    private FloatingActionButton fab;
    private View progress;
    private TextView tStatus;
    private View layoutSettings;
    private HorizontalPicker pickerDelay;
    private HorizontalPicker pickerDuration;
    private View bSkip;
    private RecordServiceHelper recordHelper;
    private int status;

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
        tStatus = (TextView) findViewById(R.id.tStatus);
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
        recordHelper = new RecordServiceHelper(this);
//        startService(new Intent(this, WearRecordService.class));
//        ensureConnection(WearRecordService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordHelper.enableEventListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        recordHelper.disableEventListener();
    }

    private void updateStatus() {
        boolean isRunning = status == STATUS_RECORDING || status == STATUS_DELAY;

        int visibleIfRunning = isRunning ? View.VISIBLE : View.INVISIBLE;
        int visibleIfNotRunning = isRunning ? View.INVISIBLE : View.VISIBLE;

        tStatus.setVisibility(visibleIfRunning);
        bSkip.setVisibility(visibleIfRunning);
        layoutSettings.setVisibility(visibleIfNotRunning);

        fab.setImageResource(isRunning ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        if (isRunning) {
            tStatus.setText(status == STATUS_DELAY ? R.string.get_ready : R.string.recording);
        }
    }

    @Override
    public void onStatusReceived(Bundle data) {
        updateStatus();
    }

    public void onFinishedRecording(Bundle data) {
        useStates(data.getBundle(DATA_STATES));
        resetUI();
    }

    public void onStartRecording(Bundle data) {
        updateStatus();
        startProgressAnimation(data);
    }

    @Override
    public void onStopRecording(Bundle data) {
        resetUI();
    }

    @Override
    public void onBroadcastReceived(Bundle data) {
        status = data.getInt(DATA_STATUS);
    }

    public void onStartDelay(Bundle data) {
        updateStatus();
        startProgressAnimation(data);
    }

    private void startProgressAnimation(Bundle data) {
        int duration = data.getInt(DATA_DURATION_MS);
        animateProgress(0, 1, duration);
    }

    private void skipState() {
        recordHelper.skipState();
    }

    private void toggleCounting() {
        if (status == WearRecordService.STATUS_NONE || status == WearRecordService.STATUS_FINISHED) {
            recordHelper.startRecording(getValue(pickerDelay), getValue(pickerDuration));
            startMobileVoiceService();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            recordHelper.stopRecording();
            fab.setImageResource(android.R.drawable.ic_media_play);
        }
    }

    private void startMobileVoiceService() {
        WearConnectionManager.getInstance().startService("com.bitflake.allcount.VoiceFeedbackService",null);
    }

    private void useStates(Bundle states) {
        Intent intent = WearCountActivity.getStartIntent(this, states, true, 1);
        startActivity(intent);
    }

    public void resetUI() {
        fab.setImageResource(android.R.drawable.ic_media_play);
        tStatus.setVisibility(View.INVISIBLE);
        bSkip.setVisibility(View.INVISIBLE);
        layoutSettings.setVisibility(View.VISIBLE);
        setProgressBar(0);
    }


    private static int getValue(HorizontalPicker picker) {
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
