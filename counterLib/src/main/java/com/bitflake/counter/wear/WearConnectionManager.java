package com.bitflake.counter.wear;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bitflake.counter.WearConnection;
import com.bitflake.counter.tools.BundleHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

public class WearConnectionManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, WearConnection {
    private final String targetPrefix;
    private String TAG = getClass().getSimpleName();
    private static WearConnectionManager sInstance;
    private final GoogleApiClient mGoogleApiClient;
    private NodeApi.GetConnectedNodesResult nodes;
    private boolean isConnected;

    public WearConnectionManager(Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        boolean isRunningOnWatch = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT_WATCH) {
            isRunningOnWatch = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
        }
        this.targetPrefix = "/" + (isRunningOnWatch ? "w" : "m");
    }

    public static WearConnectionManager init(Context context) {
        return sInstance = new WearConnectionManager(context);
    }

    public static WearConnectionManager getInstance() {
        return sInstance;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
//        Log.d(TAG, "onConnected: " + connectionHint);
        isConnected = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            }
        }).start();
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
        String path = PREFIX_BROADCAST + intent.getAction();
        sendDataToWatch(path, intent.getExtras());
    }

    public void startService(String serviceName, Bundle extras) {
        String path = PREFIX_START_SERVICE + serviceName;
        sendDataToWatch(path, extras);
    }

    public void stopService(String serviceName, Bundle extras) {
        String path = PREFIX_STOP_SERVICE + serviceName;
        sendDataToWatch(path, extras);
    }

    public void sendMessage(final String path, byte[] bytes) {
        for (Node node : nodes.getNodes()) {
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), path, bytes);
        }
    }

    public synchronized void sendDataToWatch(String path, Bundle extras) {
        if (!isConnected)
            return;
        path = targetPrefix + path;
        byte[] bytes = BundleHelper.fromBundle(extras);
//        if (extras == null) {
        sendMessage(path, bytes);
//        } else {
//            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
//            DataMap dataMap = putDataMapReq.getDataMap();
//            dataMap.putAll(DataMap.fromBundle(extras));
//            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//            Log.d("my", this.toString() + " sendDataToWatch(" + path + ")");
//        }
    }

//    public void putData(PutDataRequest data) {
//        if (!isConnected)
//            return;
//        PutDataMapRequest putDataMapReq = PutDataMapRequest.create(path);
//        DataMap dataMap = putDataMapReq.getDataMap();
//        dataMap.putAll(DataMap.fromBundle(extras));
//        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//    }
//
//    public DataMap getData(String path) {
//        if (!isConnected)
//            return null;
//        Wearable.DataApi.getDataItem(mGoogleApiClient,Uri.)
//    }
}
