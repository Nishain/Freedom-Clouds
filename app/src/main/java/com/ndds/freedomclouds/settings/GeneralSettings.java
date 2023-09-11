package com.ndds.freedomclouds.settings;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.ndds.freedomclouds.BuildConfig;
import com.ndds.freedomclouds.common.ActionButton;
import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.notifications.NotificationHandler;

import java.util.Calendar;

class GeneralSettings extends SettingsPage implements DatePickerDialog.OnDateSetListener {

    public GeneralSettings(Settings.Bundle bundle) {
        super(bundle.activity, bundle.sharedPreferences, R.layout.settings_general, "General", bundle.dialog);
    }

    protected void changeDate() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,this,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private Intent createIntentToRate(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, activity.getPackageName())));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        flags |= Build.VERSION.SDK_INT >= 21 ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        intent.addFlags(flags);
        return intent;
    }

    private void rateApp() {
        Intent intent = createIntentToRate("market://details");
        try {
            activity.startActivity(intent); //market://details
        } catch (ActivityNotFoundException e) {
            intent = createIntentToRate("https://play.google.com/store/apps/details");
            activity.startActivity(intent);
        }
    }

    private void resetDate() {
        Calendar now = Calendar.getInstance();
        activity.updateDate(now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH));
        dialog.dismiss();
        new NotificationHandler(sharedPreferences, activity).removeNotifications();
        Message.show(activity, "reset date to today");
    }

    private void restartApp() {
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(activity.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        activity.finish();
        activity.startActivity(mainIntent);
    }

    private void resetApp(ActionButton button) {
        if(button.getText().toString().contains("Are you sure ?")){
            dialog.dismiss();
            sharedPreferences.edit().clear().apply();
            Message.show(activity, "restarting the app");
            restartApp();
            return;
        }
        button.setColorTheme(R.color.beautyOrange);
        button.setText(R.string.reset_confirmation);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                button.setText("Reset App");
                button.setColorTheme(R.color.beautyGreen);
            }
        }, 4000);
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

    @Override
    void onCreate(ViewGroup settingsView) {
        setListener(R.id.settings_change_date, this::changeDate);
        setListener(R.id.settings_reset_today, this::resetDate);
        setListener(R.id.settings_developer_notice, this::openDeveloperNote);
        setListener(R.id.reset_memory, this::resetApp);
        setListener(R.id.rate_app, this:: rateApp);
        ((TextView) settingsView.findViewById(R.id.versionIndicator))
                .setText(String.format("Version %s", BuildConfig.VERSION_NAME));
        setSwitchListener(R.id.settings_ambient_light, activity.isAmbientLightResponsive(), this::onAmbientLightResponsivenessChanged);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        activity.updateDate(year, month, dayOfMonth);
        Message.show(activity, "start date changed");
        dialog.dismiss();
        new NotificationHandler(sharedPreferences, activity).removeNotifications();
    }
}
