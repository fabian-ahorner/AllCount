package com.bitflake.counter.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Messenger;

public class DirectMessengerServiceConnection extends MessengerServiceConnection implements ServiceConnection {

    private final Class<? extends Service> serviceClass;

    public DirectMessengerServiceConnection(Class<? extends Service> serviceClass, ConnectionListener listener) {
        super(listener);
        this.serviceClass = serviceClass;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        outgoing = new Messenger(service);
        if (connectionListener != null) {
            connectionListener.onDisconnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        outgoing = null;
        if (connectionListener != null) {
            connectionListener.onDisconnected();
        }
    }

    @Override
    public void connect(Context context) {
        Intent intent = new Intent(context, serviceClass);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void disconnect(Context context) {
        super.disconnect(context);
        context.unbindService(this);
    }
}
