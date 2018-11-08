package com.bringg.exampleapp.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.bringg.exampleapp.R;

import java.util.Arrays;

import driver_sdk.BringgSDKClient;
import driver_sdk.PermissionVerifier;


public class BaseActivity extends AppCompatActivity implements PermissionVerifier {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;
    public static final String TAG = BaseActivity.class.getSimpleName();

    private AlertDialog mLoadingDialog;
    private OnPermissionsResultListener onPermissionResultListener;

    @Override
    protected void onStart() {
        super.onStart();
        BringgSDKClient.getInstance().setPermissionVerifier(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BringgSDKClient.getInstance().setPermissionVerifier(null);
    }

    // Permission verifier implementation - will be called when the SDK needs a permission from the user
    @Override
    public void onMissingPermission(@NonNull String... permissions) {
        // Bringg SDK reports this when an action fails due to missing permissions (location updates, geofence detection, etc.)
        Log.i(TAG, "Sdk reported missing permissions, ask for these permissions when appropriate, permissions=" + Arrays.toString(permissions));
    }

    @Override
    public void requestPermissionWithResult(@NonNull String[] permissions, @NonNull OnPermissionsResultListener onPermissionsResultListener) {
        // Bringg SDK requests permissions that are required for an ongoing action  (location updates, geofence detection, etc.)
        // the sdk will wait for the response callback to complete/dismiss the action
        // implementation should request the permissions from the user to enable the functionality
        // callback should be invoked after the permission results are received on the activity/fragment
        this.onPermissionResultListener = onPermissionsResultListener;
        ActivityCompat.requestPermissions(BaseActivity.this, permissions, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // notify Bringg SDK that we got permission results
        if (onPermissionResultListener != null) {
            onPermissionResultListener.onRequestPermissionsResult();
        }

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            onRequestWriteExternalStorageResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
    }

    protected void onRequestWriteExternalStorageResult(boolean allow) {
    }

    protected void showLoadingProgress() {
        hideLoadingProgress();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog_layout, null);
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
}
