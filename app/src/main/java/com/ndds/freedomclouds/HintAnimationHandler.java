package com.ndds.freedomclouds;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

class HintAnimationHandler extends AnimatorListenerAdapter {
    ObjectAnimator moveAnimator;
    View view;

    HintAnimationHandler(View v) {
        view = v;
        view.setAlpha(0);
    }
    @Override
    public void onAnimationCancel(Animator animation) {
        super.onAnimationCancel(animation);
        if (moveAnimator != null)
            moveAnimator.cancel();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        moveAnimator = ObjectAnimator.ofFloat(view, "translationX", -20   , 20);
        moveAnimator.setRepeatCount(4);
        moveAnimator.setDuration(500);
        moveAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        moveAnimator.setRepeatMode(ValueAnimator.REVERSE);
        moveAnimator.start();
        moveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ObjectAnimator.ofFloat(view, "alpha", 1, 0).start();
            }
        });
    }
}
