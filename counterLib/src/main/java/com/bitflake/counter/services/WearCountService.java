package com.bitflake.counter.services;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class WearCountService extends CountService implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private boolean isConnected;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void startCounting(Bundle data) {
        super.startCounting(data);
        updateData(true, countOffset);
    }

    @Override
    public void onCount(int count) {
        super.onCount(count);
        updateData(true, count + countOffset);
    }

    @Override
    protected void stopCounting() {
        super.stopCounting();
        updateData(false);
    }

    public void updateData(boolean isCounting, int count) {
        if (!isConnected)
            return;
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEAR_STATUS_PATH);
        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putBoolean(DATA_IS_COUNTING, isCounting);
        dataMap.putInt(DATA_COUNT, count);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public void updateData(boolean isCounting) {
        if (!isConnected)
            return;
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(WEAR_STATUS_PATH);
        DataMap dataMap = putDataMapReq.getDataMap();
        dataMap.putBoolean(DATA_IS_COUNTING, isCounting);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        isConnected = true;
    }

    @Override
    public void onConnectionSuspended(int i) {
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        isConnected = false;
    }
}
