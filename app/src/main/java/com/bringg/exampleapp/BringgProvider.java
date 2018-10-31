package com.bringg.exampleapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.bringg.exampleapp.activity.BaseActivity;

import java.util.Arrays;

import driver_sdk.BringgSDKBuilder;
import driver_sdk.LeanBringgSDKClient;
import driver_sdk.PermissionVerifier;
import driver_sdk.providers.NotificationProvider;
import driver_sdk.tasks.TaskEventListener;

public class BringgProvider {

    public static final long EMPTY_USER = 0;
    public static final String BASE_HOST = "https://app.bringg.com";

    private final Context mContext;
    private LeanBringgSDKClient mLeanBringgSDKClient;

    private UIController mUIController;


    public BringgProvider(Context context) {
        mContext = context.getApplicationContext();
        mUIController = new UIController();
        mLeanBringgSDKClient = new BringgSDKBuilder(mContext.getApplicationContext(), new NotificationProviderImpl())
                .setPermissionVerifier(new PermissionVerifierImpl())
                .build();
    }

    public LeanBringgSDKClient getClient() {
        return mLeanBringgSDKClient;
    }

    public void addTaskListener(TaskEventListener listener) {
        mLeanBringgSDKClient.taskEvents().registerTaskEventListener(listener);
    }

    public void removeTaskListener(TaskEventListener listener) {
        mLeanBringgSDKClient.taskEvents().unRegisterTaskEventListener(listener);
    }

    public UIController getUIController() {
        return mUIController;
    }

    private class PermissionVerifierImpl implements PermissionVerifier {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onMissingPermission(@NonNull Context context, @NonNull String... permissions) {
            BaseActivity activity = mUIController.getCurrentActivity();
            if (activity == null)
                return;
            activity.requestPermissions(permissions, 0);
        }

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void requestPermissionWithResult(@NonNull Context context, @NonNull String[] permissions, @NonNull OnPermissionsResultListener resultListener) {
            BaseActivity activity = mUIController.getCurrentActivity();
            if (activity == null) {
                resultListener.onRequestPermissionsResult(Arrays.asList(permissions));
                return;
            }
            activity.setOnPermissionsResultListener(resultListener);
            activity.requestPermissions(permissions, 0);
        }
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
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(mContext.getResources().getString(R.string.notification_in_shift_title))
                .setContentText(mContext.getResources().getString(R.string.notification_in_shift_message));
        Intent activityIntent = new Intent(mContext, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addNextIntent(activityIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }
}
