package com.ndds.freedomclouds;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.NoCopySpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

public class PrivacyPolicyText extends SpannableString {

    private static final String SPANNED_TEXT = "privacy policy";
    public PrivacyPolicyText(Context context) {
        super("By using this app you agree to our " + SPANNED_TEXT);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.termsfeed.com/live/72acdc7a-51e1-4f89-aa04-552ec845f5ac"));
                Log.d("debugin....", "browse intent");
                context.startActivity(browserIntent);
            }
        };
        setSpan(clickableSpan, length() - SPANNED_TEXT.length(), length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    }
}
