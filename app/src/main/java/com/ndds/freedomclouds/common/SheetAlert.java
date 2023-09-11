package com.ndds.freedomclouds.common;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.ndds.freedomclouds.R;

public class SheetAlert {
    public enum Position {
        TOP,
        BOTTOM
    }

    private final View rootView;
    private final AlertDialog dialog;
    private final Position position;

    public SheetAlert(View contentView, Context context, Position position) {
        this.position = position;
        this.rootView = contentView;
        dialog = new AlertDialog.Builder(context, position == Position.TOP ? R.style.AlertTop : R.style.AlertBottom).
                setView(rootView)
                .create();
        rootView.setVisibility(View.INVISIBLE);
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            float[] direction = new float[2];
            if (position == Position.TOP)
                direction[0] = -rootView.getHeight();
            else
                direction[0] = rootView.getHeight();
            ObjectAnimator animator = ObjectAnimator.ofFloat(rootView, "translationY", direction);
            animator.setDuration(500);
            animator.start();
            rootView.setVisibility(View.VISIBLE);
        });

        dialog.setCanceledOnTouchOutside(false); // remove default behaviour
    }

    public void show(boolean canCancel) {
        dialog.setCancelable(canCancel);
        if (canCancel) {
            dialog.getWindow().getDecorView().setOnTouchListener(new View.OnTouchListener() {
                private boolean isPressedDownOutside = false;
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN && event.getY() > rootView.getHeight()) {
                        isPressedDownOutside = true;
                    } else if (isPressedDownOutside && event.getAction() == MotionEvent.ACTION_UP && event.getY() > rootView.getHeight()) {
                        isPressedDownOutside = false;
                        dismiss();
                    } else v.performClick();
                    return false;
                }
            });
        }
        dialog.show();
    }

    public void dismiss() {
        float[] direction = new float[2];
        direction[0] = rootView.getTranslationY();
        if (position == Position.TOP) {
            direction[1] = -rootView.getHeight();
        } else {
            direction[1] = rootView.getHeight();
        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(rootView, "translationY", direction);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                dialog.dismiss();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });
        animator.start();
    }
}
