package com.ndds.freedomclouds;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

public class OpenGLScreen extends GLSurfaceView {
    CustomRenderer customRenderer = null;
    public MainActivity activity;
    private Handler quoteHandler;
    private boolean isGiftRenderer;
    private boolean needToPlayEmblemSound = true;

    public OpenGLScreen(Context context, AttributeSet attrs) {

        super(context,attrs);
        setEGLContextClientVersion(2);

    }

    public void setBrightnessFactor(float factor) {
        customRenderer.brightnessFactor = factor;
        requestRender();
    }

    @SuppressWarnings("unchecked")
    public void initRenderer(Context context, Object ... rendererParams){
        isGiftRenderer = getTag().equals("2");
        customRenderer = isGiftRenderer ? new GiftRenderer(context,this) : new CustomRenderer(context,this,(ArrayList<Bitmap>) rendererParams[0], (String[]) rendererParams[1]);
        setEGLConfigChooser(true);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );

        setRenderer(customRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    public boolean autoRotate = false;
    public boolean resetYMovement = true;
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float previousX;
    private float previousY;
    private float downY;
    private float downX;
    private boolean isXRotating = true;
    private Handler handler = new Handler();
    public void glow(){
        if(getTag().equals("2"))
            return;
        customRenderer.blendFactor = 0.0f;
        customRenderer.doGlow = 1;
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        requestRender();
    }
    Handler idleHandler;
    ObjectAnimator idleAnimator;
    public void setIdleHandler(Handler handler, ObjectAnimator objectAnimator){
        idleHandler = handler;
        idleAnimator = objectAnimator;
    }
    public void resetXAngle(){
        this.handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(OpenGLScreen.this.customRenderer.quickSpinAngle > 0 || OpenGLScreen.this.customRenderer.doGlow != 0)
                    return;
                OpenGLScreen.this.autoRotate = true;
                setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            }
        },1000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                this.autoRotate = false;
                this.downY = y;
                this.downX = x;
                idleAnimator.cancel();
                idleHandler.removeCallbacksAndMessages(null);
                handler.removeCallbacksAndMessages(null);
                if(!isGiftRenderer && activity.quoteHandler != null) {
                    activity.quoteHandler.removeCallbacksAndMessages(null);
                    activity.quoteHandler = null;
                }
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                requestRender();
                break;
            case MotionEvent.ACTION_UP:
                activity.pauseEmblemSound();
                if(!isGiftRenderer)
                    activity.generateRandomQuote();
                this.idleHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(!idleAnimator.isStarted())
                            idleAnimator.start();
                    }
                },5000);

                if(Math.abs(customRenderer.mAngleY) > 0) {
                    resetYMovement = true;
                    setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                }
                resetXAngle();
                break;
            case MotionEvent.ACTION_MOVE:

                float dx = x - previousX;
                float dy = y - previousY;

                if((int) (dx * TOUCH_SCALE_FACTOR) == 0 && (int)(dy * TOUCH_SCALE_FACTOR) == 0) {
                    if(!needToPlayEmblemSound) {
                        needToPlayEmblemSound = true;
                        activity.pauseEmblemSound();
                    }
                }
                else{
                    if (needToPlayEmblemSound) {
                        needToPlayEmblemSound = false;
                        activity.playEmblemSound();
                    }
                }

                if(isXRotating || Math.abs((x-downX)) > 50){
                    isXRotating = true;
                    customRenderer.setAngleX(
                            customRenderer.getAngleX() +
                                    ((dx) * TOUCH_SCALE_FACTOR));
                }
                if (!isXRotating || Math.abs(y-downY) > 50) {
                    isXRotating = false;
                    customRenderer.setAngleY(
                            customRenderer.getAngleY() +
                                    ((dy) * TOUCH_SCALE_FACTOR));
                }

                requestRender();
                break;
        }

        previousX = x;
        previousY = y;
        return true;

    }


}
