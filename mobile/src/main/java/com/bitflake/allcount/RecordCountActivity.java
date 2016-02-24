package com.bitflake.allcount;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.bitflake.counter.HorizontalPicker;
import com.bitflake.counter.PatternView;
import com.bitflake.counter.SensorActivity;
import com.bitflake.counter.SensorCounter;
import com.bitflake.counter.StateExtractor;

public class RecordCountActivity extends SensorActivity implements SensorCounter.CountListener, View.OnClickListener {
    private StateExtractor stateExtractor = new StateExtractor();
    private SensorCounter counter = new SensorCounter();
    private TextView tCount;
    private View pCountProgress;
    private boolean isRecording;
    private boolean isCounting;
    private TextView tMessage;
    private FloatingActionButton fab;
    private View bReset;
    private View recordSettings;
    private int recordingDuration;
    private Runnable rStartRecording = new Runnable() {
        @Override
        public void run() {
            startRecording(recordingDuration);
        }
    };
    private HorizontalPicker pDelay;
    private HorizontalPicker pDuration;
    private View bSkip;
    private PatternView patternView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);
        setAnalyser(stateExtractor);
        tCount = (TextView) findViewById(R.id.tCount);
        tMessage = (TextView) findViewById(R.id.tMessage);
        pCountProgress = findViewById(R.id.progress);
        counter.setCountListener(this);
        recordSettings = findViewById(R.id.recordSettings);
        patternView = (PatternView) findViewById(R.id.patternView);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        bReset = findViewById(R.id.reset);
        bReset.setOnClickListener(this);
        bSkip = findViewById(R.id.skip);
        bSkip.setOnClickListener(this);
        pDelay = (HorizontalPicker) findViewById(R.id.pickerDelay);
        pDuration = (HorizontalPicker) findViewById(R.id.pickerDuration);
    }

    public void startRecording(int delay, final int recordingDuration) {
        if (isRecording)
            return;
        bReset.setVisibility(View.INVISIBLE);
        this.recordingDuration = recordingDuration;
        isRecording = true;
        hideRecordSettings();
        counter.reset();
        counter.setStates(null);
        stateExtractor.clear();
        pCountProgress.setTranslationX(0);
        pCountProgress.animate().translationX(-pCountProgress.getWidth()).setDuration(delay).setInterpolator(new LinearInterpolator());
        setAnalyser(stateExtractor);
        showMessage(R.string.get_ready, true);
        tCount.postDelayed(rStartRecording, delay);
        bSkip.setVisibility(View.VISIBLE);
    }

    private Runnable rStartCounting = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                speak(R.string.stop_recording);
                startCounting();
            }
        }
    };

    private void startRecording(int recordDuration) {
        if (!isRecording)
            return;
        showMessage(R.string.recording, false);
        speak(R.string.start_recording);
        pCountProgress.setTranslationX(0);
        pCountProgress.animate().translationX(-pCountProgress.getWidth()).setDuration(recordDuration).setInterpolator(new LinearInterpolator());
        tCount.postDelayed(rStartCounting, recordDuration);
        startListening();
    }

    private void startCounting() {
        if (isRecording) {
            isRecording = false;
            showMessage(R.string.counting, true);
            bSkip.setVisibility(View.INVISIBLE);
        }
        isCounting = true;
        if (!counter.hasStates()) {
            stateExtractor.compressStates();
            counter.setStates(stateExtractor.getStates());
//            patternView.setStates(stateExtractor.getStates());
        }
        stopListening();
        pCountProgress.setTranslationX(-pCountProgress.getWidth());
        setAnalyser(counter);
        startListening();
    }

    @Override
    public void onCount(int count) {
        showCount(count + 1, true);
        pCountProgress.setTranslationX(-pCountProgress.getWidth());
    }

    @Override
    public void onCountProgress(float progress) {
        pCountProgress.animate().translationX((1 - progress) * -pCountProgress.getWidth()).setDuration(100);
        patternView.invalidate();
    }

    @Override
    public void onStartPress() {
        super.onStartPress();
        fab.setImageResource(R.drawable.ic_media_pause);
        if (!isRecording && !isCounting) {
            if (counter.hasStates()) {
                startCounting();
            } else {
                startRecording(getDelay(), getDuration());
            }
        }
    }

    @Override
    public void onStopPress() {
        super.onStopPress();
        tCount.removeCallbacks(rStartCounting);
        tCount.removeCallbacks(rStartRecording);
        fab.setImageResource(R.drawable.ic_media_play);
        isRecording = false;
        isCounting = false;
        stopListening();
        pCountProgress.animate().translationX(0).setDuration(200);
        tMessage.setVisibility(View.INVISIBLE);
        if (!counter.hasStates()) {
            showRecordSettings();
        }
    }

    public void showMessage(int stringRes, boolean speak) {
        tCount.setVisibility(View.INVISIBLE);
        tMessage.setText(stringRes);
        if (speak)
            speak(stringRes);
        tMessage.setVisibility(View.VISIBLE);
    }

    public void showCount(int count, boolean speak) {
        tMessage.setVisibility(View.INVISIBLE);
        String text = String.valueOf(count);
        tCount.setText(text);
        if (speak)
            speak(text);
        tCount.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (isRecording || isCounting) {
                    onStopPress();
                } else {
                    onStartPress();
                }
                break;
            case R.id.reset:
                counter.reset();
                showCount(0, false);
                bReset.setVisibility(View.INVISIBLE);
                break;
            case R.id.skip:
                if (isRecording) {
                    if (isListening()) {
                        tCount.removeCallbacks(rStartCounting);
                        startCounting();
                    } else {
                        tCount.removeCallbacks(rStartRecording);
                        startRecording(recordingDuration);
                    }
                }
                break;
        }
    }

    @Override
    public void startListening() {
        super.startListening();
        bReset.setVisibility(View.INVISIBLE);
    }

    @Override
    public void stopListening() {
        super.stopListening();
        if (counter.hasStates() && counter.getCount() > 0)
            bReset.setVisibility(View.VISIBLE);
        bSkip.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (isRecording || isCounting) {
            onStopPress();
        } else if (counter.hasStates()) {
            counter.setStates(null);
            counter.reset();
            showRecordSettings();
        } else
            super.onBackPressed();
    }

    public void showRecordSettings() {
        recordSettings.animate().translationY(0).setDuration(500);
    }

    public void hideRecordSettings() {
        recordSettings.animate().translationY(recordSettings.getHeight() + pCountProgress.getHeight()).setDuration(500);
    }

    public int getDelay() {
        String sValue = pDelay.getValues()[pDelay.getSelectedItem()].toString();
        return Integer.valueOf(sValue) * 1000;
    }

    public int getDuration() {
        String sValue = pDuration.getValues()[pDuration.getSelectedItem()].toString();
        return Integer.valueOf(sValue) * 1000;
    }
}
