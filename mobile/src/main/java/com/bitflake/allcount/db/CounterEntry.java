package com.bitflake.allcount.db;

import com.bitflake.counter.StateWindow;
import com.orm.SugarRecord;

import java.util.List;

public class CounterEntry extends SugarRecord {
    String name;
    String data;
    String sensors;

    public CounterEntry() {
    }

    public CounterEntry(String name, List<StateWindow> states) {
        this.name = name;
        data = StateWindow.toJSON(states);
    }
    public CounterEntry(String name, String states) {
        this.name = name;
        data = states;
    }

    public List<StateWindow> getStates() {
        return StateWindow.fromJSON(data);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
