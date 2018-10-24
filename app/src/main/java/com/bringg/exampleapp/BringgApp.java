package com.bringg.exampleapp;

import android.app.Application;
import android.location.Location;

import com.bringg.exampleapp.shifts.ShiftManager;

import driver_sdk.location.activityDetection.ActivityRecognitionListener;
import driver_sdk.location.geofence.GeofenceReceiver;


public class BringgApp extends Application  {

    private BringgProvider mBringgProvider;
    private ShiftManager mShiftManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mBringgProvider = new BringgProvider(this);
        mShiftManager = new ShiftManager(this, mBringgProvider);
        if (getBringg().getClient().loginState().isLoggedIn())
            mShiftManager.load();
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
