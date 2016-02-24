package com.bitflake.counter.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bitflake.counter.WearConnection;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.HashMap;

public class WearConnectionManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, WearConnection {
    private String TAG = getClass().getSimpleName();
    private static WearConnectionManager sInstance;
    private final GoogleApiClient mGoogleApiClient;
    private NodeApi.GetConnectedNodesResult nodes;
    private boolean isConnected;
    private HashMap<String, WearServiceConnection> connections = new HashMap<>();

    public WearConnectionManager(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static WearConnectionManager init(Context context) {
        return sInstance = new WearConnectionManager(context);
    }

    public static WearConnectionManager getInstance() {
        return sInstance;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "onConnected: " + connectionHint);
        isConnected = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                for (Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mGoogleApiClient, node.getId(), "/start_activity", "Test".getBytes()).await();
                }
            }
        }).start();                        // Now you can use the Data Layer API
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended: " + cause);
        isConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: " + connectionResult);
    }

    public void sendBroadcast(Intent intent) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PREFIX_BROADCAST + intent.getAction());
        DataMap dataMap = putDataMapReq.getDataMap();
        Bundle extras = intent.getExtras();
        dataMap.putAll(DataMap.fromBundle(extras));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }

    public void startService(Intent intent) {
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(PREFIX_START_SERVICE + intent.getAction());
        DataMap dataMap = putDataMapReq.getDataMap();
        Bundle extras = intent.getExtras();
        dataMap.putAll(DataMap.fromBundle(extras));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
    }
}
