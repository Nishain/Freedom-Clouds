package com.ndds.freedomclouds.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.ndds.freedomclouds.R;

public class InsetSafeLinearLayout extends LinearLayout {
    private final int defaultPadding;
    private enum InsetOrientation {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT
    }

    private final InsetOrientation orientation;

    private int calculatePadding(int systemInset) {
        return systemInset + Math.min(defaultPadding, Math.abs(systemInset - defaultPadding));
    }

    public InsetSafeLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = getContext()
                .obtainStyledAttributes(attrs, R.styleable.InsetSafeLinearLayout);

        int enumIndex = typedArray.getInt(R.styleable.InsetSafeLinearLayout_placement, 0);
        orientation = InsetOrientation.values()[enumIndex];

        if (orientation == InsetOrientation.BOTTOM)
            defaultPadding = getPaddingBottom();
        else if (orientation == InsetOrientation.TOP)
            defaultPadding = getPaddingTop();
        else if (orientation == InsetOrientation.LEFT)
            defaultPadding = getPaddingLeft();
        else defaultPadding = getPaddingRight();
        typedArray.recycle();
    }

    @Override
    protected boolean fitSystemWindows(Rect insets) {
        if (orientation == InsetOrientation.BOTTOM) {
            setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), calculatePadding(insets.bottom));
        } else if (orientation == InsetOrientation.TOP) {
            setPadding(getPaddingLeft(), calculatePadding(insets.top), getPaddingRight(), getPaddingBottom());
        } else if (orientation == InsetOrientation.LEFT) {
            setPadding(calculatePadding(insets.left), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        } else
            setPadding(getPaddingLeft(), getPaddingTop(), calculatePadding(insets.right), getPaddingBottom());
        return false;
    }
}