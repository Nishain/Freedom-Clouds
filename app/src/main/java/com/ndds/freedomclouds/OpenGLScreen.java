package com.ndds.freedomclouds;

import android.animation.ObjectAnimator;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.MotionEvent;

import com.ndds.freedomclouds.rendering.GiftRenderer;
import com.ndds.freedomclouds.rendering.OrnamentRenderer;

public class OpenGLScreen extends GLSurfaceView {
    OrnamentRenderer ornamentRenderer = null;
    public MainActivity activity;
    private boolean isGiftRenderer;
    private boolean needToPlayEmblemSound = true;


    public OpenGLScreen(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setEGLContextClientVersion(2);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public void setBrightnessFactor(double factor) {
        ornamentRenderer.brightnessFactor = factor;
        requestRender();
    }


    public void initOrnamentRenderer(boolean enableDynamicDrawing) {
        ornamentRenderer = new OrnamentRenderer(getContext(), this, enableDynamicDrawing);
        isGiftRenderer = false;
        setEGLConfigChooser(true);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );

        setRenderer(ornamentRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void initGiftRenderer() {
        ornamentRenderer = new GiftRenderer(getContext(), this);
        isGiftRenderer = true;
        setEGLConfigChooser(true);
        setEGLConfigChooser( 8, 8, 8, 8, 16, 0 );

        setRenderer(ornamentRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public boolean autoRotate = false;
    public boolean resetYMovement = true;
    private float previousX;
    private float previousY;
    private float downY;
    private float downX;
    private boolean isXRotating = true;
    private final Handler handler = new Handler();

    public boolean isGiftRenderer() { return isGiftRenderer; }

    public void glow() {
        ornamentRenderer.blendFactor = 0.0f;
        ornamentRenderer.doGlow = 1;
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
        this.handler.postDelayed(() -> {
            if(OpenGLScreen.this.ornamentRenderer.quickSpinAngle > 0 || OpenGLScreen.this.ornamentRenderer.doGlow != 0)
                return;
            OpenGLScreen.this.autoRotate = true;
            setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        },1000);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();

        float TOUCH_SCALE_FACTOR = 180.0f / 320;
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (ornamentRenderer.quickSpinAngle > 0) {
                    ornamentRenderer.quickSpinAngle = 0;
                    ornamentRenderer.doGlow = -1;
                    return false;
                }
                this.autoRotate = false;
                this.downY = y;
                this.downX = x;
                idleAnimator.cancel();
                idleHandler.removeCallbacksAndMessages(null);
                handler.removeCallbacksAndMessages(null);
                if(!isGiftRenderer) {
                    activity.quotesMaker.removeQuoteTimer();
                }
                setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                requestRender();
                break;
            case MotionEvent.ACTION_UP:
                activity.audio.pauseEmblemSound();
                if(!isGiftRenderer)
                    activity.quotesMaker.generateRandomQuote();
                this.idleHandler.postDelayed(() -> {
                    if(!idleAnimator.isStarted())
                        idleAnimator.start();
                },5000);

                if(Math.abs(ornamentRenderer.mAngleY) > 0) {
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
                        activity.audio.pauseEmblemSound();
                    }
                }
                else{
                    if (needToPlayEmblemSound) {
                        needToPlayEmblemSound = false;
                        activity.audio.playEmblemSound();
                    }
                }

                if(isXRotating || Math.abs((x-downX)) > 50){
                    isXRotating = true;
                    ornamentRenderer.setAngleX(
                            ornamentRenderer.getAngleX() +
                                    ((dx) * TOUCH_SCALE_FACTOR));
                }
                if (!isXRotating || Math.abs(y-downY) > 50) {
                    isXRotating = false;
                    ornamentRenderer.setAngleY(
                            ornamentRenderer.getAngleY() +
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
