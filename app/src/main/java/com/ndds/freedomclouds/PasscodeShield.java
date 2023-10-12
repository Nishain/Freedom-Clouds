package com.ndds.freedomclouds;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;

import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.common.SheetAlert;

public class PasscodeShield implements View.OnClickListener, View.OnTouchListener {
    private boolean isDisplaying = false;
    private String passcode;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action ==  MotionEvent.ACTION_DOWN) {
            v.setBackgroundResource(R.drawable.keypad_button);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundColor(Color.TRANSPARENT);
        }
        return false;
    }

    public interface TaskAfterUnlock {
        void afterUnlocked();
    }

    private final Activity activity;
    public final static String PASSCODE = "passcode";
    private EditText display;
    private final SharedPreferences sharedPreferences;
    private SheetAlert alertShield;
    private TaskAfterUnlock postTask = null;

    public PasscodeShield(Activity activity, SharedPreferences sharedPreferences) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
    }

    public void runAfterNextUnlock(TaskAfterUnlock task) {
        if (sharedPreferences.contains(PASSCODE))
            postTask = task;
        else task.afterUnlocked();
    }

    public void runTaskAfterUnlock(TaskAfterUnlock task) {
        if (isDisplaying) postTask = task;
        else task.afterUnlocked();
    }

    public void show() {
        passcode = sharedPreferences.getString(PASSCODE, null);
        if (passcode == null || isDisplaying) return;
        ViewGroup layout = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.passcode_keypad, null);
        display = layout.findViewById(R.id.passcodeKeypadDisplay);
        TableLayout tableLayout = layout.findViewById(R.id.passcodeKeypadGrid);
        int noOfRows = tableLayout.getChildCount();
        for (int i = 0; i < noOfRows; i++) {
            ViewGroup row = ((ViewGroup) tableLayout.getChildAt(i));
            int cellCount = row.getChildCount();
            for (int j = 0; j < cellCount; j++) {
                row.getChildAt(j).setOnClickListener(this);
                row.getChildAt(j).setOnTouchListener(this);
            }
        }

        alertShield = new SheetAlert(layout, activity, SheetAlert.Position.BOTTOM);
        alertShield.show(false);
        isDisplaying = true;
    }

    @Override
    public void onClick(View v) {
        String buttonNumber = ((Button) v).getText().toString();
        String displayText = display.getText().toString();
        if (buttonNumber.equals("DEL")) {
            if (!displayText.isEmpty()) {
                display.setText(displayText.substring(0, displayText.length() - 1));
            }
        } else if(displayText.length() < 4) {
            String newCode = displayText + buttonNumber;
            display.setText(newCode);
            if (newCode.length() == 4) {
                if (passcode.equals(newCode)) {
                    alertShield.dismiss();
                    isDisplaying = false;
                    if(postTask != null) {
                        postTask.afterUnlocked();
                        postTask = null;
                    }
                } else {
                    Message.show(activity, "Incorrect passcode");
                    display.setText("");
                    ObjectAnimator animator = ObjectAnimator.ofFloat(display, "translationX", -100);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(125);
                    animator.setRepeatCount(5);
                    animator.setRepeatMode(ValueAnimator.REVERSE);
                    animator.start();
                }
            }
        }
    }
}
