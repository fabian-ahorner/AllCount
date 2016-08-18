package com.bitflake.counter.tools;

import android.os.Bundle;

import com.bitflake.counter.algo.shared.old.CountState;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class CountStateHelper {

    private static Bundle toBundle(CountState state) {
        Bundle b = new Bundle();
        b.putDoubleArray("means", state.means);
//        b.putDoubleArray("sd", state.sd);
        b.putInt("id", state.getId());
        if (state.getNext() != null)
            b.putInt("next", state.getNext().getId());
        return b;
    }

    private static CountState fromBundle(Bundle bundle) {
        double[] means = bundle.getDoubleArray("means");
        double[] sd = bundle.getDoubleArray("sd");
        int id = bundle.getInt("id");
        return new CountState(means, sd, id);
    }

    public static List<CountState> fromBundles(Bundle bundle) {
        return fromJSON(bundle.getString("data"));
    }

    public static Bundle toBundles(CountState state) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Bundle b = new Bundle();
        b.putString("data", toJSON(state));
        return b;
    }

    public static Bundle toBundle(List<CountState> states) {
        if (states.isEmpty())
            return null;
        return toBundles(states.get(0));
    }

    public static String toJSON(CountState state) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(state);
    }

    public static List<CountState> fromJSON(String json) {
        CountState state = stateFromJSON(json);
        List<CountState> states = new ArrayList<>();

        while (state != null) {
            states.add(state);
            state = state.getNext();
        }
        return states;
    }

    public static CountState stateFromJSON(String json) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(json, CountState.class);
    }


    public static String toJSON(List<CountState> states) {
        return toJSON(states.get(0));
    }
}
