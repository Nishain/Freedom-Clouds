package com.ndds.freedomclouds.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ndds.freedomclouds.R;

public class Message {
    public static void show(Context context, String text) {
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.toast, null);
        ((TextView) viewGroup.findViewById(R.id.toastText)).setText(text);
        Toast toast = new Toast(context);
        toast.setView(viewGroup);
        toast.show();
    }
}
