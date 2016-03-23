package com.bitflake.allcount;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.bitflake.counter.CountState;

import java.util.List;

public class CounterVisView extends View {
    private List<CountState> states;
    private Paint centerPaint;
    private Paint sidePaint;
    private int accent;
    private double[] mins;
    private double[] maxs;
    private int sensors;
    private Path[] paths;
    private Path path;
    private int maxR;
    private PointF point = new PointF();

    public CounterVisView(Context context) {
        super(context);
        init(context);
    }

    public CounterVisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CounterVisView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CounterVisView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        accent = getContext().getResources().getColor(R.color.colorAccent);
        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(getContext().getResources().getColor(R.color.colorPrimary));
        sidePaint = new Paint();
        sidePaint.setAntiAlias(true);
        sidePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        sidePaint.setColor(0xFFFFFFFF);
//        sidePaint.setColor(accent);
        float density = context.getResources().getDisplayMetrics().density;
        sidePaint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        sidePaint.setStrokeWidth(density * 4);
        path = new Path();


        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setStates(List<CountState> states) {
        this.states = states;
        this.sensors = 3;
        this.mins = new double[sensors];
        this.maxs = new double[sensors];
        for (int s = 0; s < states.size(); s++) {
            CountState state = states.get(s);
            for (int i = 0; i < sensors; i++) {
                if (s == 0) {
                    mins[i] = state.means[i] - state.sd[i];
                    maxs[i] = state.means[i] + state.sd[i];
                } else {
                    mins[i] = Math.min(mins[i], state.means[i] - state.sd[i]);
                    maxs[i] = Math.max(maxs[i], state.means[i] + state.sd[i]);
                }
            }
        }
        paths = new Path[sensors];
        path = new Path();
        for (int i = 0; i < sensors; i++) {
            paths[i] = new Path();
        }
        sidePaint.setAlpha(0xFF / sensors);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (maxR != Math.min(w, h) / 2) {
            maxR = Math.min(w, h) / 2;
            updateOutline();
        }
//        if (sidePaint.getPathEffect() == null) {
//            sidePaint.setPathEffect(new CornerPathEffect(maxR / 4));   // set the path effect when they joi
//        }

        canvas.drawCircle(w / 2, h / 2, maxR, centerPaint);
        int[] colors = new int[]{0xFFFFFF00, 0xFF00FFFF, 0xFFFF00FF};
        if (states != null) {
            for (int s = 0; s < sensors; s++) {
//                path.moveTo(w / 2, h / 2 - maxR);
                path.reset();
                for (double ratio = 0; ratio <= 1; ratio += 1.0 / states.size()) {
                    PointF p = getPoint(s, ratio, true);
                    if (path.isEmpty())
                        path.moveTo(p.x, p.y);
                    else
                        path.lineTo(p.x, p.y);
                }
                for (double ratio = 0; ratio <= 1; ratio += 1.0 / states.size()) {
                    PointF p = getPoint(s, 1 - ratio, false);
                    path.lineTo(p.x, p.y);
                }
                path.close();
//                sidePaint.setColor(colors[s]);
                canvas.drawPath(path, sidePaint);
            }
        }
    }

    private void updateOutline() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            ViewOutlineProvider viewOutlineProvider = new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    // Or read size directly from the view's width/height
                    outline.setOval(0, 0, maxR * 2, maxR * 2);
                }
            };
            setOutlineProvider(viewOutlineProvider);
        }
    }

    public PointF getPoint(int sensor, double angleRatio, boolean min) {
        int i1 = (int) Math.floor(angleRatio * states.size()) % states.size();
        int i2 = (int) Math.ceil(angleRatio * states.size()) % states.size();
        CountState s1 = states.get(i1);
        CountState s2 = states.get(i2);

        double v1 = s1.means[sensor];
        double v2 = s2.means[sensor];
        if (min) {
            v1 -= s1.sd[sensor] / 2;
            v2 -= s2.sd[sensor] / 2;
        } else {
            v1 += s1.sd[sensor] / 2;
            v2 += s2.sd[sensor] / 2;
        }

        double ratio = angleRatio * states.size() - i1;
        double value = v1 + (v2 - v1) * ratio;

        double r = (value - mins[sensor]) / (maxs[sensor] - mins[sensor]) * maxR / 2 + maxR / 4;
        if ((maxs[sensor] == mins[sensor])) {
            r = maxR / 2;
        }

        double angle = angleRatio * 2 * Math.PI + Math.PI;
        point.x = (float) (getWidth() / 2 + Math.sin(angle) * r);
        point.y = (float) (getHeight() / 2 + Math.cos(angle) * r);
        return point;
    }
}
