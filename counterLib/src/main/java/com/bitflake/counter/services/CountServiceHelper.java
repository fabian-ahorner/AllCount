package com.bitflake.counter.services;

import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;

import com.bitflake.counter.MessengerService;

public class CountServiceHelper {
    public interface Constants {
        int MSG_START_COUNTING = 1;
        int MSG_STOP_COUNTING = 2;
        int MSG_START_LISTENING = 3;
        int MSG_STOP_LISTENING = 5;
        int MSG_RESET_COUNTER = 6;

        int MSG_RESP_STATUS = 1;
        int MSG_RESP_COUNT = 2;
        int MSG_RESP_COUNT_PROGRESS = 3;

        String DATA_STATES = "states";
        String DATA_COUNT_PROGRESS = "countProgress";
        String DATA_COUNT = "countNr";
        String DATA_IS_COUNTING = "isCounting";
    }

    public static void stopCounting(Messenger serviceMessenger) {
        MessengerService.sendSignalMessage(serviceMessenger, Constants.MSG_STOP_COUNTING);
    }

    public static void startCounting(Messenger serviceMessenger, Messenger incomingMessenger, Bundle states) {
        Message msg = Message.obtain(null,
                Constants.MSG_START_COUNTING, 0, 0);
        msg.replyTo = incomingMessenger;
        Bundle b = new Bundle();
        b.putBundle(Constants.DATA_STATES, states);
        msg.setData(b);
        MessengerService.sendMessage(serviceMessenger, msg);
    }

    public static void startListening(Messenger serviceMessenger, Messenger incomingMessenger) {
        MessengerService.sendSignalMessage(serviceMessenger, incomingMessenger, Constants.MSG_START_LISTENING);
    }
}
