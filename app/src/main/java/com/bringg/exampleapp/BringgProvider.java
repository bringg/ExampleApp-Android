package com.bringg.exampleapp;

import android.content.Context;
import android.location.Location;

import com.bringg.exampleapp.database.Pref;

import org.jetbrains.annotations.Nullable;

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

public class BringgProvider {

    private final Context mContext;

    private final LeanBringgSDKClient mLeanBringgSDKClient;


    public BringgProvider(Context context) {

        mContext = context.getApplicationContext();
        mLeanBringgSDKClient = LeanBringgSDKFactory.init(mContext,
                new LeanUserStatusProviderImpl(),
                new UserEventsListenerImpl(),
                new SecuredStorageProviderImpl(), new LeanBringgRealTimeEventCallbackImpl());

    }

    public LeanBringgSDKClient getClient() {
        return mLeanBringgSDKClient;
    }

    private class LeanBringgRealTimeEventCallbackImpl implements LeanBringgRealTimeEventCallback {

        @Override
        public void onShiftEnded() {

        }

        @Override
        public void onTaskAdded(Task newTask) {

        }

        @Override
        public void onTaskStarted(Task remoteTask) {

        }

        @Override
        public void onWayPointArrived(Waypoint waypoint) {

        }

        @Override
        public void onTaskRemoved(long id) {

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
}
