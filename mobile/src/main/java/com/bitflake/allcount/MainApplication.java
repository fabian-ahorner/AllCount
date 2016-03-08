package com.bitflake.allcount;

import android.support.multidex.MultiDexApplication;

import com.bitflake.counter.wear.WearConnectionManager;
import com.google.devrel.wcl.WearManager;
import com.orm.SugarContext;

public class MainApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SugarContext.init(this);
        WearConnectionManager.init(this);
        WearManager.initialize(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

}
