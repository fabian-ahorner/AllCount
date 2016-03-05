package com.bitflake.counter.services;

public interface RecordConstants {
    String WEAR_STATUS_PATH = "/record/status";
    String CMD_START_RECORDING = "startRecording";
    String CMD_STOP_RECORDING = "stopRecording";
    String CMD_SKIP = "skip";
    String CMD_REQUEST_UPDATE = "requestUpdate";

    String EVENT_STATUS = "status";
    String EVENT_START_DELAY = "startDelay";
    String EVENT_START_RECORDING = "startRecording";
    String EVENT_FINISHED_RECORDING = "start";
    String EVENT_STOP_RECORDING = "stopRecording";

    String DATA_EVENT_TYPE = "eventType";
    String DATA_COMMAND = "command";
    String DATA_DELAY_MS = "delay";
    String DATA_STATUS = "status";
    String DATA_STATES = "states";
    String DATA_DURATION_MS = "duration";

    int STATUS_NONE = 0;
    int STATUS_DELAY = 1;
    int STATUS_RECORDING = 2;
    int STATUS_FINISHED = 3;
}
