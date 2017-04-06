package com.bitflake.counter.algo.shared.current.count;

public class CountSettings {
    public static final int TRANSIENT_STATES = 1;
    public static double VALID_START_ANGLE = Math.PI / 4;
    public static double MIN_FIRST_ANGLE = 0.5;
    public static double MIN_GOAL_LIKELIHOOD = 0.25;
    public static double SORT_OUT_DISTANCE = 1;
    //    public static double MIN_START_DISTANCE = 1;
    public static double MIN_START_DISTANCE = 0.1;
    public static double MOVING_FRACTION = 0.5;
    public static double EXTRACT_SPS = 5;
    public static double RESET_DISTANCE = 0.5;
    public static double RESET_STRICTNESS = 3;
    public static double NEXT_POWER = 0.1;
    public static double MIN_UPDATE_DISTANCE = 2;
    public static double MOVE_LIKELIHOOD = 1;
    public static double BACK_STEP_PROBABILITY = 0.005;
    public static double MOVE_DRIVE = 14;
    public static double STRONG_STATE_DRIVE = 3;
    public static double STRONG_PARTICLE_HISTORY_DRIVE = 5;
    public static double STRONG_PARTICLE_LIKELIHOOD_DRIVE = 0.1;

    public static double RESAMPLE_LIKELIHOOD = 0.9;
    public static double RESET_LIKELIHOOD = 2;
    public static int PARTICLE_COUNT = 100;
    public static double PARTICLE_COUNT_THRESHOLD = 0.25; //1.5
    public static double STATE_DISCRETE_STEPS = 4;
    public static double START_PARTICLES = 0.09;
    public static double SAMPLES_PER_SECOUND = 20;
    public static double BACK_PANANLTY = 1;
    public static double DISTANCE_TO_INIT = 0.04;
    public static double RESAMPLE_STEEPNESS = 8;
}
