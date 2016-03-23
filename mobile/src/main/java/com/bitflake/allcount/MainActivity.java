package com.bitflake.allcount;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Spinner;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.services.RecordServiceHelper;
import com.bitflake.counter.services.WearCountService;
import com.bitflake.counter.services.WearRecordService;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends ServiceConnectedActivity implements View.OnClickListener {
    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private CounterAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ensureConnection(VoiceFeedbackService.class);
//        ensureConnection(WearCountService.class);
        ensureConnection(WearRecordService.class);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new CounterAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                startActivity(new Intent(this, RecordActivity.class));
                startService(new Intent(this, WearCountService.class));
                new RecordServiceHelper(this).startRecording(getDelay(), getDuration());
        }
    }

    public int getDelay() {
        return getValueFromSpinner((Spinner) findViewById(R.id.spinnerDelay));
    }

    private int getValueFromSpinner(Spinner s) {
        String text = s.getSelectedItem().toString();
        return Integer.valueOf(text) * 1000;
    }

    public int getDuration() {
        return getValueFromSpinner((Spinner) findViewById(R.id.spinnerDuration));
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CounterLoader().execute();
    }

    class CounterLoader extends AsyncTask<Void, Void, List<CounterEntry>> {
        @Override
        protected List<CounterEntry> doInBackground(Void... params) {
            List<CounterEntry> counters = Select.from(CounterEntry.class).orderBy("name").list();
            return counters;
        }

        @Override
        protected void onPostExecute(List<CounterEntry> counters) {
            adapter.setCounters(counters);
        }
    }
}
