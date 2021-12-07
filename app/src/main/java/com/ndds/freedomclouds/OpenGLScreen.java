package com.ndds.freedomclouds;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class OpenGLScreen extends GLSurfaceView {
    CustomRenderer customRenderer = null;
    public OpenGLScreen(Context context, AttributeSet attrs) {

        super(context,attrs);

        setEGLContextClientVersion(2);

        customRenderer = new CustomRenderer(context,this);
        setEGLConfigChooser(true);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );
        setRenderer(customRenderer);

        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    public boolean autoRotate = false;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;
    public boolean quickSpinEnabled = false;
    private Handler handler = new Handler();
    public void glow(){
        customRenderer.blendFactor = 0.0f;
        customRenderer.doGlow = 1;
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
    }
    public void quickSpin(){
        quickSpinEnabled = true;
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.autoRotate = false;
                handler.removeCallbacksAndMessages(null);
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                requestRender();
                break;
            case MotionEvent.ACTION_UP:
                this.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(OpenGLScreen.this.customRenderer.quickSpinAngle > 0)
                            return;
                        OpenGLScreen.this.autoRotate = true;
                        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                        requestRender();
                    }
                },1000);
                break;
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2) {
                    dx = dx * -1 ;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2) {
                    dy = dy * -1 ;
                }
                customRenderer.setAngle(
                        customRenderer.getAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        previousX = x;
        previousY = y;
        return true;

    }
}
