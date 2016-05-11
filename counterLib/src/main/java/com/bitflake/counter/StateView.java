package com.bitflake.counter;

import android.animation.ObjectAnimator;
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
import android.view.MotionEvent;
import android.view.View;

import com.bitflake.counter.services.CountConstants;
import com.bitflake.counter.services.RecordConstants;
import com.bitflake.counter.tools.ValueHelper;

import java.util.ArrayList;
import java.util.List;

public class StateView extends View {
    private List<CountState> states;
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
            0xFFFFFFFF, 0xFF888888, 0xFFF50057
    };
    private boolean isRegistered;
    private List<CountState> compressedStates;
    private int depth;

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
                min.addValue(state.means[i] - state.sd[i]);
                max.addValue(state.means[i] + state.sd[i]);
            }
        }
        stateWith = 0;

        compressedStates = new ArrayList<>(states);
        compressedStates = StateExtractor.compressStates(compressedStates);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.density = getResources().getDisplayMetrics().density;
        path = new Path();
        paintStates = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStates.setStrokeWidth(2 * density);
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
        if (stateWith == 0 && states != null) {
            stateWith = getWidth() / (states.size() - 1);
        }
        drawStates(canvas);
    }

    private void drawStates(Canvas canvas) {

        if (states != null) {
            for (int s = 0; s < sensors; s++) {
                path.reset();
                paintStates.setColor(colors[s]);
                paintStates.setAlpha(0xFF);
                paintStates.setStyle(Paint.Style.FILL_AND_STROKE);

                for (int state = 0; state < states.size() - 1; state++) {
                    float y1 = getY(s, state, true);
                    float x1 = getX(state);
                    float y2 = getY(s, state + 1, true);
                    float x2 = getX(state + 1);

                    float xC = (x1 + x2) / 2;
                    if (path.isEmpty())
                        path.moveTo(x1, y1);
//                    path.cubicTo(xC, y1, xC, y2, x2, y2);
                    path.lineTo(x2, y2);
                    if (compressedStates != null && compressedStates.contains(states.get(state)))
                        canvas.drawCircle(x1, (y1 + getY(s, state, true)) / 2, density * 4, paintStates);
                }
                for (int state = states.size() - 1; state > 0; state--) {
                    float y1 = getY(s, state, false);
                    float x1 = getX(state);
                    float y2 = getY(s, state - 1, false);
                    float x2 = getX(state - 1);
                    float xC = (x1 + x2) / 2;

                    if (state == states.size() - 1)
                        path.lineTo(x1, y1);

//                    path.cubicTo(xC, y1, xC, y2, x2, y2);
                    path.lineTo(x2, y2);

                }
//                path.close();
                paintStates.setColor(colors[s]);
                paintStates.setAlpha(0x88);
                canvas.drawPath(path, paintStates);
//                paintStates.setColor(colors[s]);
//                paintStates.setStyle(Paint.Style.STROKE);
//                paintStates.setStrokeWidth(density);
//                canvas.drawPath(path, paintStates);
            }
        }
        if (lastState != null && !isRecording) {
//            float x = mostLikelyState * getWidth() / states.size();
            showValues(canvas, lastState, mlsX);
        } else if (isRecording && states.size() > 0) {
            showValues(canvas, states.get(0), getWidth());
        }
    }

    //    private void showValues(Canvas canvas) {
//        for (int s = 0; s < sensors; s++) {
//            float yMax = getY(s, lastState, true);
//            float yMin = getY(s, lastState, false);
//            paintStates.setColor(colors[s]);
//            paintStates.setStyle(Paint.Style.FILL);
//            canvas.drawCircle(mlsX, (yMin + yMax) / 2, density * 8, paintStates);
//            paintStates.setStyle(Paint.Style.STROKE);
//            paintStates.setStrokeWidth(2 * density);
//            canvas.drawLine(mlsX, yMin, mlsX, yMax, paintStates);
//        }
//    }
    private void showValues(Canvas canvas, CountState state, float x) {
        for (int s = 0; s < sensors; s++) {
            float yMax = getY(s, state, true);
            float yMin = getY(s, state, false);
            paintStates.setColor(colors[s]);
            paintStates.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, (yMin + yMax) / 2, density * 8, paintStates);
            paintStates.setStyle(Paint.Style.STROKE);
            paintStates.setStrokeWidth(2 * density);
//            canvas.drawLine(x, yMin, x, yMax, paintStates);
        }
    }

    public float getY(int sensor, int state, boolean min) {
        return getY(sensor, states.get(state), min);
    }

    public float getY(int sensor, CountState state, boolean min) {
        double value = state.means[sensor] + (min ? -1 : 1) * state.sd[sensor] / 2;
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
                lastState = CountState.stateFromJSON(data.getString(CountConstants.DATA_LAST_STATE));
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
        states.add(state);
        if (states.size() > 1) {
            newMin.addValue(min);
            newMax.addValue(max);
            for (int i = 0; i < sensors; i++) {
                newMin.addValue(state.means[i] - state.sd[i]);
                newMax.addValue(state.means[i] + state.sd[i]);
            }
            ObjectAnimator.ofFloat(StateView.this, "min", newMin.getFloat()).start();
            ObjectAnimator.ofFloat(StateView.this, "max", newMax.getFloat()).start();
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
        return min.getFloat();
    }

    public void setMin(float min) {
        if (min != this.min.getFloat())
            invalidate();
        this.min.setValue(min);
    }

    public float getMax() {
        return max.getFloat();
    }

    public void setMax(float max) {
        if (max != this.max.getFloat())
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return false;
        if (event.getX() > getWidth() / 2) {
            this.depth++;
        } else if (depth >= 0) {
            this.depth--;
        }
        if(states.size()>0) {
            compressedStates = new ArrayList<>(states);
            compressedStates = StateExtractor.compressStates(compressedStates);
        }
        invalidate();
        return true;
    }
}
