package com.ndds.freedomclouds.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.ndds.freedomclouds.R;

public class ActionButton extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener {

    Paint backgroundPaint = new Paint();
    Paint outlinePaint = new Paint();
    int strokeWidth;
    int backgroundColor;

    private void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        setBackgroundColor(Color.TRANSPARENT);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setColor(Color.BLACK);
        outlinePaint.setStrokeWidth(strokeWidth);
        setOnTouchListener(this);
    }

    public ActionButton(Context context) {
        super(context);
        backgroundColor = getResources().getColor(R.color.beautyGreen);
        init(context);
    }

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ActionButton, 0, 0);
        backgroundColor = a.getColor(R.styleable.ActionButton_colorTheme, 0);
        a.recycle();
        init(context);
    }

    public ActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.ActionButton, 0, 0);
        backgroundColor = a.getColor(R.styleable.ActionButton_colorTheme, 0);
        a.recycle();
        init(context);
    }

    public void setColorTheme(int colorResourceId) {
        backgroundColor = getResources().getColor(colorResourceId);
    }

    @Override
    public void setEnabled(boolean enabled) {
        setAlpha(enabled ? 1 : 0.5f);
        super.setEnabled(enabled);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = getHeight() / 2f;
        RectF roundedRect = new RectF(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);
        canvas.drawRoundRect(roundedRect, radius, radius, backgroundPaint);
        canvas.drawRoundRect(roundedRect, radius, radius, outlinePaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            setAlpha(0.5f);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            setAlpha(1);
        }
        return false;
    }
}
