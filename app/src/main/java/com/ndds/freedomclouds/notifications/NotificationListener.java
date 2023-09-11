package com.ndds.freedomclouds.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.ndds.freedomclouds.R;

public class NotificationListener extends BroadcastReceiver {
    final static String CHANNEL_ID = "reminder";

    private void createNotification(NotificationHandler notificationHandler, Context context) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Remind", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notify user about days elapsed from events");
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        RemoteViews notificationContent = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        RemoteViews notificationContentLarge = new RemoteViews(context.getPackageName(), R.layout.notification_layout_expanded);
        String textContent = notificationHandler.getNotificationContent();
        notificationContent.setTextViewText(R.id.notificationMessage, textContent);
        notificationContentLarge.setTextViewText(R.id.notificationMessage, textContent);

        Notification notification = builder.setContentTitle("Event Reminder")
                .setCustomContentView(notificationContent)
                .setCustomBigContentView(notificationContentLarge)
                .setSmallIcon(R.drawable.notification_icon)
                .build();
        notificationManager.notify(100, notification);
        notificationHandler.setRepeatNotification();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHandler notificationHandler = new NotificationHandler(context);
        switch (intent.getAction()) {
            case NotificationHandler.ACTION_NOTIFICATION:
                createNotification(notificationHandler, context);
                break;
            case "android.intent.action.BOOT_COMPLETED":
                notificationHandler.setRepeatNotification();
        }
    }
}
