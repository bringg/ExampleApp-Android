package com.bringg.exampleapp;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;


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
import driver_sdk.storage.db.SecuredStorage;
import driver_sdk.storage.db.SecuredStorageProvider;
import driver_sdk.tasks.TaskEventListener;

public class BringgProvider {

    public static final long EMPTY_USER = 0;
    public static final String BASE_HOST = "https://app.bringg.com";

    private final Context mContext;
    private LeanBringgSDKClient mLeanBringgSDKClient;
    private CopyOnWriteArraySet<TaskEventListener> mTasksListeners;
    private UIController mUIController;


    public BringgProvider(Context context) {

        mTasksListeners = new CopyOnWriteArraySet<>();
        mContext = context.getApplicationContext();
        mUIController = new UIController();
        mLeanBringgSDKClient = new BringgSDKBuilder(mContext.getApplicationContext(), new NotificationProviderImpl())
                .taskEventListener(new TaskEventListenerImpl())
                .build();
    }

    public LeanBringgSDKClient getClient() {
        return mLeanBringgSDKClient;
    }

    public void addTaskListener(TaskEventListener listener) {
        mTasksListeners.add(listener);
    }

    public void removeTaskListener(TaskEventListener listener) {
        mTasksListeners.remove(listener);
    }

    private void notifyWayPointArrived(Waypoint waypoint) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onWayPointArrived(waypoint);
        }
    }

    private void notifyTaskRemoved(long taskId) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskRemoved(taskId);
        }
    }

    private void notifyTaskStarted(Task remoteTask, int i) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskStarted(remoteTask, i);
        }
    }

    private void notifyTaskAdded(Task newTask) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskAdded(newTask);
        }
    }

    private void notifyTaskDone(Task task) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskDone(task);
        }
    }

    private void notifyNextTaskAvailable(Task task) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onNextTaskAvailable(task);
        }
    }

    private void notifyPendingTaskDataUpdated(PendingTasksData pendingTasksData) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onPendingTaskDataUpdated(pendingTasksData);
        }
    }

    private void notifyNoteAdded(long l, long l1, NoteData noteData) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onNoteAdded(l, l1, noteData);
        }
    }

    private void notifyWayPointUpdated(Task task, Waypoint waypoint) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onWayPointUpdated(task, waypoint);
        }
    }

    private void notifyTasksLoaded(Collection<Task> collection) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTasksLoaded(collection);
        }
    }

    private void notifyTasksRemoved(Collection<Long> collection) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTasksRemoved(collection);
        }
    }

    private void notifyTasksUpdated(Collection<Task> collection) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTasksUpdated(collection);
        }
    }

    private void notifyTasksAdded(Collection<Task> collection) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTasksAdded(collection);
        }
    }

    private void notifyTaskCanceled(long l, String s, CancellationReason cancellationReason) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskCanceled(l, s, cancellationReason);
        }
    }

    private void notifyTaskUpdated(Task task) {
        for (TaskEventListener listener : mTasksListeners) {
            listener.onTaskUpdated(task);
        }
    }

    public UIController getUIController() {
        return mUIController;
    }

    private class PermissionVerifierImpl implements PermissionVerifier {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void requestPermission(Context context, int i, String... permissions) {
            Activity activity = mUIController.getCurrentActivity();
            if (activity == null)
                return;
            activity.requestPermissions(permissions, i);
        }

        @Override
        public void requestPermissionWithResult(Context context, int i, OnPermissionsResultListener onPermissionsResultListener, String... strings) {
        }

        @Override
        public String[] getPendingPermissions(Context context, String... strings) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
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
        return new Notification.Builder(mContext)
                .setContentTitle(mContext.getResources().getString(R.string.notification_in_shift_title))
                .setContentText(mContext.getResources().getString(R.string.notification_in_shift_message))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();


    }

    private class TaskEventListenerImpl implements TaskEventListener {
        @Override
        public void onTaskAdded(@NonNull Task task) {
            notifyTaskAdded(task);
        }

        @Override
        public void onTaskUpdated(@NonNull Task task) {
            notifyTaskUpdated(task);
        }

        @Override
        public void onTaskRemoved(long l) {
            notifyTaskRemoved(l);
        }

        @Override
        public void onTaskCanceled(long l, @NonNull String s, @NonNull CancellationReason cancellationReason) {
            notifyTaskCanceled(l, s, cancellationReason);
        }

        @Override
        public void onTasksAdded(@NonNull Collection<Task> collection) {
            notifyTasksAdded(collection);
        }

        @Override
        public void onTasksUpdated(@NonNull Collection<Task> collection) {
            notifyTasksUpdated(collection);

        }

        @Override
        public void onTasksRemoved(@NonNull Collection<Long> collection) {
            notifyTasksRemoved(collection);

        }

        @Override
        public void onTasksLoaded(@NonNull Collection<Task> collection) {
            notifyTasksLoaded(collection);

        }

        @Override
        public void onTaskStarted(@NonNull Task task, int i) {
            notifyTaskStarted(task, i);
        }

        @Override
        public void onWayPointArrived(@NonNull Waypoint waypoint) {
            notifyWayPointArrived(waypoint);
        }

        @Override
        public void onWayPointUpdated(@NonNull Task task, @NonNull Waypoint waypoint) {
            notifyWayPointUpdated(task, waypoint);
        }

        @Override
        public void onNoteAdded(long l, long l1, NoteData noteData) {
            notifyNoteAdded(l, l1, noteData);

        }

        @Override
        public void onPendingTaskDataUpdated(@NonNull PendingTasksData pendingTasksData) {
            notifyPendingTaskDataUpdated(pendingTasksData);

        }

        @Override
        public void onNextTaskAvailable(@NonNull Task task) {
            notifyNextTaskAvailable(task);
        }

        @Override
        public void onTaskDone(@NonNull Task task) {
            notifyTaskDone(task);
        }
    }


}
