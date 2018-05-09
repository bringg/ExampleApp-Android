package com.bringg.exampleapp;

import android.app.Application;

import com.bringg.exampleapp.shifts.ShiftManager;

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
