package com.bitflake.counter.services;

import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.text.format.Formatter;
import android.util.Log;

import com.bitflake.counter.SensorProxy;
import com.bitflake.counter.algo.shared.SlidingWindow;
import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.algo.shared.old.SensorValueListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxySensorService extends BroadcastReceiverService implements SensorValueListener {
    protected SlidingWindow window = new SlidingWindow(3, 20);
    private LocalAccelerationSensor sensor;
    private PowerManager.WakeLock mWakeLock;
    private ServerSocket serverSocket;
    private Socket connection;
    private DataOutputStream connectionOut;
    private long startTime;

    @Override
    public void onCreate() {
        super.onCreate();
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
        startServer(5647);
    }

    public void stopListening() {
        sensor.stopListening();
        if (mWakeLock != null && mWakeLock.isHeld())
            mWakeLock.release();
    }

    public void startListening() {
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Allcount");
        mWakeLock.acquire();
        sensor.startListening();
        startTime = -1;
    }

    public boolean isListening() {
        return sensor.isListening();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensor.destroy();
    }

    public void setAnalyser(SlidingWindow.WindowAnalyser analyser) {
        window.setAnalyser(analyser);
    }

    @Override
    public void onValueChanged(float[] values) {
        try {
            if (startTime < 0)
                startTime = System.nanoTime();
            int time = (int) ((System.nanoTime() - startTime) / 1000_000);
            if (time > 1000)
                Log.d("my", "time=" + time);
//            time = 123;
            connectionOut.writeInt(time);
            for (int sensor = 0; sensor < values.length; sensor++) {
                connectionOut.writeFloat(values[sensor]);
            }
            connectionOut.writeInt(123);
        } catch (IOException e) {
            e.printStackTrace();
            stopListening();
        }
    }

    public void startServer(final int port) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    serverSocket = new ServerSocket(port);
                    WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
                    String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    Log.d("Server", "Started server: " + ip);
                    while (true) {
                        connection = serverSocket.accept();
                        Log.d("Server", "Opened connection");
                        try {
                            connectionOut = new DataOutputStream(connection.getOutputStream());
                            DataInputStream in = new DataInputStream(connection.getInputStream());
                            int command;
                            do {
                                command = in.readInt();
                                switch (command) {
                                    case SensorProxy.RECORD:
                                        Log.d("Server", "Server record");
                                        startListening();
                                        break;
                                    case SensorProxy.STOP_RECORDING:
                                        Log.d("Server", "Server stop recording");
                                        stopListening();
                                        connectionOut.writeInt(-1);
                                        break;
                                }
                            } while (command != SensorProxy.DISSCONNECT);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            connection.close();
                        }
                        Log.d("Server", "Lost connection");

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (serverSocket != null)
                            serverSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("Server", "Server stopped");
            }
        }.start();
    }
}
