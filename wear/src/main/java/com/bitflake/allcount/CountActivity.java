package com.bitflake.allcount;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import com.bitflake.counter.Constances;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.wear.WearConnectionManager;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.WearCountService;
import com.bitflake.counter.services.CountServiceHelper;


public class CountActivity extends ServiceConnectedActivity implements CountConstants {

    private static final String EXTRA_START = "start";
    private static final String EXTRA_COUNT_OFFSET = "count";
    private static final String EXTRA_STATES = "states";
    private TextView tCount;
    private FloatingActionButton fab;
    private View pCountProgress;
    private boolean isCounting;
    private Bundle states;
    private int countOffset;
    private CountServiceHelper countServiceHelper;
    private View bReset;

    public static Intent getStartIntent(Context context, Bundle states, boolean start, int count) {
        Intent i = new Intent(context, CountActivity.class);
        i.putExtra(EXTRA_START, start);
        i.putExtra(EXTRA_COUNT_OFFSET, count);
        i.putExtra(EXTRA_STATES, states);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setContentView(R.layout.activity_count);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCounting();
            }
        });
        bReset = findViewById(R.id.reset);
        bReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCounter();
            }
        });
        tCount = (TextView) findViewById(R.id.tCount);
        tCount.setVisibility(View.VISIBLE);
        pCountProgress = findViewById(R.id.progress);
        findViewById(R.id.recordSettings).setVisibility(View.INVISIBLE);

        states = getIntent().getBundleExtra(EXTRA_STATES);
        countOffset = getIntent().getIntExtra(EXTRA_COUNT_OFFSET, 0);
        tCount.setText(String.valueOf(countOffset));
        boolean startCounting = getIntent().getBooleanExtra(EXTRA_START, false);
        ensureConnection(WearCountService.class);

        this.countServiceHelper = new CountServiceHelper(this);
        if (startCounting) {
            startServiceAndCounting();
            startMobileVoiceService();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            startService(new Intent(this, WearCountService.class));
        }
    }

    private void startServiceAndCounting() {
        Intent i = new Intent(this,
                WearCountService.class);
        i.putExtra(DATA_COMMAND, CMD_START_COUNTING);
        i.putExtra(DATA_STATES, states);
        i.putExtra(DATA_COUNT_OFFSET, countOffset);
        startService(i);
    }

    private void startMobileVoiceService() {
        WearConnectionManager.getInstance().startService("com.bitflake.allcount.VoiceFeedbackService", null);
    }

    private void resetCounter() {
        tCount.setText("0");
        bReset.setVisibility(View.INVISIBLE);
        countOffset = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent(this, WearCountService.class);
        stopService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dataReceiver, new IntentFilter(Constances.INTENT_COUNT_STATUS ));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(dataReceiver);
    }

    private void toggleCounting() {
        if (isCounting) {
            countServiceHelper.stopCounting();
            fab.setImageResource(android.R.drawable.ic_media_play);
        } else {
            startMobileVoiceService();
            countServiceHelper.startCounting(states, countOffset);
            countOffset = 0;
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int count = data.getInt(WearCountService.DATA_COUNT);
            countOffset = count;
            isCounting = data.getBoolean(WearCountService.DATA_IS_COUNTING);
            String sCount = String.valueOf(count);
            tCount.setText(sCount);
            String event = data.getString(DATA_EVENT_TYPE);
            switch (event) {
                case EVENT_START_COUNTING:
                case EVENT_STOP_COUNTING:
                case EVENT_STATUS:
                    fab.setImageResource(isCounting ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                    bReset.setVisibility(isCounting || count == 0 ? View.INVISIBLE : View.VISIBLE);
                    setCountProgress(0);
                    break;
                default:
            }
        }

    };

    public void setCountProgress(float progress) {
        pCountProgress.setTranslationX(-(1 - progress) * pCountProgress.getWidth());
    }

    @Override
    public void onBackPressed() {
        if (isCounting)
            toggleCounting();
        super.onBackPressed();
    }
}
