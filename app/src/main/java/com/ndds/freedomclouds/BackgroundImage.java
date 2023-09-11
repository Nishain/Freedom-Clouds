package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class BackgroundImage extends androidx.appcompat.widget.AppCompatImageView {

    public void init(Context context) {
        Bitmap backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.wood);

        Bitmap darkerBitmap = backgroundBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas drawCanvas = new Canvas(darkerBitmap);
        drawCanvas.drawColor(0xff0000ff, PorterDuff.Mode.MULTIPLY);
        setImageBitmap(darkerBitmap);
    }

    public BackgroundImage(Context context) {
        super(context);
    }

    public BackgroundImage(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BackgroundImage(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void changeBrightness(double brightnessFactor) {
        setAlpha((float) (1 - brightnessFactor));
    }
}
