package com.bitflake.allcount;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.Constances;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.algo.shared.old.CountState;
import com.bitflake.counter.StateView;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.WearCountService;
import com.bitflake.counter.services.CountServiceHelper;

import java.io.File;


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
    private StateView patternView;
    private CountServiceHelper countServiceHelper;
    private View bReset;

    private long id;
    private long stateId;
    private CounterEntry counterEntry;
    private EditText counterName;
    private boolean hasPressedBack;
    private Snackbar snack;
    private File currentFile;
    private View bShare;
    private File recordFile;

    public static Intent getStartIntent(Context context, Bundle states, boolean start, int count) {
        Intent i = new Intent(context, CountActivity.class);
        i.putExtra(EXTRA_START, start);
        i.putExtra(EXTRA_COUNT_OFFSET, count);
        i.putExtra(EXTRA_STATES, states);
        return i;
    }

    public static Intent getStartIntent(Context context, Bundle states, Long id) {
        Intent i = new Intent(context, CountActivity.class);
        i.putExtra(EXTRA_STATES_ID, id);
        i.putExtra(EXTRA_STATES, states);
        return i;
    }

    public static Intent getStartIntent(Context context, Bundle states) {
        Intent i = new Intent(context, CountActivity.class);
        i.putExtra(EXTRA_STATES, states);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
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
        bShare = findViewById(R.id.share);
        bShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareDataFile();
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
        patternView = (StateView) findViewById(R.id.patternView);

        states = getIntent().getBundleExtra(EXTRA_STATES);
        stateId = getIntent().getLongExtra(EXTRA_STATES_ID, -1);

        if (stateId != -1)
            counterEntry = CounterEntry.findById(CounterEntry.class, stateId);

        if (states == null) {
            this.states = CountState.toBundle(counterEntry.getStates());
        }

        if (counterEntry != null) {
            counterName.setText(counterEntry.getName());
        }

//        List<CountState> tmp = CountState.fromBundles(states);
//        StateExtractor.compressStates(tmp);
//        states = CountState.toBundle(tmp);

        patternView.setStates(CountState.fromBundles(states));
        patternView.listenToCounter();

        countOffset = getIntent().getIntExtra(EXTRA_COUNT_OFFSET, 0);
        tCount.setText(String.valueOf(countOffset));
        boolean startCounting = getIntent().getBooleanExtra(EXTRA_START, false);

        this.countServiceHelper = new CountServiceHelper(this);
        if (startCounting) {
            countServiceHelper.startServiceAndCounting(MobileCountService.class, states, countOffset);
            fab.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            startService(new Intent(this, MobileCountService.class));
        }
        ensureConnection(MobileCountService.class);
        ensureConnection(VoiceFeedbackService.class);
    }

    private void saveCounter() {
        String newName = counterName.getText().toString();
        if (newName.length() > 0) {
            if (counterEntry == null)
                counterEntry = new CounterEntry(newName, states.getString("data"));
            else
                counterEntry.setName(newName);
            counterEntry.save();
        } else if (counterEntry != null) {
            counterEntry.delete();
            counterEntry = null;
        }
    }

    private void resetCounter() {
        tCount.setText("0");
        bReset.setVisibility(View.INVISIBLE);
        bShare.setVisibility(View.INVISIBLE);
        countOffset = 0;
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
            currentFile = new File(getCacheDir(), Constances.DATA_FILE_COUNT);
        } else {
            if (counterEntry == null) {
                countServiceHelper.startCounting(states, countOffset);
            } else {
                countServiceHelper.startCounting(states, counterEntry.getId(), countOffset);
            }
            countOffset = 0;
            fab.setImageResource(android.R.drawable.ic_media_pause);
            startService(new Intent(this, VoiceFeedbackService.class));
        }
    }

    private void shareDataFile() {

        ShareDialog dialog = new ShareDialog();
        dialog.show(getFragmentManager(), "NoticeDialogFragment");

//        if (currentFile != null) {
//            long time = System.currentTimeMillis();
//            String fileName = String.format(Constances.DATA_FILE_FORMAT,
//                    counterName.getText().toString(),
//                    countOffset, time);
//            if (recordFile != null) {
//                String recName = String.format(Constances.DATA_FILE_FORMAT_REC,
//                        counterName.getText().toString(),
//                        countOffset, time);
//                recordFile.renameTo(new File(getCacheDir(), recName));
//                recordFile = new File(getCacheDir(), recName);
//            }
//            File tmpFile = new File(getCacheDir(), fileName);
//            currentFile.renameTo(tmpFile);
//            currentFile = tmpFile;
//
////            final Uri uri = FileProvider.getUriForFile(this, "com.bitflake.allcount.fileprovider", currentFile);
//// create an intent, so the user can choose which application he/she wants to use to share this file
////            final Intent intent = ShareCompat.IntentBuilder.from(this)
////                    .setType("text/plain")
////                    .setSubject(currentFile.getName())
////                    .addEmailTo("f.ahorner@gmail.com")
//////                    .setStream(uri)
////                    .setChooserTitle("Share with...")
////                    .createChooserIntent()
////                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
////                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//
//            intent.setType("text/plain");
//            intent.putExtra(android.content.Intent.EXTRA_EMAIL,
//                    new String[]{"f.ahorner@gmail.com"});
//            intent.putExtra(Intent.EXTRA_SUBJECT, currentFile.getName());
//
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//            ArrayList<Uri> uris = new ArrayList<Uri>();
//            uris.add(FileProvider.getUriForFile(this, "com.bitflake.allcount.fileprovider", currentFile));
//            if (recordFile != null)
//                uris.add(FileProvider.getUriForFile(this, "com.bitflake.allcount.fileprovider", recordFile));
//            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//
//            startActivity(intent);
//        }
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
                bShare.setVisibility(isCounting || count == 0 ? View.INVISIBLE : View.VISIBLE);
            }
            String sCount = String.valueOf(count);
            tCount.setText(sCount);
            String event = data.getString(DATA_EVENT_TYPE);
            switch (event) {
                case EVENT_STATUS:
                    if (data.containsKey(DATA_STATES_ID)) {
                        long counterId = data.getLong(DATA_STATES_ID);
                        counterEntry = CounterEntry.findById(CounterEntry.class, counterId);
                        counterName.setText(counterEntry.getName());
                    }
//                    states = data.getBundle(DATA_STATES);
                    break;
                default:
            }
        }

    };


    @SuppressWarnings("WrongConstant")
    @Override
    public void onBackPressed() {
//        if (isCounting)
//            toggleCounting();

        if (hasPressedBack || counterEntry != null) {
            super.onBackPressed();
            countServiceHelper.stopCounting();
        } else {
            hasPressedBack = true;
            snack = Snackbar
                    .make(tCount, R.string.delete_counter_warning, Snackbar.LENGTH_INDEFINITE);
            snack.setDuration(5000);
            snack.show();
//            tCount.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    snack.dismiss();
//                    snack = null;
//                }
//            }, 6000);
        }
    }


}
