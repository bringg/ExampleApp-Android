package com.bringg.exampleapp;

import com.bringg.exampleapp.activity.BaseActivity;

public class UIController {
    private BaseActivity currentActivity;

    public void setCurrentActivity(BaseActivity activity) {
        currentActivity = activity;
    }

    public BaseActivity getCurrentActivity() {
        return currentActivity;
    }

    public boolean isForeground() {
        return currentActivity != null && currentActivity.isForeground();
    }
}
