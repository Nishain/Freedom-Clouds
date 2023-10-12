package com.ndds.freedomclouds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.rendering.GiftRenderer;
import com.ndds.freedomclouds.rendering.OrnamentRenderer;

@SuppressLint("ViewConstructor")
public class OpenGLScreen extends GLSurfaceView {
    OrnamentRenderer ornamentRenderer = null;
    public MainActivity activity;
    public String backTitle;
    private boolean isGiftRenderer;
    private boolean needToPlayEmblemSound = true;

    public void setBackTitle(String title) {
        backTitle = title;
        queueEvent(() -> {
            ornamentRenderer.updateBackPlate();
        });
    }

    public void playShiftAnimation(boolean moveAway, View target) {
        final float offset = 0.8f;
        ObjectAnimator shiftAnimator = ObjectAnimator.ofFloat(ornamentRenderer, "horizontalShift", moveAway? 0 : offset, moveAway ? offset : 0)
                .setDuration(1000);
        ObjectAnimator alphaAnimator = ObjectAnimator.
                ofFloat(target, "alpha", moveAway ? 0: 1, moveAway ? 1 : 0)
                .setDuration(1000);

        shiftAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setRenderMode(RENDERMODE_WHEN_DIRTY);
                if (moveAway) alphaAnimator.start();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                setRenderMode(RENDERMODE_CONTINUOUSLY);
            }
        });

        if (moveAway) {
            target.setAlpha(0);
            target.setVisibility(VISIBLE);
            shiftAnimator.start();
        } else {
            alphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    target.setVisibility(GONE);
                    shiftAnimator.start();
                }
            });
            alphaAnimator.start();
        }
    }

    public OpenGLScreen(MainActivity activity, String title) {
        super(activity);
        this.activity = activity;
        backTitle = title;
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
