package com.bitflake.counter.wear;

import android.app.Application;

import com.google.devrel.wcl.WearManager;

public class WearConnectionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        WearConnectionManager.init(this);
        WearManager.initialize(this);
    }
}
