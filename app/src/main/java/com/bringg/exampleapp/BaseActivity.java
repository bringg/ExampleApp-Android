package com.bringg.exampleapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;

public abstract class BaseActivity extends AppCompatActivity implements BringgProvider.BringgProviderListener {
    protected BringgProvider mBringgProvider;
    private AlertDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getApplication() instanceof BringgApp) {
            mBringgProvider = ((BringgApp) getApplication()).getBringg();
            mBringgProvider.addListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBringgProvider != null)
            mBringgProvider.removeListener(this);
    }

    protected void showLoadingProgress() {
        hideLoadingProgress();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        mLoadingDialog = dialogBuilder.create();
        mLoadingDialog.show();
    }

    protected void hideLoadingProgress() {
        if (mLoadingDialog == null)
            return;
        mLoadingDialog.dismiss();
        mLoadingDialog = null;
    }


    public BringgProvider getBringProvider() {
        return mBringgProvider;
    }


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
    public void onTaskRemoved(long taskId) {

    }

    @Override
    public void onWayPointArrived(Waypoint waypoint) {

    }

    @Override
    public void onUserLoggedOut() {

    }
}
