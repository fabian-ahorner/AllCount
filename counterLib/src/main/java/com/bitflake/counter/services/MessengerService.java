package com.bitflake.counter.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class MessengerService extends Service {

    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            MessengerService.this.handleMessage(msg);
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    public boolean handleMessage(Message msg) {
        return false;
    }

    public static boolean sendMessage(Messenger m, Message msg) {
        try {
            m.send(msg);
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    public static void sendSignalMessage(Messenger m, int what) {
        Message msg = Message.obtain(null,
                what, 0, 0);
        sendMessage(m, msg);
    }

    public static void sendSignalMessage(Messenger m, Messenger responseM, int what) {
        Message msg = Message.obtain(null,
                what, 0, 0);
        msg.replyTo = responseM;
        sendMessage(m, msg);
    }
}
