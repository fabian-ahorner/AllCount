package com.bitflake.allcount;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.services.RecordServiceHelper;
import com.bitflake.counter.services.WearCountService;
import com.bitflake.counter.services.WearRecordService;

public class HelpActivity extends ServiceConnectedActivity implements View.OnClickListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        findViewById(R.id.bRecord).setOnClickListener(this);
        ensureConnection(VoiceFeedbackService.class);
        ensureConnection(MobileRecordService.class);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bRecord:
                new RecordServiceHelper(this).startRecording(0, 0);

//                new RecordServiceHelper(this).startRecording();
                startService(new Intent(this, MobileRecordService.class));
                startActivity(new Intent(this, RecordActivity.class));
//                finish();
                return;
        }
    }
}
