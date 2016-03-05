package com.bitflake.counter.services;

public interface CountConstants {
    String WEAR_STATUS_PATH = "/count/status";

    String CMD_START_COUNTING = "startCounting";
    String CMD_STOP_COUNTING = "stopCounting";
    String CMD_REQUEST_UPDATE = "requestUpdate";

    String EVENT_STATUS = "status";
    String EVENT_COUNT = "count";
    String EVENT_STOP_COUNTING = "stop";
    String EVENT_START_COUNTING = "start";
    String EVENT_RESET = "reset";

    String DATA_EVENT_TYPE = "eventType";
    String DATA_COMMAND = "command";
    String DATA_STATES = "states";
    String DATA_COUNT_PROGRESS = "countProgress";
    String DATA_PARTICLE_COUNT = "particleCount";
    String DATA_STATE_SCORES = "stateScores";
    String DATA_COUNT = "countNr";
    String DATA_IS_COUNTING = "isCounting";
    String DATA_COUNT_OFFSET = "countOffset";
}
