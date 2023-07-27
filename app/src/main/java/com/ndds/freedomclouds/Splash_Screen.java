package com.ndds.freedomclouds;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

public class Splash_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        TextView privacyNotice = findViewById(R.id.privacy_policy);

        SharedPreferences sharedPreferences = getSharedPreferences("configuration", MODE_PRIVATE);
        boolean isUserSet = sharedPreferences.contains(MainActivity.START_TIME);
        if (!isUserSet) {
            privacyNotice.setVisibility(View.VISIBLE);
            privacyNotice.setText(new PrivacyPolicyText(this));
            privacyNotice.setMovementMethod(LinkMovementMethod.getInstance());
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        startActivity(new Intent(Splash_Screen.this,MainActivity.class));

                    }
                });


            }
        }, isUserSet ? 1000 : 5000);
    }

    @Override
    public void finish() {

        super.finish();

    }
}
