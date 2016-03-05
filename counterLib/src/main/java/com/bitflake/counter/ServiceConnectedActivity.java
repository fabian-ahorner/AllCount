package com.bitflake.counter;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

public class ServiceConnectedActivity extends AppCompatActivity {
    public HashMap<Class<? extends Service>, ServiceConnection> connections = new HashMap<>();

    public void ensureConnection(Class<? extends Service> serviceClass) {
        DummyServiceConnection connection = new DummyServiceConnection();
        connections.put(serviceClass, connection);
        bindService(new Intent(this, serviceClass), connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ServiceConnection connection : connections.values()) {
            unbindService(connection);
        }
    }

    private class DummyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}
