package com.bringg.exampleapp;

import android.app.Application;

import com.bringg.exampleapp.shifts.ShiftHelper;
import com.bringg.exampleapp.shifts.ShiftManager;


public class BringgApp extends Application {

    private BringgProvider mBringgProvider;
    private ShiftManager mShiftManager;
    private ShiftHelper mShiftHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        mBringgProvider = new BringgProvider(this);
        mShiftManager = new ShiftManager( mBringgProvider);
        mShiftHelper = new ShiftHelper(getApplicationContext(),mShiftManager);
    }

    public BringgProvider getBringg() {
        return mBringgProvider;
    }

    public void notifyLoginSuccess() {
        mShiftManager.getShiftStatusFromRemote();
    }

    public ShiftHelper getShiftHelper() {
        return mShiftHelper;
    }
}
