package com.bitflake.counter.wear;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.devrel.wcl.WearManager;
import com.google.devrel.wcl.connectivity.WearFileTransfer;
import com.google.devrel.wcl.filters.NearbyFilter;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public class ChannelHelper implements WearFileTransfer.OnChannelReadyListener {


    private final ChannelHelperListener listener;
    private String nodeId;
    private String path;
    private Channel channel;
    private com.google.android.gms.common.api.ResultCallback<? super com.google.android.gms.wearable.Channel.GetOutputStreamResult> outputCallback = new ResultCallback<Channel.GetOutputStreamResult>() {
        @Override
        public void onResult(@NonNull Channel.GetOutputStreamResult getOutputStreamResult) {
            if (getOutputStreamResult.getStatus().isSuccess()) {
                listener.onOutputStreamOpened(getOutputStreamResult.getOutputStream());
            } else {
                listener.onConnectionLost();
            }
        }
    };


    private com.google.android.gms.common.api.ResultCallback<? super com.google.android.gms.wearable.Channel.GetInputStreamResult> inputCallback = new ResultCallback<Channel.GetInputStreamResult>() {
        @Override
        public void onResult(@NonNull Channel.GetInputStreamResult getInputStreamResult) {
            if (getInputStreamResult.getStatus().isSuccess()) {
                listener.onInputStreamOpened(getInputStreamResult.getInputStream());
            } else {
                listener.onConnectionLost();
            }
        }
    };

    private ChannelApi.ChannelListener channelListener = new ChannelApi.ChannelListener() {
        @Override
        public void onChannelOpened(Channel channel) {

        }

        @Override
        public void onChannelClosed(Channel channel, int i, int i1) {
            listener.onConnectionLost();
        }

        @Override
        public void onInputClosed(Channel channel, int i, int i1) {

        }

        @Override
        public void onOutputClosed(Channel channel, int i, int i1) {

        }
    };


    public ChannelHelper(Context context, ChannelHelperListener listener, String nodeId, String path) {
        this.nodeId = nodeId;
        this.path = path;
        this.listener = listener;
        Node node = null;
        if (nodeId == null) {
            Set<Node> nodes = new NearbyFilter().filterNodes(WearManager.getInstance().getConnectedNodes());
            if (nodes.size() > 0) {
                node = nodes.iterator().next();
                WearManager.getInstance().openChannel(node, "/sensor/acceleration", this);
            }
        } else {
            node = WearManager.getInstance().getNodeById(nodeId);
        }
        WearManager.getInstance().openChannel(node, "/sensor/acceleration", this);
    }


    public void closeConnection() {
        if (channel != null)
            channel.close(WearManager.getInstance().getGoogleClient());
    }

    @Override
    public void onChannelReady(int statusCode, Channel channel) {
        Log.d("my", "onChannelReady: " + statusCode);

        if (channel != null) {
            this.channel = channel;
            channel.getOutputStream(WearManager.getInstance().getGoogleClient()).setResultCallback(outputCallback);
            channel.getInputStream(WearManager.getInstance().getGoogleClient()).setResultCallback(inputCallback);
        }
    }

    public interface ChannelHelperListener {
        void onOutputStreamOpened(OutputStream outputStream);

        void onInputStreamOpened(InputStream inputStream);

        void onConnectionLost();
    }
}
