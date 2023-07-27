package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class BackgroundImage extends androidx.appcompat.widget.AppCompatImageView {
    Bitmap backgroundBitmap;

    private void init(Context context) {
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wood);
    }

    public BackgroundImage(Context context) {
        super(context);
        init(context);
    }

    public BackgroundImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BackgroundImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void switchLight(boolean isBright) {
        if (isBright) {
            setImageBitmap(backgroundBitmap);
        } else {
            Bitmap darkerBitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas drawCanvas = new Canvas(darkerBitmap);
            drawCanvas.drawColor(0xff1010ee, PorterDuff.Mode.MULTIPLY);
            setImageBitmap(darkerBitmap);
        }
    }
}
