package com.bringg.exampleapp;

import android.support.annotation.NonNull;

import driver_sdk.models.AppConfiguration;

class AppConfigurationImpl implements AppConfiguration {
    @Override
    public String getDefaultFormTitle() {
        return null;
    }

    @Override
    public String getDefaultFormButtonLabel() {
        return null;
    }

    @Override
    public String getTaskCancelOtherTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getNoteAddedByYouString() {
        return null;
    }
}
