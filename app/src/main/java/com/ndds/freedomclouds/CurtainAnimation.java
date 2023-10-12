package com.ndds.freedomclouds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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
        splashOverlay = activity.getLayoutInflater().inflate(R.layout.splash_screen, null);
        ViewGroup parentView = activity.getWindow().getDecorView().findViewById(R.id.root);
        parentView.addView(splashOverlay, parentView.getChildCount());
    }

    private void animateGreetingCurtain() {
        ViewGroup parentView = activity.getWindow().getDecorView().findViewById(R.id.root);
        ViewGroup newUserOverlay = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.the_gift_introduction, null);
        View darkCurtain = newUserOverlay.findViewById(R.id.dark_curtain);
        darkCurtain.setAlpha(1);
        parentView.addView(newUserOverlay, parentView.getChildCount() - 1);
        ObjectAnimator firstSplashVanish = ObjectAnimator.ofFloat(splashOverlay, "alpha", 1, 0).setDuration(800);
        firstSplashVanish.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator darkCurtainOpen = ObjectAnimator.ofFloat(darkCurtain, "alpha", 1, 0).setDuration(1500);
                darkCurtainOpen.setFloatValues(1, 0);
                parentView.removeView(splashOverlay);

                darkCurtainOpen.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        new Handler().postDelayed(() -> {
                            View whiteOutOverlay = newUserOverlay.findViewById(R.id.whiteOut);
                            ObjectAnimator whiteOut = ObjectAnimator.ofFloat(whiteOutOverlay, "alpha", 0, 1)
                                    .setDuration(1000);
                            whiteOut.addListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    ObjectAnimator secondSplashVanish = ObjectAnimator.ofFloat(newUserOverlay, "alpha", 1, 0)
                                            .setDuration(1500);
                                    secondSplashVanish.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            parentView.removeView(newUserOverlay);
                                            activity.openCurtain(true);
                                        }
                                    });
                                    secondSplashVanish.start();
                                }
                            });
                            whiteOut.start();
                        }, 4000);
                    }
                });
                darkCurtainOpen.start();
            }
        });
        firstSplashVanish.start();
    }

    public void showSplashScreen(PasscodeShield passcodeShield) {
        TextView privacyNotice = splashOverlay.findViewById(R.id.privacy_policy);
        boolean isUserSet = sharedPreferences.contains(MainActivity.START_TIME);
        if (isUserSet) {
            privacyNotice.setVisibility(View.GONE);
        }
        passcodeShield.runAfterNextUnlock(() -> {
            new Handler().postDelayed(() -> {
                if (!isUserSet) animateGreetingCurtain();
                else splashOverlay.startAnimation(openAnimation);
            }, isUserSet ? 1000 : 5000);
        });
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        ((ViewGroup) activity.getWindow().getDecorView().findViewById(R.id.root))
                .removeView(splashOverlay);
        activity.openCurtain(false);
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }
}
