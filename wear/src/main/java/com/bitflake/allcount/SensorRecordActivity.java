package com.bitflake.allcount;

import android.os.Bundle;
import android.util.Log;

import com.bitflake.counter.ServiceConnectedActivity;
import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.sensors.SensorValueListener;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.devrel.wcl.WearManager;

public class SensorRecordActivity extends ServiceConnectedActivity implements SensorValueListener {
    private LocalAccelerationSensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
        sensor.start();
    }

    @Override
    public void onValueChanged(float[] values) {

        if (WearManager.getInstance().isConnected()) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor/acceleration");
            DataMap dataMap = putDataMapReq.getDataMap();
            dataMap.putFloatArray("values", values);
            dataMap.putLong("time", System.currentTimeMillis());
            //        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
            Log.d("my", "send Values");
            WearManager.getInstance().putDataItem(putDataMapReq.asPutDataRequest());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensor.stop();
    }
}
