package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class MainActivity extends AppCompatActivity {

    private OpenGLScreen openGLScreen;
    private Handler idleHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
//        OpenGLScreen openGLScreen = new OpenGLScreen(this);
        setContentView(R.layout.activity_main);
        openGLScreen = findViewById(R.id.gl_screen);
        openGLScreen.setZOrderOnTop(true);
        View v = findViewById(R.id.rotate_hint);
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(v, "alpha", 0, 1);
        objectAnimator.setRepeatMode(ValueAnimator.REVERSE);
        objectAnimator.setRepeatCount(7);
        objectAnimator.setDuration(500);
        idleHandler = new Handler();
        openGLScreen.setIdleHandler(idleHandler,objectAnimator);
        openGLScreen.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        objectAnimator.start();
    }
    public void glow(View v){
        openGLScreen.glow();
    }
}
