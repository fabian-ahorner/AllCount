package com.bitflake.allcount;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bitflake.allcount.db.CounterEntry;
import com.bitflake.counter.CountState;
import com.bitflake.counter.StateExtractor;
import com.bitflake.counter.StateView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CounterAdapter extends RecyclerView.Adapter<CounterAdapter.ViewHolder> {
    private List<CounterEntry> counters;
    private CounterEntry[] lastUsedCounters;
    private Comparator<CounterEntry> lastUsedComparator = new Comparator<CounterEntry>() {
        @Override
        public int compare(CounterEntry lhs, CounterEntry rhs) {
            return (int) (rhs.getLastUsed() - lhs.getLastUsed());
        }
    };

    public void setCounters(List<CounterEntry> counters) {
        this.counters = counters;
        extractLastUsedCounters();
        notifyDataSetChanged();
    }

    private void extractLastUsedCounters() {
        this.lastUsedCounters = new CounterEntry[Math.min(3, counters.size() / 3)];
        if (lastUsedCounters.length > 0) {
            int lastCounter = -1;
            for (int i = 0; i < counters.size(); i++) {
                CounterEntry counter = counters.get(i);
                if (lastCounter < lastUsedCounters.length - 1) {
                    lastUsedCounters[++lastCounter] = counter;
                    if (lastCounter + 1 == lastUsedCounters.length)
                        Arrays.sort(lastUsedCounters, lastUsedComparator);
                } else if (counter.getLastUsed() > lastUsedCounters[lastCounter].getLastUsed()) {
                    lastUsedCounters[lastCounter] = counter;
                    Arrays.sort(lastUsedCounters, lastUsedComparator);
                }
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final TextView text;
        private final StateView vis;
        private CounterEntry counter;

        public ViewHolder(View v) {
            super(v);
            text = (TextView) v.findViewById(R.id.text1);
            vis = (StateView) v.findViewById(R.id.counterVis);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        public void bind(CounterEntry counter, boolean isLastUsed) {
            this.counter = counter;
            text.setText(counter.getName());
            vis.setStates(counter.getStates());
            if (isLastUsed) {
                itemView.setBackgroundResource(R.color.colorPrimary);
            } else {
                itemView.setBackgroundResource(0);
            }
        }

        @Override
        public void onClick(View v) {
            counter.touch();
            Intent i = CountActivity.getStartIntent(v.getContext(), CountState.toBundle(counter.getStates()), counter.getId());
            v.getContext().startActivity(i);
//            counter.delete();
        }

        @Override
        public boolean onLongClick(View v) {
            counter.touch();
            List<CountState> states = StateExtractor.compressStates(counter.getStates());
            Intent i = CountActivity.getStartIntent(v.getContext(), CountState.toBundle(states));
            v.getContext().startActivity(i);
            return true;
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
        if (position >= lastUsedCounters.length) {
            holder.bind(counters.get(position - lastUsedCounters.length), false);
        } else {
            holder.bind(lastUsedCounters[position], true);
        }
    }

    @Override
    public int getItemCount() {
        return counters == null ? 0 : counters.size() + lastUsedCounters.length;
    }
}