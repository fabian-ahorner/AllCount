package com.bitflake.allcount;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.Constances;
import com.bitflake.counter.PatternView;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.StateWindow;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.WearCountService;
import com.bitflake.counter.services.CountServiceHelper;


public class CountActivity extends ServiceConnectedActivity implements CountConstants {

    private static final String EXTRA_START = "start";
    //    private static final String EXTRA_TARGET = "target";
    private static final String EXTRA_COUNT_OFFSET = "count";
    private static final String EXTRA_STATES = "states";
    private static final String EXTRA_STATES_ID = "id";
    private TextView tCount;
    private FloatingActionButton fab;
    private View pCountProgress;
    private boolean isCounting;
    private Bundle states;
    private int countOffset;
    private PatternView patternView;
    private CountServiceHelper countServiceHelper;
    private View bReset;

    private long id;
    private long stateId;
    private CounterEntry counterEntry;
    private EditText counterName;

    public static Intent getStartIntent(Context context, Bundle states, boolean start, int count) {
        Intent i = new Intent(context, CountActivity.class);
        i.putExtra(EXTRA_START, start);
        i.putExtra(EXTRA_COUNT_OFFSET, count);
        i.putExtra(EXTRA_STATES, states);
        return i;
    }

    public static Intent getStartIntent(Context context, String target) {
        Intent i = new Intent(context, CountActivity.class);
//        i.putExtra(EXTRA_TARGET, target);
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
        counterName = (EditText) findViewById(R.id.counterName);
        counterName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                saveCounter();
            }
        });
        pCountProgress = findViewById(R.id.progress);
        patternView = (PatternView) findViewById(R.id.patternView);

        states = getIntent().getBundleExtra(EXTRA_STATES);
        stateId = getIntent().getLongExtra(EXTRA_STATES_ID, -1);
        if (states == null) {
            counterEntry = CounterEntry.findById(CounterEntry.class, stateId);
            this.states = StateWindow.toBundle(counterEntry.getStates());
        }

        if (counterEntry != null) {
            counterName.setText(counterEntry.getName());
        }

        countOffset = getIntent().getIntExtra(EXTRA_COUNT_OFFSET, 0);
        tCount.setText(String.valueOf(countOffset));
        boolean startCounting = getIntent().getBooleanExtra(EXTRA_START, false);

        this.countServiceHelper = new CountServiceHelper(this);
        if (startCounting) {
            startServiceAndCounting();
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            startService(new Intent(this, MobileCountService.class));
        }
        ensureConnection(MobileCountService.class);
        ensureConnection(VoiceFeedbackService.class);
    }

    private void saveCounter() {
        if (counterEntry == null) {
            counterEntry = new CounterEntry(counterName.getText().toString(), states.getString("data"));
        } else {
            counterEntry.setName(counterName.getText().toString());
        }
        counterEntry.save();
    }

    private void startServiceAndCounting() {
        Intent i = new Intent(this,
                MobileCountService.class);
        i.putExtra(DATA_COMMAND, CMD_START_COUNTING);
        i.putExtra(DATA_STATES, states);
        i.putExtra(DATA_COUNT_OFFSET, countOffset);
        startService(i);
    }

    private void resetCounter() {
        tCount.setText("0");
        bReset.setVisibility(View.INVISIBLE);
        countOffset = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(dataReceiver, new IntentFilter(Constances.INTENT_COUNT_STATUS));
        countServiceHelper.requestUpdate();
//        registerReceiver(dataReceiver, new IntentFilter(Constances.INTENT_COUNT_STATUS + Constances.INTENT_TARGET_WEAR));
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
            countServiceHelper.startCounting(states, countOffset);
            countOffset = 0;
            fab.setImageResource(android.R.drawable.ic_media_pause);
            startService(new Intent(this, VoiceFeedbackService.class));
        }
    }

    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            int count = data.getInt(WearCountService.DATA_COUNT);
            countOffset = count;
            boolean isCounting = data.getBoolean(WearCountService.DATA_IS_COUNTING);
            if (isCounting != CountActivity.this.isCounting) {
                CountActivity.this.isCounting = isCounting;
                fab.setImageResource(isCounting ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                bReset.setVisibility(isCounting || count == 0 ? View.INVISIBLE : View.VISIBLE);
            }
            String sCount = String.valueOf(count);
            tCount.setText(sCount);
            String event = data.getString(DATA_EVENT_TYPE);
            switch (event) {
                case EVENT_STATUS:
                    states = data.getBundle(DATA_STATES);
                case EVENT_START_COUNTING:
                case EVENT_STOP_COUNTING:
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
//        if (isCounting)
//            toggleCounting();
        super.onBackPressed();
    }
}
