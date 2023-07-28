package com.ndds.freedomclouds;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PrivacyPolicyText extends androidx.appcompat.widget.AppCompatTextView {

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
        setMovementMethod(LinkMovementMethod.getInstance());
    }

}
