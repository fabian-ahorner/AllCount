package com.bitflake.counter.services;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public abstract class MessengerServiceConnection {
    protected Messenger incomming, outgoing;
    protected ConnectionListener connectionListener;

    public MessengerServiceConnection(ConnectionListener listener) {
        this.incomming = new Messenger(new IncomingHandler());
        this.connectionListener = listener;
    }

    public Messenger getOutgoingMessenger() {
        return outgoing;
    }

    public void send(Message msg) throws RemoteException {
        if (outgoing != null) {
            if (msg.replyTo == null)
                msg.replyTo = incomming;
            outgoing.send(msg);
        }
    }

    public void disconnect(Context context) {
        connectionListener.onDisconnected();
        outgoing = null;
    }

    public abstract void connect(Context context);

    public interface ConnectionListener {
        void onConnected(Messenger outgoing);

        void onDisconnected();

        void handleMessage(Message msg);
    }

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            connectionListener.handleMessage(msg);
        }
    }

}
