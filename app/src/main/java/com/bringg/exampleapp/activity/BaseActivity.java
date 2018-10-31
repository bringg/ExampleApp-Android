package com.bringg.exampleapp.activity;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.bringg.exampleapp.R;

import java.util.ArrayList;
import java.util.List;

import driver_sdk.PermissionVerifier;


public class BaseActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;
    public static final int REQUEST_CODE_CAMERA = 4;
    public static final String TAG = BaseActivity.class.getSimpleName();

    private AlertDialog mLoadingDialog;
    private boolean mIsForeground;
    private PermissionVerifier.OnPermissionsResultListener onPermissionsResultListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            onRequestWriteExternalStorageResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
            return;
        }
        if (requestCode == REQUEST_CODE_CAMERA) {
            onRequestCameraResult(grantResults[0] == PackageManager.PERMISSION_GRANTED);
        }
        if (onPermissionsResultListener != null) {
            List<String> deniedPermissions = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[i]);
                }
            }
            onPermissionsResultListener.onRequestPermissionsResult(deniedPermissions);
        }
    }

    protected void onRequestCameraResult(boolean allow) {
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

    @Override
    protected void onPause() {
        super.onPause();
        mIsForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsForeground = true;
    }

    public boolean isForeground() {
        return mIsForeground;
    }

    public void setOnPermissionsResultListener(PermissionVerifier.OnPermissionsResultListener onPermissionsResultListener) {
        this.onPermissionsResultListener = onPermissionsResultListener;
    }
}
