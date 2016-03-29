package com.bitflake.allcount.db;

import com.bitflake.counter.CountState;
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
        data = CountState.toJSON(states);
        lastUsed = System.currentTimeMillis();
    }

    public CounterEntry(String name, String states) {
        this.name = name;
        data = states;
        lastUsed = System.currentTimeMillis();
    }

    public List<CountState> getStates() {
        return CountState.fromJSON(data);
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
}
