package com.bitflake.counter.tools;

import android.os.Bundle;
import android.os.Parcel;

public class BundleHelper {
    public static byte[] fromBundle(Bundle b) {
        if (b == null)
            return null;
        Parcel p = Parcel.obtain();
        b.writeToParcel(p, 0);
        return p.marshall();
    }

    public static Bundle toBundle(byte[] data) {
        if (data == null)
            return null;
        Parcel p = Parcel.obtain();
        p.unmarshall(data, 0, data.length);
        p.setDataPosition(0); // this is extremely important!
        return p.readBundle();
    }
}
