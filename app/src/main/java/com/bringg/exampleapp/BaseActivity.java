package com.bringg.exampleapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bringg.exampleapp.utils.Utils;

import java.util.Collection;

import driver_sdk.models.CancellationReason;
import driver_sdk.models.NoteData;
import driver_sdk.models.tasks.PendingTasksData;
import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;
import driver_sdk.tasks.TaskEventListener;


public abstract class BaseActivity extends AppCompatActivity implements TaskEventListener {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CODE_CAMERA = 4;

    protected BringgProvider mBringgProvider;
    private AlertDialog mLoadingDialog;
    private boolean mIsForeground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getApplication() instanceof BringgApp) {
            mBringgProvider = ((BringgApp) getApplication()).getBringg();
            mBringgProvider.addTaskListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBringgProvider != null)
            mBringgProvider.removeTaskListener(this);
    }

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
    }

    protected void onRequestCameraResult(boolean allow) {

    }

    protected void onRequestWriteExternalStorageResult(boolean allow) {

    }

    private boolean askPermission(String manifestPermission, int requestCode) {

        if (Utils.isNeedAskRuntimePermission() && ContextCompat.checkSelfPermission(this, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{manifestPermission}, requestCode);
            return true;
        }
        return false;
    }

    protected boolean askCameraPermission() {

        if (Utils.isNeedAskRuntimePermission() && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
            return true;
        }
        return false;
    }

    protected boolean askWriteToExternalStoragePermission() {
        if (Utils.isNeedAskRuntimePermission() && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            return true;
        }
        return false;
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

    @Override
    protected void onPause() {
        super.onPause();
        mIsForeground = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBringgProvider.getUIController().setCurrentActivity(this);
        mIsForeground = true;
    }

    public BringgProvider getBringProvider() {
        return mBringgProvider;
    }

    protected void toast(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onTaskAdded(@NonNull Task task) {

    }

    @Override
    public void onTaskUpdated(@NonNull Task task) {

    }

    @Override
    public void onTaskRemoved(long l) {

    }

    @Override
    public void onTaskCanceled(long l, @NonNull String s, @NonNull CancellationReason cancellationReason) {

    }

    @Override
    public void onTasksAdded(@NonNull Collection<Task> collection) {

    }

    @Override
    public void onTasksUpdated(@NonNull Collection<Task> collection) {

    }

    @Override
    public void onTasksRemoved(@NonNull Collection<Long> collection) {

    }

    @Override
    public void onTasksLoaded(@NonNull Collection<Task> collection) {

    }

    @Override
    public void onTaskStarted(@NonNull Task task, int i) {

    }

    @Override
    public void onWayPointArrived(@NonNull Waypoint waypoint) {

    }

    @Override
    public void onWayPointUpdated(@NonNull Task task, @NonNull Waypoint waypoint) {

    }

    @Override
    public void onNoteAdded(long l, long l1, NoteData noteData) {

    }

    @Override
    public void onPendingTaskDataUpdated(@NonNull PendingTasksData pendingTasksData) {

    }

    @Override
    public void onNextTaskAvailable(@NonNull Task task) {

    }

    @Override
    public void onTaskDone(@NonNull Task task) {

    }

    public boolean isFroeground() {
        return mIsForeground;
    }
}
