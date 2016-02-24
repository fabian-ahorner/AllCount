package com.bitflake.counter.services;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

public class RecordServiceHelper {

    public interface Constants {
        int MSG_START_RECORDING = 1;
        int MSG_STOP_RECORDING = 2;
        int MSG_START_LISTENING = 3;
        int MSG_STOP_LISTENING = 5;
        int MSG_SKIP_STATE = 6;

        int MSG_RESP_STATUS = 1;
        int MSG_RESP_START_DELAY = 2;
        int MSG_RESP_START_RECORDING = 3;
        int MSG_RESP_STOPPED_RECORDING = 4;
        int MSG_RESP_FINISHED_RECORDING = 5;

        String DATA_DELAY_MS = "delay";
        String DATA_DURATION_MS = "duration";

        int STATUS_NONE = 0;
        int STATUS_DELAY = 1;
        int STATUS_RECORDING = 2;
        int STATUS_FINISHED = 3;
    }

    public static void startRecording(Messenger serviceMessenger, Messenger incomingMessenger, int delay, int duration) {
        Message msg = Message.obtain(null,
                Constants.MSG_START_RECORDING, 0, 0);
        Bundle b = new Bundle();
        b.putInt(Constants.DATA_DELAY_MS, delay);
        b.putInt(Constants.DATA_DURATION_MS, duration);
        msg.setData(b);
        msg.replyTo = incomingMessenger;
        MessengerService.sendMessage(serviceMessenger, msg);
    }

    public static void stopRecording(Messenger serviceMessenger) {
        MessengerService.sendSignalMessage(serviceMessenger, Constants.MSG_STOP_RECORDING);
    }

    public static void startListening(Messenger serviceMessenger, Messenger incomingMessenger) {
        MessengerService.sendSignalMessage(serviceMessenger, incomingMessenger, Constants.MSG_START_LISTENING);
    }

    public static void skipState(Messenger serviceMessenger) {
        MessengerService.sendSignalMessage(serviceMessenger, Constants.MSG_SKIP_STATE);
    }

}
