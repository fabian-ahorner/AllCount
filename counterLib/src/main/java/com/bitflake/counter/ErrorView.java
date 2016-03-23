package com.bitflake.counter;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.bitflake.counter.services.RecordConstants;

public class ErrorView extends View {
    private Paint paint;
    private float maxError;
    private double[] errors;
    private Paint paintAccent;

    public ErrorView(Context context) {
        super(context);
        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        int colorPrimary = context.getResources().getColor(R.color.colorPrimary);
        int colorAccent = context.getResources().getColor(R.color.colorAccent);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colorPrimary);
        paintAccent = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintAccent.setColor(colorAccent);
        context.registerReceiver(receiver, new IntentFilter(RecordConstants.INTENT_RECORD_PROGRESS));
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            errors = intent.getDoubleArrayExtra(RecordConstants.DATA_ERRORS);
            for (int i = 0; i < errors.length; i++) {
                maxError = (float) Math.max(maxError, errors[i]);
            }
            invalidate();
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (errors != null) {
            float w = getWidth() / errors.length;
            float h = getHeight();
            for (int i = 0; i < errors.length; i++) {
                canvas.drawRect(i * w, (float) (errors[i] / maxError) * h, (i + 1) * w, h, paint);
            }
            canvas.drawLine(0, h / maxError, getWidth(), h / maxError, paintAccent);
        }
    }
}
