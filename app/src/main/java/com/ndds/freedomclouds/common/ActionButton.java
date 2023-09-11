package com.ndds.freedomclouds.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.ndds.freedomclouds.R;

public class ActionButton extends androidx.appcompat.widget.AppCompatButton implements View.OnTouchListener {

    Paint paint = new Paint();
    int radius;
    int strokeWidth;
    int backgroundColor;

    private void init(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        setBackgroundColor(Color.TRANSPARENT);
        strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
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
    protected void onDraw(Canvas canvas) {
        paint.setColor(backgroundColor);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        RectF roundedRect = new RectF(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);
        canvas.drawRoundRect(roundedRect, radius, radius, paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(roundedRect, radius, radius, paint);
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
