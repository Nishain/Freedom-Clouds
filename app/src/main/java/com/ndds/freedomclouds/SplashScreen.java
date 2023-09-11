package com.ndds.freedomclouds;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        TextView privacyNotice = findViewById(R.id.privacy_policy);

        SharedPreferences sharedPreferences = getSharedPreferences("configuration", MODE_PRIVATE);
        boolean isUserSet = sharedPreferences.contains(MainActivity.START_TIME);
        if (!isUserSet) {
            privacyNotice.setVisibility(View.VISIBLE);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(SplashScreen.this,MainActivity.class));
            }
        }, isUserSet ? 1000 : 5000);
    }
}
