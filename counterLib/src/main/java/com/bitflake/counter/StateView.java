package com.bitflake.counter;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import com.bitflake.counter.algo.shared.old.CountState;
import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.RecordConstants;
import com.bitflake.counter.algo.shared.old.ValueHelper;
import com.bitflake.counter.tools.CountStateHelper;

import java.util.ArrayList;
import java.util.List;

public class StateView extends View {
    private static final int STILL_WINDOWS = 40;
    private static final double MAX_SD = 2;
    private List<CountState> states;
    private List<CountState> tempStates = new ArrayList<>();
    private int sensors = 3;
    private Path path;
    private ValueHelper.Min min = new ValueHelper.Min();
    private ValueHelper.Max max = new ValueHelper.Max();
    private ValueHelper.Min newMin = new ValueHelper.Min();
    private ValueHelper.Max newMax = new ValueHelper.Max();

    private Paint paintStates;
    private float density;
    private float mlsX;
    private boolean isRecording;
    private float stateWith;

    private int[] colors = new int[]{
            0xFFFF00FF, 0xFFFFFF00, 0xFF00FFFF
    };
    private boolean isRegistered;
    private List<CountState> compressedStates;
    private int depth = 100;
    private int stillFrames;
    private CountState firstState;

    public StateView(Context context) {
        super(context);
    }

    public StateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StateView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        this.sensors = 3;
        min.clear();
        max.clear();
        for (int s = 0; s < states.size(); s++) {
            CountState state = states.get(s);
            for (int i = 0; i < sensors; i++) {
                min.addValue(state.means[i] );
                max.addValue(state.means[i] );
            }
        }
        stateWith = 0;

//        compressedStates = new ArrayList<>(states);
//        compressedStates = StateExtractor.compressStates(compressedStates, true, depth);
        firstState = states.get(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.density = getResources().getDisplayMetrics().density;
        path = new Path();
        paintStates = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStates.setStrokeWidth(density * 2);
//        paintStates.setPathEffect(new CornerPathEffect(density * 10));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isRegistered)
            getContext().unregisterReceiver(receiver);
    }

    public void listenToCounter() {
        getContext().registerReceiver(receiver, new IntentFilter(Constances.INTENT_COUNT_PROGRESS));
        isRegistered = true;
    }

