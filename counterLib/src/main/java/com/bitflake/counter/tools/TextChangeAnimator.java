package com.bitflake.counter.tools;

import android.view.View;
import android.widget.TextView;

public class TextChangeAnimator {
    private TextView v1;
    private TextView v2;

    public TextChangeAnimator(TextView v1, TextView v2) {
        this.v1 = v1;
        this.v2 = v2;
        v2.setTranslationX(v2.getWidth());
        v2.setVisibility(View.INVISIBLE);
    }

    public void setText(String text) {
        v2.setTranslationX(v2.getWidth());
        v2.setText(text);
        v2.setVisibility(View.VISIBLE);
        v1.animate().translationX(-v1.getWidth());
        v2.animate().translationX(0);
        TextView tmp = v1;
        v1 = v2;
        v2 = tmp;
    }

    public void setText(int textResource) {
        setText(v1.getContext().getString(textResource));
    }
}
