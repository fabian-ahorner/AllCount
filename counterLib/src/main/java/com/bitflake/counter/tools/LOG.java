package com.bitflake.counter.tools;

import android.util.Log;

public class LOG {
    private static String DEFAULT_TAG = "bitflake";
    private static boolean sDebug = true;

    public static void setDebugingEnabled(boolean debug) {
        sDebug = true;
    }

    public static void d(String tag, String msg) {
        if (sDebug)
            Log.d(tag, msg);
    }

    public static void d(Object c, String msg) {
        d(c.getClass(), msg);
    }

    public static void d(Class c, String msg) {
        Log.d(DEFAULT_TAG, msg + " |" + c.getSimpleName());
    }

    public static void d(Object c, String msg, Object... args) {
        d(c.getClass(), String.format(msg, args));
    }

}