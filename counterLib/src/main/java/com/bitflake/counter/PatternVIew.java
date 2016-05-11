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
    private Path pathParticles;
    private Path pathScores;
    private double maxScore;
    private double maxParticles;
    private float[] particleCount;
    private float[] stateScores;
    private float density;

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
        this.density = getResources().getDisplayMetrics().density;
        float strokeWith = density * 4;
        paintScore = new Paint();
        paintScore.setColor(getResources().getColor(R.color.colorPrimary));
        paintScore.setStyle(Paint.Style.FILL);
        paintScore.setStrokeCap(Paint.Cap.ROUND);
        paintScore.setPathEffect(new CornerPathEffect(strokeWith * 10));   // set the pathParticles effect when they join.
        paintScore.setStrokeWidth(strokeWith);


        paintParticles = new Paint();
        paintParticles.setAntiAlias(true);
        paintParticles.setColor(getResources().getColor(R.color.colorAccent));
        paintParticles.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        paintParticles.setPathEffect(new CornerPathEffect(strokeWith * 10));   // set the pathParticles effect when they join.
        paintParticles.setStyle(Paint.Style.STROKE);
        paintParticles.setStrokeCap(Paint.Cap.ROUND);
        paintParticles.setStrokeWidth(strokeWith);
        pathParticles = new Path();
        pathScores = new Path();
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
            pathParticles.reset();
            pathParticles.moveTo(0, getHeight());

            pathScores.reset();
            pathScores.moveTo(0, getHeight());
            pathScores.lineTo(0, getHeight());
            for (int i = 0; i < particleCount.length; i++) {
                double pScore = stateScores[i] / maxScore;
//                canvas.drawLine(i * w + w / 2, (int) (h - pScore * h), i * w + w / 2, h, paintScore);

                pathScores.lineTo(i * w + w / 2, (int) (pScore * h));
//                canvas.drawCircle(i * w + w / 2, (int) (h - pScore * h), paintParticles.getStrokeWidth() * 3
//                        , paintParticles);

                double pParticle = particleCount[i] / maxParticles;
                paintParticles.setStyle(Paint.Style.FILL);
//                canvas.drawCircle(i * w + w / 2, (int) (h - pParticle * h), paintParticles.getStrokeWidth() * 2
//                        , paintParticles);
                paintParticles.setStyle(Paint.Style.STROKE);
                pathParticles.lineTo(i * w + w / 2, (int) (h - pParticle * h));
            }
            pathScores.lineTo(getWidth(), getHeight());
            pathScores.lineTo(getWidth(), getHeight());
            pathParticles.lineTo(getWidth(), getHeight());
            pathScores.close();
            canvas.drawPath(pathScores, paintScore);
            canvas.drawPath(pathParticles, paintParticles);
            float maxDistance = (float) (h / maxScore);
            canvas.drawLine(0, maxDistance, getWidth(), maxDistance, paintParticles);
        }

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
