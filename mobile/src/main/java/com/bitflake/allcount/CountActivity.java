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
import android.widget.TextView;

import com.bitflake.counter.services.CountService;
import com.bitflake.counter.TextToSpeachActivity;
import com.bitflake.counter.services.CountServiceHelper;


public class CountActivity extends TextToSpeachActivity implements CountServiceHelper.Constants {
    private TextView tCount;
    private FloatingActionButton fab;
    private View pCountProgress;
    private Messenger incomingMessenger = new Messenger(new IncomingHandler());
    private boolean isCounting;
    private Bundle states;


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
        tCount = (TextView) findViewById(R.id.tCount);
        tCount.setVisibility(View.VISIBLE);
        pCountProgress = findViewById(R.id.progress);
        findViewById(R.id.recordSettings).setVisibility(View.INVISIBLE);

        states = getIntent().getExtras();

        Intent intent = new Intent(this, CountService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void toggleCounting() {
        if (serviceMessenger == null)
            return;
        if (isCounting) {
            CountServiceHelper.stopCounting(serviceMessenger);
            fab.setImageResource(android.R.drawable.ic_media_play);
        } else {
            CountServiceHelper.startCounting(serviceMessenger, incomingMessenger, states);
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle data = msg.getData();
            int count = data.getInt(CountService.DATA_COUNT);
            float countProgress = data.getFloat(CountService.DATA_COUNT_PROGRESS);
            isCounting = data.getBoolean(CountService.DATA_IS_COUNTING);
            String sCount = String.valueOf(count);
            switch (msg.what) {
                case MSG_RESP_COUNT_PROGRESS:
                    setCountProgress(countProgress);
                    break;
                case MSG_RESP_COUNT:
                    setCountProgress(countProgress);
                    tCount.setText(sCount);
                    speak(sCount);
                    break;
                case MSG_RESP_STATUS:
                    setCountProgress(countProgress);
                    tCount.setText(sCount);
                    fab.setImageResource(isCounting ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private Messenger serviceMessenger;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            CountServiceHelper.startListening(serviceMessenger, incomingMessenger);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            serviceMessenger = null;
        }
    };

    public void setCountProgress(float progress) {
        pCountProgress.setTranslationX(-(1 - progress) * pCountProgress.getWidth());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(serviceConnection);
    }
}
