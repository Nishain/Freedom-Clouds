package com.ndds.freedomclouds.notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.ndds.freedomclouds.MainActivity;
import com.ndds.freedomclouds.R;

import java.util.Calendar;

public class NotificationHandler {
    private final AlarmManager alarmManager;
    private final SharedPreferences sharedPreferences;
    private final PendingIntent pendingIntent;
    public static int REQUEST_CODE = 456;
    public static String NOTIFICATION_REPEAT_INTERVAL = "notificationRepeatInterval";
    public static String NOTIFICATION_REMIND_TIME = "notificationRemindTime";
    public static String NOTIFICATION_CONTENT_TEMPLATE = "notificationContentTemplate";

    public final static String ACTION_NOTIFICATION = "SEND_NOTIFICATION";
    public static String DEFAULT_TIME = "8:45"; // 8:30 AM
    private final String NOTIFICATION_DEFAULT_TEMPLATE;
    private static final String UTC_TIME_ON_APP_START = "timeOnAppStarted";

    public static boolean didBootReceiverFailed(SharedPreferences sharedPreferences, Context context) {
        Calendar calendar = Calendar.getInstance();
        boolean isNotificationEnabled = sharedPreferences.contains(NOTIFICATION_REPEAT_INTERVAL);
        long timeAppStarted = sharedPreferences.getLong(UTC_TIME_ON_APP_START, calendar.getTimeInMillis());
        Intent intent = new Intent(context, NotificationListener.class).setAction(ACTION_NOTIFICATION);
        boolean isNotScheduled = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_NO_CREATE) == null;

        boolean result = isNotificationEnabled &&
                SystemClock.elapsedRealtime() < (calendar.getTimeInMillis() - timeAppStarted)
                && isNotScheduled;
        sharedPreferences.edit().putLong(UTC_TIME_ON_APP_START, calendar.getTimeInMillis()).apply();
        return result;
    }

    public NotificationHandler(Context context) {
        NOTIFICATION_DEFAULT_TEMPLATE = context.getString(R.string.defaultNotificationContent);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        sharedPreferences = context.getSharedPreferences("configuration",Context.MODE_PRIVATE);
        Intent intent = new Intent(context, NotificationListener.class).setAction(ACTION_NOTIFICATION);
        pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    public NotificationHandler(SharedPreferences sharedPreferences, Context context) {
        NOTIFICATION_DEFAULT_TEMPLATE = context.getString(R.string.defaultNotificationContent);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.sharedPreferences = sharedPreferences;
        Intent intent = new Intent(context, NotificationListener.class).setAction(ACTION_NOTIFICATION);
        pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent,
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    @SuppressLint("DefaultLocale")
    public String getNotificationContent() {
        Calendar startDate = MainActivity.getDateComponents(sharedPreferences);
        int daysElapsed = (int) ((Calendar.getInstance().getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 60 * 60 * 24));
        return sharedPreferences.getString(NOTIFICATION_CONTENT_TEMPLATE, NOTIFICATION_DEFAULT_TEMPLATE)
                .replace("{days}", String.valueOf(daysElapsed));
    }

    public String getTemplate() {
        return sharedPreferences.getString(NOTIFICATION_CONTENT_TEMPLATE, "");
    }

    public void setRepeatNotification() {
        int repeatInterval = sharedPreferences.getInt(NOTIFICATION_REPEAT_INTERVAL, 0);
        if (repeatInterval == 0) {
            // no notifications are set
            return;
        }
        assignRepeatNotification(repeatInterval, getRemindTime());
    }

    public boolean setRepeatNotification(int hourOfDay, int minute) {
        int repeatInterval = sharedPreferences.getInt(NOTIFICATION_REPEAT_INTERVAL, 0);
        if (repeatInterval == 0) {
            // no notifications are set
            return false;
        }
        assignRepeatNotification(repeatInterval, new int[] { hourOfDay, minute });
        return true;
    }

    @SuppressLint("DefaultLocale")
    public boolean setRemindTime(int hourOfDay, int minute) {
        sharedPreferences.edit().putString(NOTIFICATION_REMIND_TIME, String.format("%d:%d", hourOfDay, minute)).apply();
        return setRepeatNotification(hourOfDay, minute);
    }

    public int[] getRemindTime() {
        String[] remindTime = sharedPreferences.getString(NOTIFICATION_REMIND_TIME, DEFAULT_TIME)
                .split(":");
        return new int[] {
                Integer.parseInt(remindTime[0]),
                Integer.parseInt(remindTime[1])
        };
    }

    public void setRepeatNotification(int intervalInDays) {
        sharedPreferences.edit().putInt(NOTIFICATION_REPEAT_INTERVAL, intervalInDays).apply();
        assignRepeatNotification(intervalInDays, getRemindTime());
    }

    private void assignRepeatNotification(int intervalInDays, int[] timeOffset) {
        long repeatIntervalMillis = (long) intervalInDays * 24 * 60 * 60 * 1000;
        Calendar startTime = MainActivity.getDateComponents(sharedPreferences);
        startTime.set(Calendar.HOUR_OF_DAY, timeOffset[0]);
        startTime.set(Calendar.MINUTE, timeOffset[1]);
        long currentUTC = Calendar.getInstance().getTimeInMillis();
        long startOffset = repeatIntervalMillis - ((currentUTC - startTime.getTimeInMillis()) % repeatIntervalMillis) + 1;

        alarmManager.cancel(pendingIntent);
        alarmManager.set(AlarmManager.RTC_WAKEUP, currentUTC + startOffset, pendingIntent);
    }

    public void setTemplate(String newTemplate) {
        if (newTemplate == null) {
            sharedPreferences.edit().remove(NOTIFICATION_CONTENT_TEMPLATE).apply();
            return;
        }
        sharedPreferences.edit().putString(NOTIFICATION_CONTENT_TEMPLATE, newTemplate).apply();
    }

    public void removeNotifications() {
        sharedPreferences.edit().remove(NOTIFICATION_REPEAT_INTERVAL).apply();
        alarmManager.cancel(pendingIntent);
    }
}
