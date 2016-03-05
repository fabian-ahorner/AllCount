package com.bitflake.allcount;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bitflake.counter.sensors.LocalAccelerationSensor;
import com.bitflake.counter.sensors.SensorValueListener;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.devrel.wcl.WearManager;

public class DataMapRecordService extends Service implements SensorValueListener {
    private LocalAccelerationSensor sensor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensor = new LocalAccelerationSensor(this);
        sensor.setValueListener(this);
        sensor.start();
    }

    @Override
    public void onValueChanged(float[] values) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/sensor/acceleration");
        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putFloatArray("values", values);
        Log.d("my", "send Values");
        //        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        WearManager.getInstance().putDataItem(putDataMapReq.asPutDataRequest());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensor.stop();
    }
}
