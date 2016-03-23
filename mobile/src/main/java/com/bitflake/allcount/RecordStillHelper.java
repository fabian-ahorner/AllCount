package com.bitflake.allcount;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.bitflake.counter.services.RecordConstants;

public class RecordStillHelper extends BroadcastReceiver implements RecordConstants {
    private final View view;

    public RecordStillHelper(View view) {
        this.view = view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(DATA_STILLNESS)) {
            float stillness = intent.getFloatExtra(DATA_STILLNESS, 0);
            float current = view.getTranslationY();
            float goal = view.getHeight() * stillness;
            float distance = Math.abs(current - goal);
            int duration = (int) (distance * 300 / view.getHeight());
//            view.setTranslationY();
            view.animate().translationY(goal).setDuration(duration);
        }
    }
}
