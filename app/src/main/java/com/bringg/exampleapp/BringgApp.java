package com.bringg.exampleapp;

import android.app.Application;


public class BringgApp extends Application {

    private BringgProvider mBringgProvider;

    @Override
    public void onCreate() {
        super.onCreate();
        mBringgProvider = new BringgProvider(this);
    }

    public BringgProvider getBringg() {
        return mBringgProvider;
    }

}
