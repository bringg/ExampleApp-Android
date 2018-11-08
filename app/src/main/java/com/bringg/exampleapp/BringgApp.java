package com.bringg.exampleapp;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bringg.exampleapp.activity.TaskListActivity;

import driver_sdk.BringgSDKBuilder;
import driver_sdk.providers.NotificationProvider;


public class BringgApp extends Application {

    private static final String TAG = "BringgExampleApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // init Bringg SDK
        // we provide here:
        // 1. context
        // 2. Notification provider - provides the foreground service notification
        new BringgSDKBuilder(this, new NotificationProviderImpl())
                .build();
    }

    private class NotificationProviderImpl implements NotificationProvider {
        @NonNull
        @Override
        public Notification getShiftNotification() {
            return generateNotification();
        }
    }

    private Notification generateNotification() {

        String channelId = "channel-01";
        String channelName = "Channel Name";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Resources resources = getResources();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(resources.getString(R.string.notification_in_shift_title))
                .setContentText(resources.getString(R.string.notification_in_shift_message));
        Intent activityIntent = new Intent(this, TaskListActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(activityIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

}
