package com.bitflake.allcount;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.CountState;
import com.bitflake.counter.StateView;

import java.util.List;

public class CounterAdapter extends RecyclerView.Adapter<CounterAdapter.ViewHolder> {
    private List<CounterEntry> counters;

    public void setCounters(List<CounterEntry> counters) {
        this.counters = counters;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView text;
        private final StateView vis;
        private CounterEntry counter;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.text1);
            vis = (StateView) v.findViewById(R.id.counterVis);
            v.setOnClickListener(this);
        }

        public void bind(CounterEntry counter) {
            this.counter = counter;
            text.setText(counter.getName());
            vis.setStates(counter.getStates());
        }

        @Override
        public void onClick(View v) {
            Intent i = CountActivity.getStartIntent(v.getContext(), CountState.toBundle(counter.getStates()), counter.getId());
            v.getContext().startActivity(i);
        }
    }

    public CounterAdapter() {
    }

    @Override
    public CounterAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_counters, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(counters.get(position));
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size();
    }
}