package com.bitflake.allcount.db;

import com.bitflake.counter.algo.shared.current.CountState;
import com.bitflake.counter.tools.CountStateHelper;
import com.orm.SugarRecord;

import java.util.List;

public class CounterEntry extends SugarRecord {
    String name;
    String data;
    Long lastUsed;

    public CounterEntry() {
    }

    public CounterEntry(String name, List<CountState> states) {
        this.name = name;
        data = CountStateHelper.toJSON(states);
        lastUsed = System.currentTimeMillis();
    }

    public CounterEntry(String name, String states) {
        this.name = name;
        data = states;
        lastUsed = System.currentTimeMillis();
    }

    public List<CountState> getStates() {
        return CountStateHelper.fromJSON(data);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLastUsed() {
        return lastUsed == null ? 0 : lastUsed;
    }

    public void touch() {
        lastUsed = System.currentTimeMillis();
        save();
    }

    @Override
    public String toString() {
        return name;
    }
}
