package com.bitflake.counter;

import android.os.Bundle;
import android.util.SparseArray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class StateWindow {
    @Expose
    private double[] means;
    @Expose
    private double[] sd;
    @Expose
    private StateWindow next;
    @Expose
    private int id;
    @Expose
    private double distanceToNext = 0;
    private double distance;
    private int particleCount = 0;
    private int totalParticles;

    public StateWindow(double[] means, double[] sd, int id) {
        this.means = means;
        this.sd = sd;
        this.id = id;
    }

    public StateWindow(double[] means, double[] sd) {
        this.means = means;
        this.sd = sd;
    }

    public double getDistance(StateWindow w) {
        double sim = 0;
        for (int i = 0; i < means.length; i++) {
            sim += Math.pow((means[i] - w.means[i]), 2);
            sim += Math.pow(sd[i] - w.sd[i], 2);
        }
        return Math.sqrt(sim) / (means.length * 2);
//        double sim = 0;
//        for (int i = 0; i < means.length; i++) {
//            sim += getSimLog(means[i], w.means[i]);
//            sim += getSimLog(sd[i], w.sd[i]);
//        }
//        return -sim;
    }

    private double getSimLog(double v1, double v2) {
        v1 += 0.01;
        v2 += 0.01;
        return Math.log10((2 * v1 * v2) / (v1 * v1 + v2 * v2));
    }

    public void setNext(StateWindow w) {
        this.next = w;
        this.distanceToNext = w == null ? 0 : getDistance(w);
    }

    public double getDistanceToNext() {
        return distanceToNext;
    }

    public StateWindow getNext() {
        return next;
    }

    @Override
    public String toString() {
        return String.format("Means: %5.2f %5.2f %5.2f  \tSD: %5.2f %5.2f %5.2f  \tSim: %5.2f", means[0], means[1], means[2], sd[0], sd[1], sd[2], distanceToNext);
    }


    public void updateDistance(StateWindow w) {
        if (next != null)
            next.updateDistance(w);
        this.distance = getDistance(w);
    }

    public double getDistance() {
        return distance;
    }

    public void removeParticle() {
        particleCount--;
    }

    public void addParticle() {
        particleCount++;
    }

    public void resetParticleCount() {
        particleCount = 0;
    }

    public int getParticleCount() {
        return particleCount;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Bundle toBundle() {
        Bundle b = new Bundle();
        b.putDoubleArray("means", means);
        b.putDoubleArray("sd", sd);
        b.putInt("id", id);
        if (next != null)
            b.putInt("next", next.id);
        return b;
    }

    private static StateWindow fromBundle(Bundle bundle) {
        double[] means = bundle.getDoubleArray("means");
        double[] sd = bundle.getDoubleArray("sd");
        int id = bundle.getInt("id");
        return new StateWindow(means, sd, id);
    }

    public static List<StateWindow> fromBundles(Bundle bundle) {
        return fromJSON(bundle.getString("data"));
//        List<StateWindow> states = new ArrayList<>();
//        SparseArray<StateWindow> stateMap = new SparseArray<>();
//        Bundle b = bundle;
//        do {
//            StateWindow state = fromBundle(b);
//            states.add(state);
//            stateMap.put(state.getId(), state);
//            b = b.getBundle("nextBundle");
//        } while (b != null);
//        b = bundle;
//        do {
//            int id = b.getInt("id");
//            if (b.containsKey("next")) {
//                int next = b.getInt("next");
//                StateWindow state = stateMap.get(id);
//                state.setNext(stateMap.get(next));
//            }
//            b = b.getBundle("nextBundle");
//        } while (b != null);
//        return states;
    }

    public Bundle toBundles() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Bundle b = new Bundle();
        b.putString("data", toJSON());
        return b;
//        Bundle bundle = toBundle();
//        StateWindow s = next;
//        Bundle b = bundle;
//
//        do {
//            Bundle nextBundle = s.toBundle();
//            b.putBundle("nextBundle", nextBundle);
//            b = nextBundle;
//            s = s.getNext();
//        } while (s != null);
//        return bundle;
    }

    public int getId() {
        return id;
    }

    public static Bundle toBundle(List<StateWindow> states) {
        if (states.isEmpty())
            return null;
        return states.get(0).toBundles();
    }

    public double getScore() {
        return 1 / Math.pow((1 + getDistance()), 4);
    }

    public static String toJSON(List<StateWindow> states) {
        return states.get(0).toJSON();
    }

    public String toJSON() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    public static List<StateWindow> fromJSON(String json) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        StateWindow state = gson.fromJson(json, StateWindow.class);
        List<StateWindow> states = new ArrayList<>();

        while (state != null) {
            states.add(state);
            state = state.getNext();
        }
        return states;
    }

    public void setTotalParticles(int totalParticles) {
        this.totalParticles = totalParticles;
    }
}
