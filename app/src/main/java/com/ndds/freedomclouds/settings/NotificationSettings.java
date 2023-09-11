package com.ndds.freedomclouds.settings;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ndds.freedomclouds.R;
import com.ndds.freedomclouds.common.Message;
import com.ndds.freedomclouds.notifications.NotificationHandler;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NotificationSettings extends SettingsPage implements TimePickerDialog.OnTimeSetListener {
    VoidAwareTextWatcher textWatcher;
    private NotificationHandler notificationHandler;

    public NotificationSettings(Settings.Bundle bundle) {
        super(bundle.activity, bundle.sharedPreferences, R.layout.setting_notifications, "Notifications", bundle.dialog);
    }

    @Override
    void onCreate(ViewGroup settingsView) {
        int initialInterval = sharedPreferences.getInt(NotificationHandler.NOTIFICATION_REPEAT_INTERVAL, 0);
        notificationHandler = new NotificationHandler(sharedPreferences, activity);
        textWatcher = new VoidAwareTextWatcher(initialInterval < 1 ? null : String.valueOf(initialInterval), settingsView, R.id.editRemindInterval, R.id.notificationAction, R.id.notificationRemove, new String[] {
                "Update Interval",
                "Apply and enable notifications"
        });
        setListener(R.id.notificationAction, this::onCreateOrUpdateInterval);
        setListener(R.id.notificationRemove, this::onDeleteInterval);
        setListener(R.id.remindTimeButton, this::openTimePicker);
        setListener(R.id.saveTemplateButton, this:: onUpdateTemplate);
        setListener(R.id.notificationTemplateClear, this:: clearToDefaultTemplate);
        setListener(R.id.notificationRemindTimeSpecialNotice, this::showNotificationRemindSpecialNotice);

        EditText templateEditor = settingsView.findViewById(R.id.notificationTemplate);
        templateEditor.setText(notificationHandler.getTemplate());

        setDisplayingRemindTime(notificationHandler.getRemindTime());
    }

    private void onUpdateTemplate() {
        String newTemplate = ((EditText) settingsView.findViewById(R.id.notificationTemplate)).getText().toString();
        if (!newTemplate.contains("{days}")) {
            Message.show(activity, "Should include {days} tag");
            return;
        }
        Message.show(activity, "notification template got updated");
        notificationHandler.setTemplate(newTemplate);
    }

    private void showNotificationRemindSpecialNotice() {
        new AlertDialog.Builder(activity)
                .setTitle("Notification Remind Time")
                .setMessage(R.string.remindTimeSpecialNotice)
                .setPositiveButton("Got it!", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void clearToDefaultTemplate() {
        EditText templateEditor = settingsView.findViewById(R.id.notificationTemplate);
        if (templateEditor.getText().toString().isEmpty()) return;
        if (notificationHandler.getTemplate().isEmpty()) {
            templateEditor.setText("");
            return;
        }
        Message.show(activity, "Notification template reset to default");

        templateEditor.setText("");
        notificationHandler.setTemplate(null);
    }

    @SuppressLint("SimpleDateFormat")
    private void setDisplayingRemindTime(int[] newTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, newTime[0]);
        calendar.set(Calendar.MINUTE, newTime[1]);

        ((TextView) settingsView.findViewById(R.id.remindTimeLabel)).setText(
                new SimpleDateFormat("hh:mm aa").format(calendar.getTime())
        );
    }

    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(activity, this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
    }

    private void onDeleteInterval() {
        if(textWatcher.clearValue()) {
            notificationHandler.removeNotifications();
            Message.show(activity, "Notifications disabled");
        }
    }

    private void onCreateOrUpdateInterval() {
        String newIntervalString = textWatcher.inputString;
        int newInterval = newIntervalString == null || newIntervalString.isEmpty() ? 0 : Integer.parseInt(newIntervalString);
        if (newInterval < 1) {
            Message.show(activity, "Interval should be at least 1 day");
            return;
        }
        if(newInterval > 30) {
            Message.show(activity, "Interval is too long maximum is 60 days");
            return;
        }
        Message.show(activity, "Notification interval updated");
        notificationHandler.setRepeatNotification(newInterval);
        textWatcher.updateValue(newIntervalString);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setDisplayingRemindTime(new int[] { hourOfDay, minute });
        if(notificationHandler.setRemindTime(hourOfDay, minute)) {
            Message.show(activity, "Reminding time got updated");
        } else {
            Message.show(activity, "Set interval of days first!");
        }
    }
}
