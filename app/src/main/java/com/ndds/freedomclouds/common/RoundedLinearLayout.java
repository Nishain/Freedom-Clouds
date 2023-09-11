package com.ndds.freedomclouds.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.ndds.freedomclouds.R;

public class RoundedLinearLayout extends LinearLayout {

    final Paint paint = new Paint();
    int topRadius = 0, bottomRadius = 0;

    private void init () {
        setWillNotDraw(false);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
    }
    public RoundedLinearLayout(Context context) {
        super(context);
        init();
    }

    public RoundedLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.RoundedLinearLayout, 0, 0);
        int outlineRadius = a.getDimensionPixelSize(R.styleable.RoundedLinearLayout_outlineRadius, 0);
        if (outlineRadius > 0) {
            topRadius = outlineRadius;
            bottomRadius = outlineRadius;
        } else {
            int topRadius = a.getDimensionPixelSize(R.styleable.RoundedLinearLayout_topOutlineRadius, 0);
            int bottomRadius = a.getDimensionPixelSize(R.styleable.RoundedLinearLayout_bottomOutlineRadius, 0);
            if (topRadius > 0)
                this.topRadius = topRadius;
            if (bottomRadius > 0)
                this.bottomRadius = bottomRadius;
        }
        init();
        paint.setColor(
                a.getColor(R.styleable.RoundedLinearLayout_bgColor, Color.WHITE)
        );
    }

    public RoundedLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path path = new Path();
        int width = getWidth();
        int height = getHeight();
        if (topRadius > 0) {
            path.moveTo(0, topRadius);
            path.quadTo(0,0, topRadius, 0);
            path.lineTo(width - topRadius, 0);
            path.quadTo(width, 0, width, topRadius);
        } else {
            path.moveTo(0, 0);
            path.lineTo(width, 0);
        }

        if (bottomRadius > 0) {
            path.lineTo(width, height - bottomRadius);
            path.quadTo(width, height, width - bottomRadius, height);
            path.lineTo(bottomRadius, height);
            path.quadTo(0, height, 0, height - bottomRadius);
        } else {
            path.lineTo(width, height);
            path.lineTo(0, height);
        }
        path.close();
        canvas.drawPath(path, paint);
    }
}
