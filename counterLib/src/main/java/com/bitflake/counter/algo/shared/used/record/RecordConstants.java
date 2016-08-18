package com.bitflake.counter.algo.shared.used.record;

public interface RecordConstants extends SensorConstants {
    String INTENT_RECORD_STATUS = "com.bitflake.counter.record.status";
    String INTENT_RECORD_PROGRESS = "com.bitflake.counter.record.progress";
    String INTENT_RECORD_CONTROL = "com.bitflake.counter.record.control";

    String WEAR_STATUS_PATH = "/record/status";
    String CMD_START_RECORDING = "startRecording";
    String CMD_STOP_RECORDING = "stopRecording";
    String CMD_SKIP = "skip";

    String EVENT_START_DELAY = "startDelay";
    String EVENT_START_CALIBRATING = "startCalibrating";
    String EVENT_FINISHED_RECORDING = "finished";
    String EVENT_STOP_RECORDING = "stopRecording";
    String EVENT_START_MOVING = "EVENT_START_MOVING";
    String EVENT_START_MOVE_BACK = "EVENT_START_MOVE_BACK";

    String DATA_DELAY_MS = "delay";
    String DATA_STILLNESS = "stillness";
    String DATA_STATUS = "status";
    String DATA_STATES = "elements";
    String DATA_DURATION_MS = "duration";
    String DATA_REMAINING_TIME = "remainingTime";

    int STATUS_NONE = 0;
    int STATUS_DELAY = 1;
    int STATUS_RECORDING = 2;
    int STATUS_FINISHED = 3;
}
