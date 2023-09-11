package com.ndds.freedomclouds;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PrivacyPolicyText extends androidx.appcompat.widget.AppCompatTextView {
    private static class ClickMovementMethod implements MovementMethod {
        private final int normalColor, selectedColor;
        private float[] pressedCoordinate = null;

        private ClickMovementMethod(int normalColor, int selectedColor) {
            this.normalColor = normalColor;
            this.selectedColor = selectedColor;
        }

        @Override
        public void initialize(TextView widget, Spannable text) {

        }

        @Override
        public boolean onKeyDown(TextView widget, Spannable text, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onKeyUp(TextView widget, Spannable text, int keyCode, KeyEvent event) {
            return false;
        }

        @Override
        public boolean onKeyOther(TextView view, Spannable text, KeyEvent event) {
            return false;
        }

        @Override
        public void onTakeFocus(TextView widget, Spannable text, int direction) {

        }

        @Override
        public boolean onTrackballEvent(TextView widget, Spannable text, MotionEvent event) {
            return false;
        }

        @Override
        public boolean onTouchEvent(TextView widget, Spannable text, MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
                int x = (int) event.getX();
                int y = (int) event.getY();


                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();

                Layout layout = widget.getLayout();

                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);

                ClickableSpan[] links = text.getSpans(off, off, ClickableSpan.class);
                if (action == MotionEvent.ACTION_UP) {
                    if (pressedCoordinate != null) {
                        if (Math.abs(pressedCoordinate[0] - event.getX()) < 10 &&
                                Math.abs(pressedCoordinate[1] - event.getY()) < 10) {
                            widget.setLinkTextColor(normalColor);
                            links[0].onClick(widget);
                            pressedCoordinate = null;
                        } else {
                            widget.setLinkTextColor(normalColor);
                            pressedCoordinate = null;
                        }
                    }
                } else if (links.length > 0) {
                    widget.setLinkTextColor(selectedColor);
                    pressedCoordinate = new float[]{event.getX(), event.getY()};
                }
            }
            return false;
        }

        @Override
        public boolean onGenericMotionEvent(TextView widget, Spannable text, MotionEvent event) {
            return false;
        }

        @Override
        public boolean canSelectArbitrarily() {
            return false;
        }
    }

    private static final String SPANNED_TEXT = "privacy policy";

    public PrivacyPolicyText(@NonNull Context context) {
        super(context);
        init(context);
    }

    public PrivacyPolicyText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PrivacyPolicyText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        SpannableString spannableString = new SpannableString("By using this app you agree to our " + SPANNED_TEXT);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.termsfeed.com/live/72acdc7a-51e1-4f89-aa04-552ec845f5ac"));
                context.startActivity(browserIntent);
            }
        };
        spannableString.setSpan(clickableSpan, spannableString.length() - SPANNED_TEXT.length(), spannableString.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        setText(spannableString);
        Resources resources = getContext().getResources();
        setMovementMethod(new ClickMovementMethod(
                getLinkTextColors().getDefaultColor()
                , resources.getColor(R.color.beautyGreen)));
    }

}
