package com.bringg.exampleapp;

import android.Manifest;
import android.app.Activity;
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
import android.util.Log;


import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArraySet;

import driver_sdk.BringgSDKBuilder;
import driver_sdk.LeanBringgSDKClient;
import driver_sdk.PermissionVerifier;
import driver_sdk.ShiftStatusListener;
import driver_sdk.connection.realtime.api.RealTimeEventCallback;
import driver_sdk.models.CancellationReason;
import driver_sdk.models.NoteData;
import driver_sdk.models.User;
import driver_sdk.models.WayPointUpdatedDataFromEvent;
import driver_sdk.models.tasks.PendingTasksData;
import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;
import driver_sdk.providers.NotificationProvider;
import driver_sdk.shift.ShiftEventsListener;
import driver_sdk.storage.db.SecuredStorage;
import driver_sdk.storage.db.SecuredStorageProvider;
import driver_sdk.tasks.TaskEventListener;

public class BringgProvider {

    public static final long EMPTY_USER = 0;
    public static final String BASE_HOST = "https://app.bringg.com";

    private final Context mContext;
    private LeanBringgSDKClient mLeanBringgSDKClient;
    private CopyOnWriteArraySet<ShiftEventsListener> mShiftListeners;

    private UIController mUIController;


    public BringgProvider(Context context) {

        mShiftListeners = new CopyOnWriteArraySet<>();
        mContext = context.getApplicationContext();
        mUIController = new UIController();
        mLeanBringgSDKClient = new BringgSDKBuilder(mContext.getApplicationContext(), new NotificationProviderImpl())
                .setShiftEventsListener(new ShiftEventsListenerImpl()).setPermissionVerifier(new PermissionVerifierImpl())
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

    public void addShiftListener(ShiftEventsListener listener) {
        mShiftListeners.add(listener);
    }

    public void removeShiftListener(ShiftEventsListener listener) {
        mShiftListeners.remove(listener);
    }

    private void notifyShiftEnded(long shiftId, String deviceId) {
        for (ShiftEventsListener shiftEventsListener : mShiftListeners)
            shiftEventsListener.onShiftEnded(shiftId, deviceId);
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
            if (activity == null)
                return;
            activity.setOnPermissionsResultListener(resultListener);
            activity.requestPermissions(permissions, 0);
        }
    }

    private class NotificationProviderImpl implements NotificationProvider {
        @NotNull
        @Override
        public Notification getShiftNotification() {
            return generateNotification();
        }
    }

    private Notification generateNotification() {

        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext, channelId)
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
        mBuilder.setContentIntent(resultPendingIntent);

        return mBuilder.build();

    }

    private class ShiftEventsListenerImpl implements ShiftEventsListener {
        @Override
        public void onShiftEnded(long shiftId, @NonNull String deviceId) {
            notifyShiftEnded(shiftId, deviceId);
        }
    }
}
