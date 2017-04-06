package com.bitflake.allcount;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.services.ProxySensorService;
import com.bitflake.counter.services.RecordServiceHelper;
import com.orm.query.Select;

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
        toolbar.setTitle(R.string.app_name);
//        inflateMenu(R.menu.menu_main);
//        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//
//                switch (menuItem.getItemId()) {
//                    case R.id.action_help:
//                        showHelp();
//                        return true;
//                }
//
//                return false;
//            }
//        });

        ensureConnection(VoiceFeedbackService.class);
//        ensureConnection(WearCountService.class);
        ensureConnection(MobileRecordService.class);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        adapter = new CounterAdapter();
        recyclerView.setAdapter(adapter);
//        startService(new Intent(this, ProxySensorService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                showHelp();
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                new RecordServiceHelper(this).startRecording(getDelay(), getDuration());
                startService(new Intent(this, MobileRecordService.class));
                startActivity(new Intent(this, RecordActivity.class));
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
            List<CounterEntry> counters = Select.from(CounterEntry.class).orderBy("LOWER(name)").list();
            return counters;
        }

        @Override
        protected void onPostExecute(List<CounterEntry> counters) {
            if (counters.isEmpty()) {
                showHelp();
                finish();
            }
            adapter.setCounters(counters);
        }
    }

    public void showHelp() {
        startActivity(new Intent(MainActivity.this, HelpActivity.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        stopService(new Intent(this, ProxySensorService.class));
    }
}
