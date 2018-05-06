package com.bringg.exampleapp;

import android.app.Application;
import android.location.Location;

import org.jetbrains.annotations.Nullable;

import driver_sdk.LeanBringgSDKClient;
import driver_sdk.LeanBringgSDKFactory;
import driver_sdk.account.UserEventsListener;
import driver_sdk.providers.LeanUserStatusProvider;

public class BringgApp extends Application {

    private BringgProvider mBringgProvider;
    private ShiftManager mShiftManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBringgProvider = new BringgProvider(this);
        mShiftManager = new ShiftManager(this, mBringgProvider);

    }


    public BringgProvider getBringg() {
        return mBringgProvider;
    }

    public ShiftManager getShiftManager() {
        return mShiftManager;
    }

    public void notifyLoginSuccess() {
        mShiftManager.load();
    }
}
