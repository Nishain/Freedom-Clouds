package com.ndds.freedomclouds;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class CurtainAnimation implements Animation.AnimationListener {
    private final View splashOverlay;
    private final SharedPreferences sharedPreferences;
    private final MainActivity activity;
    private final Animation openAnimation;

    CurtainAnimation(MainActivity activity, SharedPreferences sharedPreferences) {
        openAnimation = AnimationUtils.loadAnimation(activity, R.anim.push_up_anim);
        openAnimation.setAnimationListener(this);
        this.activity = activity;

        this.sharedPreferences = sharedPreferences;
        splashOverlay = activity.findViewById(R.id.splash_overlay);
        splashOverlay.setVisibility(View.VISIBLE);
    }

    public void startAnimate() {
        splashOverlay.setVisibility(View.VISIBLE);
        splashOverlay.startAnimation(openAnimation);
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        splashOverlay.setVisibility(View.GONE);

        activity.openCurtain(!sharedPreferences.contains(MainActivity.START_TIME));
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
