package com.bitflake.allcount;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.bitflake.counter.HorizontalPicker;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.StateView;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.CountServiceHelper;
import com.bitflake.counter.services.RecordConstants;
import com.bitflake.counter.services.RecordServiceHelper;
import com.bitflake.counter.services.WearRecordService;
import com.bitflake.counter.tools.TextChangeAnimator;

public class RecordActivity extends ServiceConnectedActivity implements RecordConstants, RecordServiceHelper.RecordEventListener {
    private View progress;
    private TextChangeAnimator tStatus;
    private View bSkip;
    private RecordServiceHelper recordHelper;
    private int status;
    private StateView stateView;
    private RecordStillHelper stillHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_record);

        TextView tStatus1 = (TextView) findViewById(R.id.tStatus1);
        TextView tStatus2 = (TextView) findViewById(R.id.tStatus2);
        tStatus = new TextChangeAnimator(tStatus1, tStatus2);
        stateView = (StateView) findViewById(R.id.patternView);
        stateView.listenToRecorder();
        bSkip = findViewById(R.id.skip);
        bSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipState();
            }
        });
        bSkip.setVisibility(View.INVISIBLE);
        progress = findViewById(R.id.progress);
        recordHelper = new RecordServiceHelper(this);
        stillHelper = new RecordStillHelper(findViewById(R.id.stillness));
        registerReceiver(stillHelper, new IntentFilter(INTENT_RECORD_PROGRESS));
        startService(new Intent(this, WearRecordService.class));
        ensureConnection(VoiceFeedbackService.class);
        ensureConnection(WearRecordService.class);
        recordHelper.enableEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recordHelper.requestUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    public void onStatusReceived(Bundle data) {
//        updateStatus();
        long duration = data.getLong(DATA_DURATION_MS);
        if (duration > 0) {
            long remainingTime = data.getLong(DATA_REMAINING_TIME);
            float currentProgress = remainingTime * progress.getWidth() / duration;
            this.progress.setTranslationX(-currentProgress);
            if (remainingTime > 0)
                this.progress.animate().translationX(0).setDuration(remainingTime).setInterpolator(new LinearInterpolator());
        }
    }

    public void onFinishedRecording(Bundle data) {
        Bundle states = data.getBundle(DATA_STATES);
//        new CountServiceHelper(this).startCounting(MobileCountService.class, states, 1);
        Intent intent = CountActivity.getStartIntent(this, states, true, 1);
        startActivity(intent);
        finish();
    }

    public void onStartRecording(Bundle data) {
        tStatus.setText(R.string.start_calibrating_long);
    }

    @Override
    public void onStopRecording(Bundle data) {
        resetUI();
    }

    @Override
    public void onBroadcastReceived(Bundle data) {
        status = data.getInt(DATA_STATUS);
    }

    @Override
    public void onStartCalibrating(Bundle data) {
        tStatus.setText(R.string.start_calibrating_long);
    }

    @Override
    public void onStartMoving(Bundle data) {
        tStatus.setText(R.string.do_repetition);
        bSkip.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartMoveBack(Bundle data) {
        tStatus.setText(R.string.hold_still_at_start);
    }

    public void onStartDelay(Bundle data) {
        startProgressAnimation(data);
    }

    private void startProgressAnimation(Bundle data) {
        long duration = data.getLong(DATA_DURATION_MS);
        animateProgress(0, 1, duration);
    }

    private void skipState() {
        recordHelper.skipState();
    }

    public void resetUI() {
        bSkip.setVisibility(View.INVISIBLE);
        setProgressBar(0);
    }

    public void setProgressBar(float progress) {
        this.progress.animate().cancel();
        this.progress.setTranslationX(-(1 - progress) * this.progress.getWidth());
    }

    public void animateProgress(float progress, long duration) {
        this.progress.animate().translationX(-(1 - progress) * this.progress.getWidth()).setDuration(duration).setInterpolator(new LinearInterpolator());
    }

    public void animateProgress(float from, float to, long duration) {
        setProgressBar(from);
        animateProgress(to, duration);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recordHelper.stopRecording();
        recordHelper.disableEventListener();
        unregisterReceiver(stillHelper);
    }
}
