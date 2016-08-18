package com.bitflake.counter.algo.shared;

/**
 * Created by fahor on 11/07/2016.
 */
public class CountSettings {
    public static final int TRANSIENT_STATES = 1;
    public static double EXTRACT_SPS = 5;
    public static double RESET_DISTANCNE = 5;
    public static double RESET_STRICTNESS = 3;
    public static double NEXT_POWER = 0.1;
    public static double MIN_UPDATE_DISTANCE = 0.1;
    public static double MOVE_LIKELIHOOD = 1;
    public static double BACK_STEP_PROBABILITY = 0.005;
    public static double MOVE_DRIVE = 6;
    public static double STRONG_STATE_DRIVE = 2;
    public static double STRONG_PARTICLE_HISTORY_DRIVE = 5;
    public static double STRONG_PARTICLE_LIKELIHOOD_DRIVE = 0.1;

    public static double RESAMPLE_LIKELIHOOD = 0.9;
    public static double RESET_LIKELIHOOD = 2;
    public static int PARTICLE_COUNT = 100;
    public static double PARTICLE_COUNT_THRESHOLD = 0.35; //1.5
    public static double STATE_DISCRETE_STEPS = 4;
    public static double START_PARTICLES = 0.09;
    public static double SAMPLES_PER_SECOUND = 20;
}
