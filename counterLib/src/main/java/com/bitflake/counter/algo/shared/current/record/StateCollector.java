package com.bitflake.counter.algo.shared.current.record;

import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.algo.shared.current.CountState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fahor on 04/10/2016.
 */
public class StateCollector implements SlidingWindow.WindowAnalyser {
    private List<CountState> states = new ArrayList<>();
    private CountState lastState;
    private double distSum;
    private int lastStateId;

    public void clear() {
        states.clear();
        lastState = null;
        lastStateId = 0;
    }

    @Override
    public void analyseValues(double[] means) {
        CountState state = new CountState(means);
        state.setId(lastStateId++);
        state.setTime(lastStateId);
        states.add(state);
        if (lastState != null) {
            lastState.setNext(state);
        }
        lastState = state;
    }
    public List<CountState> getStates() {
        return states;
    }
}
