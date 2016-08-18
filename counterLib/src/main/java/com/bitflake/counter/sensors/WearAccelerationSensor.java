package com.bitflake.counter.sensors;


import android.content.Context;
import android.util.Log;

import com.bitflake.counter.algo.shared.old.SensorDataProvider;
import com.bitflake.counter.wear.ChannelHelper;
import com.bitflake.counter.wear.WearConnectionManager;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class WearAccelerationSensor extends SensorDataProvider implements ChannelHelper.ChannelHelperListener, WearFileTransfer.OnChannelReadyListener, DataApi.DataListener {
    private ChannelHelper channelHelper;
    private OutputStream out;

    public WearAccelerationSensor(final Context context) {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                channelHelper = new ChannelHelper(context, WearAccelerationSensor.this, null, "/sensor/acceleration");
//            }
//        }.start();
    }

    @Override
    public void start() {
        WearConnectionManager.getInstance().startService("com.bitflake.allcount.DataMapRecordService", null);
        Wearable.DataApi.addListener(WearManager.getInstance().getGoogleClient(), this);
//        if (out != null)
//            try {
//                out.write(1);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

//        Set<Node> nodes = new NearbyFilter().filterNodes(WearManager.getInstance().getConnectedNodes());
//        if (nodes.size() > 0) {
//            Node node = nodes.iterator().next();
//            WearManager.getInstance().openChannel(node, "/sensor/acceleration", this);
//        } else {
//            Log.d("my", "no nearby nodes");
//        }
    }

    @Override
    public void stop() {
        WearConnectionManager.getInstance().stopService("com.bitflake.allcount.DataMapRecordService", null);
        Wearable.DataApi.removeListener(WearManager.getInstance().getGoogleClient(), this);
        if (out != null)
            try {
                out.write(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public void destroy() {
        super.destroy();
//        channelHelper.closeConnection();
    }

    @Override
    public void onOutputStreamOpened(OutputStream outputStream) {
        Log.d("my", "onOutputStreamOpened");
        out = outputStream;
    }

    @Override
    public void onInputStreamOpened(final InputStream inputStream) {
        Log.d("my", "onInputStreamOpened");

        new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        DataInputStream in = new DataInputStream(inputStream);
                        float[] values = new float[3];
                        for (int i = 0; i < 3; i++) {
                            values[i] = in.readFloat();
                        }
                        if (isListening()) {
                            Log.d("my", "notifyValueChanged");
                            notifyValueChanged(values);
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
    }

    @Override
    public void onChannelReady(int statusCode, Channel channel) {
        Log.d("my", "onChannelReady(" + statusCode);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                String path = item.getUri().getPath();
                if (path.equals("/sensor/acceleration")) {
                    Log.d("my", "dataReceived!");
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    notifyValueChanged(dataMap.getFloatArray("values"));
                }
            }
        }
    }
}
