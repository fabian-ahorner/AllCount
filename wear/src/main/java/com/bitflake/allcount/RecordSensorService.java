package com.bitflake.allcount;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.sensors.SensorValueListener;
import com.bitflake.counter.wear.ChannelHelper;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.Node;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class RecordSensorService extends Service implements SensorValueListener, ChannelHelper.ChannelHelperListener, WearFileTransfer.OnChannelReadyListener {
    private static final String DATA_NODE_ID = "nodeId";
    private static final String DATA_PATH = "path";
    private LocalAccelerationSensor sensor;
    private ChannelHelper channelHelper;
    private DataOutputStream out;

    @Override
    public void onCreate() {
        super.onCreate();
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String nodeId = intent.getStringExtra(DATA_NODE_ID);
            String path = intent.getStringExtra(DATA_PATH);
            channelHelper = new ChannelHelper(this, this, nodeId, path);
            Node node = WearManager.getInstance().getNodeById(nodeId);
            WearManager.getInstance().openChannel(node, "/sensor/acceleration", this);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onValueChanged(float[] values) {
        if (out != null) {
            try {
                for (int i = 0; i < values.length; i++) {
                    out.writeFloat(values[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onOutputStreamOpened(OutputStream outputStream) {
        this.out = new DataOutputStream(outputStream);
    }

    @Override
    public void onInputStreamOpened(final InputStream inputStream) {
        Log.d("my", "onInputStreamOpened");

        new Thread() {
            @Override
            public void run() {
                try {
                    int v;
                    Log.d("my", "Start listening ");
                    while ((v = inputStream.read()) != -1) {
                        Log.d("my", "In received: " + v);
                        if (v == 0) {
                            sensor.stopListening();
                        } else if (v == 1) {
                            sensor.startListening();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    public void onConnectionLost() {
        Log.d("my", "onConnectionLost");

        sensor.stopListening();
        stopSelf();
    }

    public static void start(Context context, Channel channel) {
        String nodeId = channel.getNodeId();
        String path = channel.getPath();
        start(context, nodeId, path);
    }

    public static void start(Context context, String nodeId, String path) {
        Intent i = new Intent(context, RecordSensorService.class);
        i.putExtra(DATA_NODE_ID, nodeId);
        i.putExtra(DATA_PATH, path);
        context.startService(i);
    }

    @Override
    public void onChannelReady(int statusCode, Channel channel) {
        Log.d("my", "onChannelReady: " + statusCode);
        if (channel != null) {
            channel.getOutputStream(WearManager.getInstance().getGoogleClient()).setResultCallback(outputCallback);
            channel.getInputStream(WearManager.getInstance().getGoogleClient()).setResultCallback(inputCallback);
        }
        sensor.start();
    }

    private ResultCallback<Channel.GetOutputStreamResult> outputCallback = new ResultCallback<Channel.GetOutputStreamResult>() {
        @Override
        public void onResult(@NonNull Channel.GetOutputStreamResult getOutputStreamResult) {
            Log.d("my", "onOutputStreamOpened(" + getOutputStreamResult.getStatus());
            OutputStream output = getOutputStreamResult.getOutputStream();
            if (output != null) {
                onOutputStreamOpened(output);
            }
        }
    };
    private ResultCallback<Channel.GetInputStreamResult> inputCallback = new ResultCallback<Channel.GetInputStreamResult>() {
        @Override
        public void onResult(@NonNull Channel.GetInputStreamResult getOutputStreamResult) {
            Log.d("my", "onInputStreamOpened(" + getOutputStreamResult.getStatus());
            InputStream input = getOutputStreamResult.getInputStream();
            if (input != null) {
                onInputStreamOpened(input);
            }
        }
    };
}
