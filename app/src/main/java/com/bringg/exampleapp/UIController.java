package com.bringg.exampleapp;

import android.app.Activity;

public class UIController {
    private BaseActivity currentActivity;

    public void setCurrentActivity(BaseActivity activity) {
        currentActivity = activity;
    }

    public BaseActivity getCurrentActivity() {
        return currentActivity;
    }

    public boolean isForeground() {
        return currentActivity != null && currentActivity.isFroeground();
    }
}
