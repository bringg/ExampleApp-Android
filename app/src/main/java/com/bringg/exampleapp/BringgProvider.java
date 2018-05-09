package com.bringg.exampleapp;

import android.content.Context;
import android.location.Location;

import com.bringg.exampleapp.database.Pref;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import driver_sdk.LeanBringgRealTimeEventCallback;
import driver_sdk.LeanBringgSDKClient;
import driver_sdk.LeanBringgSDKFactory;
import driver_sdk.account.UserEventsListener;
import driver_sdk.models.User;
import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;
import driver_sdk.providers.LeanUserStatusProvider;
import driver_sdk.storage.db.SecuredStorage;
import driver_sdk.storage.db.SecuredStorageProvider;
import driver_sdk.util.TimeUtil;

public class BringgProvider {

    public static final long EMPTY_USER = 0;
    public static final String BASE_HOST = "https://app.bringg.com";

    private final Context mContext;

    private final LeanBringgSDKClient mLeanBringgSDKClient;

    private CopyOnWriteArraySet<BringgProviderListener> mListeners;


    public BringgProvider(Context context) {

        mListeners = new CopyOnWriteArraySet<>();
        mContext = context.getApplicationContext();
        TimeUtil.init(mContext);
        mLeanBringgSDKClient = LeanBringgSDKFactory.init(mContext,
                new LeanUserStatusProviderImpl(),
                new UserEventsListenerImpl(),
                new SecuredStorageProviderImpl(), new LeanBringgRealTimeEventCallbackImpl());

    }

    public LeanBringgSDKClient getClient() {
        return mLeanBringgSDKClient;
    }

    public void addListener(BringgProviderListener listener) {
        mListeners.add(listener);
    }

    public void removeListener(BringgProviderListener listener) {
        mListeners.remove(listener);
    }

    private class LeanBringgRealTimeEventCallbackImpl implements LeanBringgRealTimeEventCallback {

        @Override
        public void onShiftEnded() {
            notifyShiftEnded();
        }

        @Override
        public void onTaskAdded(Task newTask) {
            notifyTaskAdded(newTask);

        }

        @Override
        public void onTaskStarted(Task remoteTask) {
            notifyTaskStarted(remoteTask);
        }

        @Override
        public void onWayPointArrived(Waypoint waypoint) {
            notifyWayPointArrived(waypoint);
        }

        @Override
        public void onTaskRemoved(long id) {
            notifyTaskRemoved(id);
        }
    }

    private void notifyWayPointArrived(Waypoint waypoint) {
        for (BringgProviderListener listener : mListeners) {
            listener.onWayPointArrived(waypoint);
        }
    }

    private void notifyTaskRemoved(long taskId) {
        for (BringgProviderListener listener : mListeners) {
            listener.onTaskRemoved(taskId);
        }
    }

    private void notifyTaskStarted(Task remoteTask) {
        for (BringgProviderListener listener : mListeners) {
            listener.onTaskStarted(remoteTask);
        }
    }

    private void notifyTaskAdded(Task newTask) {
        for (BringgProviderListener listener : mListeners) {
            listener.onTaskAdded(newTask);
        }
    }

    private void notifyShiftEnded() {
        for (BringgProviderListener listener : mListeners) {
            listener.onShiftEnded();
        }
    }
    private void notifyUserLoggedout() {
        for (BringgProviderListener listener : mListeners) {
            listener.onUserLoggedOut();
        }
    }


    private class SecuredStorageProviderImpl implements SecuredStorageProvider {

        @Nullable
        @Override
        public SecuredStorage getStorage() {
            return new SecuredStorageImpl();
        }

        @Override
        public void clearStorage() {
            Pref.get(mContext).clear();
        }
    }

    private class UserEventsListenerImpl implements UserEventsListener {
        @Override
        public void onUserLoggedOut() {
            notifyUserLoggedout();
        }
    }


    private class LeanUserStatusProviderImpl implements LeanUserStatusProvider {

        @Override
        public long getSecondsSinceLastLocationReport() {
            return 0;
        }

        @Override
        public int getBatteryStatus() {
            return 0;
        }

        @Nullable
        @Override
        public Location getLastLocation() {
            return null;
        }

        @Override
        public boolean isLocationAvailable() {
            return false;
        }
    }

    private class SecuredStorageImpl implements SecuredStorage {

        @Override
        public void addUser(User user) {
            Pref.get(mContext).setUser(user);
        }

        @Override
        public void updateUser(User user) {
            Pref.get(mContext).setUser(user);
        }

        @Override
        public User getUser() {
            return Pref.get(mContext).getUser();
        }
    }

    interface BringgProviderListener {

        void onShiftEnded();

        void onTaskAdded(Task newTask);

        void onTaskStarted(Task remoteTask);

        void onTaskRemoved(long taskId);

        void onWayPointArrived(Waypoint waypoint);

        void onUserLoggedOut();
    }
}
