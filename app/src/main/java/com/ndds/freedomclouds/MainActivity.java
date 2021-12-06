package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private OpenGLScreen openGLScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
//        OpenGLScreen openGLScreen = new OpenGLScreen(this);
        setContentView(R.layout.activity_main);
        openGLScreen = findViewById(R.id.gl_screen);
        openGLScreen.setZOrderOnTop( true );

        openGLScreen.getHolder().setFormat( PixelFormat.TRANSLUCENT);
    }
    public void glow(View v){
        openGLScreen.glow();
    }
}
