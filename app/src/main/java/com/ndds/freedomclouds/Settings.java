package com.ndds.freedomclouds;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class Settings implements DatePickerDialog.OnDateSetListener {
    MainActivity activity;
    AlertDialog dialog;
    private ViewGroup settingsView;
    final private SharedPreferences sharedPreferences;

    Settings(MainActivity activity, SharedPreferences sharedPreferences) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
    }

    private void changeDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void resetDate() {
        Calendar now = Calendar.getInstance();
        activity.updateDate(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));
    }

    private void restartApp() {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        activity.finish();
        activity.startActivity(mainIntent);
    }

    private void resetApp(Button button) {
        if(button.getText().toString().contains("Are you sure ?")){
            dialog.dismiss();
            sharedPreferences.edit().remove(MainActivity.START_TIME).apply();
            Toast.makeText(activity, "restarting the app", Toast.LENGTH_SHORT).show();
            restartApp();
            return;
        }
        button.setBackgroundResource(R.drawable.round_beauty_orange);
        button.setText(R.string.reset_confirmation);
    }


    private void setListener(int id, Runnable r) {
        settingsView.findViewById(id).setOnClickListener(v -> {
            if (id != R.id.reset_memory)
                dialog.dismiss();
            r.run();
        });
    }

    private void setListener(int id, FunctionConsumer<Button> consumer) {
        settingsView.findViewById(id).setOnClickListener(v -> {
            if (id != R.id.reset_memory)
                dialog.dismiss();
            consumer.accept((Button) v);
        });
    }

    private void setSwitchListener(int id, boolean initialValue, FunctionConsumer<Boolean> consumer) {
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch switchWidget = settingsView.findViewById(id);
        switchWidget.setChecked(initialValue);
        switchWidget.setOnCheckedChangeListener((buttonView, isChecked) -> consumer.accept(isChecked));
    }

    private void onAmbientLightResponsivenessChanged(Boolean isResponsive) {
        activity.onAmbientLightResponsivenessChanged(isResponsive);
    }


    private void openDeveloperNote() {
        ViewGroup viewGroup = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.developer_note,null);
        AssetManager assetManager = activity.getAssets();
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if(view instanceof TextView && !(view instanceof Button))
                ((TextView) view).setTypeface(Typeface.createFromAsset(assetManager,"fonts/CherrySwash-Regular.ttf"));
        }
        AlertDialog noteAlert = new AlertDialog.Builder(activity).setView(viewGroup).create();
        viewGroup.findViewById(R.id.developer_note_ok).setOnClickListener(v1 -> noteAlert.dismiss());
        noteAlert.show();
        activity.audio.playSound(R.raw.paper_flip);
    }

    public void show() {
        settingsView = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.settings,null);
        dialog = new AlertDialog.Builder(activity, R.style.SettingsAlert).setView(settingsView).create();
        setListener(R.id.settings_change_date, this::changeDate);
        setListener(R.id.settings_reset_today, this::resetDate);
        setListener(R.id.settings_developer_notice, this::openDeveloperNote);
        setListener(R.id.reset_memory, this::resetApp);
        setListener(R.id.restart_app, this::restartApp);
        setSwitchListener(R.id.settings_ambient_light, activity.isAmbientLightResponsive(), this::onAmbientLightResponsivenessChanged);
        settingsView.findViewById(R.id.settings_dismiss).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        activity.audio.playSound(R.raw.paper_flip);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        activity.updateDate(year, month, dayOfMonth);
    }
}
