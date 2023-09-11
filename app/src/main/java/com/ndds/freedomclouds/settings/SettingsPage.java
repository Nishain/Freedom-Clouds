package com.ndds.freedomclouds.settings;

import android.content.SharedPreferences;
import android.view.ViewGroup;

import androidx.appcompat.widget.SwitchCompat;

import com.ndds.freedomclouds.common.ActionButton;
import com.ndds.freedomclouds.common.FunctionConsumer;
import com.ndds.freedomclouds.MainActivity;
import com.ndds.freedomclouds.common.SheetAlert;

abstract class SettingsPage {
    protected final MainActivity activity;
    protected final SharedPreferences sharedPreferences;
    public final int layoutId;
    public final String title;
    protected ViewGroup settingsView;
    protected SheetAlert dialog;


    public SettingsPage(MainActivity activity, SharedPreferences sharedPreferences, int layoutId, String title, SheetAlert dialog) {
        this.title = title;
        this.activity = activity;
        this.dialog = dialog;
        this.sharedPreferences = sharedPreferences;
        this.layoutId = layoutId;
    }

    protected void setListener(int id, Runnable r) {
        settingsView.findViewById(id).setOnClickListener(v -> {
            r.run();
        });
    }

    protected void setListener(int id, FunctionConsumer<ActionButton> consumer) {
        settingsView.findViewById(id).setOnClickListener(v -> {
            consumer.accept((ActionButton) v);
        });
    }

    protected <T> void setListener(int id, FunctionConsumer<T> consumer, T value) {
        settingsView.findViewById(id).setOnClickListener(v -> {
            consumer.accept(value);
        });
    }

    protected void setSwitchListener(int id, boolean initialValue, FunctionConsumer<Boolean> consumer) {
        SwitchCompat switchWidget = settingsView.findViewById(id);
        switchWidget.setChecked(initialValue);
        switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> consumer.accept(isChecked));
    }

    abstract void onCreate(ViewGroup settingsView);

    public void setUpContent(ViewGroup settingsView) {
        this.settingsView = settingsView;
        SettingsPage.this.onCreate(settingsView);
    }
}