    public void listenToRecorder() {
        getContext().registerReceiver(receiver, new IntentFilter(RecordConstants.INTENT_RECORD_PROGRESS));
        states = new ArrayList<>();
        isRecording = true;
        isRegistered = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stateWith == 0 && states != null && states.size() > 1) {
            stateWith = getWidth() / (states.size() - 1);
//            paintStates.setPathEffect(new CornerPathEffect(getWidth() / 15));   // set the pathParticles effect when they join.
        }
        drawStates(canvas);
    }

    private void drawStates(Canvas canvas) {

        if (states != null) {
            float r = Math.min(stateWith / 5, getWidth() / 100);
//            r *= 5;
            for (int s = 0; s < sensors; s++) {
                path.reset();
                paintStates.setColor(0xFFFFFF);
                paintStates.setAlpha(0xFF);
                paintStates.setStyle(Paint.Style.FILL_AND_STROKE);
                paintStates.setColor(colors[s]);

                for (int state = 0; state < states.size(); state++) {
                    float y = getY(s, state);
                    float x = getX(state);
                    if (path.isEmpty())
                        path.moveTo(x, y);
                    path.lineTo(x, y);
//                    if (compressedStates != null && compressedStates.contains(states.get(state)) )
//                        canvas.drawLine(x, 0, x, getHeight(), paintStates);
                    canvas.drawCircle(x, y, r / (states.get(state).isTransientState() ? 2 : 1), paintStates);
                }
                paintStates.setAlpha(0xFF);
                paintStates.setStyle(Paint.Style.STROKE);
                paintStates.setStrokeWidth(density * 3);
                canvas.drawPath(path, paintStates);
            }

            if (lastState != null && !isRecording) {
//            float x = mostLikelyState * getWidth() / states.size();
                showValues(canvas, lastState, mlsX);
            } else if (isRecording && firstState != null) {
                showValues(canvas, firstState, getWidth());
            }
        }
    }

    private void showValues(Canvas canvas, CountState state, float x) {
        for (int s = 0; s < sensors; s++) {
            float y = getY(s, state);
            paintStates.setColor(colors[s]);
            paintStates.setStrokeWidth(1);
//            canvas.drawCircle(x, (yMin + yMax) / 2, density * 8, paintStates);
            canvas.drawLine(0, y, getWidth(), y, paintStates);
//            paintStates.setStrokeWidth(2 * density);
//            canvas.drawLine(x, yMin, x, yMax, paintStates);
        }
    }

    public float getY(int sensor, int state) {
        return getY(sensor, states.get(state));
    }

    public float getY(int sensor, CountState state) {
        double value = state.means[sensor];
        double valueNormalised = (value - this.min.getValue()) / (max.getValue() - this.min.getValue());

        return getHeight() - (float) valueNormalised * getHeight();
    }

    public float getY(double value) {
        double valueNormalised = (value - this.min.getValue()) / (max.getValue() - this.min.getValue());

        return getHeight() - (float) valueNormalised * getHeight();
    }

    public float getX(int state) {
        return stateWith * state;
    }

    private CountState lastState;
    private int mostLikelyState;
    public BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            if (intent.hasExtra(CountConstants.DATA_LAST_STATE)) {
                lastState = CountStateHelper.stateFromJSON(data.getString(CountConstants.DATA_LAST_STATE));
                if (isRecording)
                    addState(lastState);
            }

            if (data.containsKey(CountConstants.DATA_MOST_LIKELY_STATE)) {
                mostLikelyState = data.getInt(CountConstants.DATA_MOST_LIKELY_STATE);
                float newMlsX = getX(mostLikelyState);
                if (newMlsX < mlsX)
                    mlsX = -density * 8;
                ObjectAnimator.ofFloat(StateView.this, "mlsX", newMlsX).start();
            }
            invalidate();
        }
    };

    private void addState(CountState state) {
        if (firstState == null)
            firstState = state;
        states.add(state);
        while (states.size() > 80)
            states.remove(0);
        if (states.size() > 1) {
            if (min.hasValue())
                newMin.addValue(min.getValue());
            if (max.hasValue())
                newMax.addValue(max.getValue());
            for (int i = 0; i < sensors; i++) {
                min.addValue(state.means[i] );
                max.addValue(state.means[i] );
            }
//            ObjectAnimator.ofFloat(StateView.this, "min", (float) newMin.getValue()).start();
//            ObjectAnimator.ofFloat(StateView.this, "max", (float) newMax.getValue()).start();
//            ObjectAnimator.ofFloat(StateView.this, "stateWith", getWidth() / (states.size() - 1)).start();
//            this.min.addValue(newMin);
//            this.max.addValue(newMax);
            setStateWith(getWidth() / (states.size() - 1f));
//            invalidate();
        }
    }

    public float getMlsX() {
        return mlsX;
    }

    public void setMlsX(float mlsX) {
        if (mlsX != this.mlsX)
            invalidate();
        this.mlsX = mlsX;
    }

    public float getMin() {
        return (float) min.getValue();
    }

    public void setMin(float min) {
        if (min != (float) this.min.getValue())
            invalidate();
        this.min.setValue(min);
    }

    public float getMax() {
        return (float) max.getValue();
    }

    public void setMax(float max) {
        if (max != (float) this.max.getValue())
            invalidate();
        this.max.setValue(max);
    }

    public float getStateWith() {
        return stateWith;
    }

    public void setStateWith(float stateWith) {
        if (stateWith != this.stateWith)
            invalidate();
        this.stateWith = stateWith;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() != MotionEvent.ACTION_DOWN)
//            return false;
//        if (event.getX() > getWidth() / 2) {
//            this.depth++;
//        } else if (depth >= 0) {
//            this.depth--;
//        }
//        if (states != null && states.size() > 0) {
////            compressedStates = new ArrayList<>(states);
////            compressedStates = StateExtractor.compressStates(compressedStates, true, depth);
//        }
//        invalidate();
//        return true;
//    }
}
