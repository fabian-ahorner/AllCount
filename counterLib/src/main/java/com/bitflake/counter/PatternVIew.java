package com.bitflake.counter;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.bitflake.counter.services.CountConstants;

public class PatternView extends View {
    private Paint paintScore;
    private Paint paintParticles;
    private Path path;
    private double maxScore;
    private double maxParticles;
    private float[] particleCount;
    private float[] stateScores;

    public PatternView(Context context) {
        super(context);
    }

    public PatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PatternView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        float strokeWith = getResources().getDisplayMetrics().density * 4;
        paintScore = new Paint();
        paintScore.setColor(getResources().getColor(R.color.colorPrimary));
        paintScore.setStyle(Paint.Style.STROKE);
        paintScore.setStrokeCap(Paint.Cap.ROUND);
        paintScore.setStrokeWidth(strokeWith);

        paintParticles = new Paint();
        paintParticles.setAntiAlias(true);
        paintParticles.setColor(getResources().getColor(R.color.colorAccent));
        paintParticles.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paintParticles.setPathEffect(new CornerPathEffect(strokeWith * 10));   // set the path effect when they join.
        paintParticles.setStyle(Paint.Style.STROKE);
        paintParticles.setStrokeCap(Paint.Cap.ROUND);
        paintParticles.setStrokeWidth(strokeWith);
        path = new Path();
        getContext().registerReceiver(receiver, new IntentFilter(Constances.INTENT_COUNT_PROGRESS));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (particleCount != null) {
//            double maxScore = 0;
//            double maxParticles = 0;
            for (int i = 0; i < particleCount.length; i++) {
                maxScore = Math.max(maxScore, stateScores[i]);
                maxParticles = Math.max(maxParticles, particleCount[i]);
            }

            int w = getWidth() / particleCount.length;
            int h = getHeight();
            path.reset();
            for (int i = 0; i < particleCount.length; i++) {
                double pScore = stateScores[i] / maxScore;
                canvas.drawLine(i * w + w / 2, (int) (h - pScore * h), i * w + w / 2, h, paintScore);
//                canvas.drawCircle(i * w + w / 2, (int) (h - pScore * h), paintParticles.getStrokeWidth() * 3
//                        , paintParticles);

                double pParticle = particleCount[i] / maxParticles;
                paintParticles.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(i * w + w / 2, (int) (h - pParticle * h), paintParticles.getStrokeWidth() * 2
//                        , paintParticles);
                paintParticles.setStyle(Paint.Style.STROKE);
                if (i == 0)
                    path.moveTo(i * w + w / 2, (int) (h - pParticle * h));
                else
                    path.lineTo(i * w + w / 2, (int) (h - pParticle * h));
            }
            canvas.drawPath(path, paintParticles);
        }
    }

    public void setStats(float[] particleCount, float[] stateScores) {
        this.particleCount = particleCount;
        this.stateScores = stateScores;
        invalidate();
    }

    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            particleCount = data.getFloatArray(CountConstants.DATA_PARTICLE_COUNT);
            stateScores = data.getFloatArray(CountConstants.DATA_STATE_SCORES);
            invalidate();
        }
    };
}
