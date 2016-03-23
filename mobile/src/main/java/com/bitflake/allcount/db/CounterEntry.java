package com.bitflake.allcount.db;

import com.bitflake.counter.CountState;
import com.orm.SugarRecord;

import java.util.List;

public class CounterEntry extends SugarRecord {
    String name;
    String data;
    String sensors;

    public CounterEntry() {
    }

    public CounterEntry(String name, List<CountState> states) {
        this.name = name;
        data = CountState.toJSON(states);
    }
    public CounterEntry(String name, String states) {
        this.name = name;
        data = states;
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
}
