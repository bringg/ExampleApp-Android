package com.bringg.exampleapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.bringg.exampleapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import driver_sdk.PermissionVerifier;
import driver_sdk.models.CancellationReason;
import driver_sdk.models.NoteData;
import driver_sdk.models.Task;
import driver_sdk.models.Waypoint;
import driver_sdk.models.tasks.PendingTasksData;
import driver_sdk.tasks.TaskEventListener;


public class BaseActivity extends AppCompatActivity implements TaskEventListener {
    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;
    private static final int REQUEST_CODE_CAMERA = 4;
    public static final String TAG = BaseActivity.class.getSimpleName();

    protected BringgProvider mBringgProvider;
    private AlertDialog mLoadingDialog;
    private boolean mIsForeground;
    private PermissionVerifier.OnPermissionsResultListener onPermissionsResultListener;

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
        if (onPermissionsResultListener != null) {
            List<String> deniedPermissions = new ArrayList<String>();

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

    protected void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onTaskAdded(@NonNull Task task) {
        Log.d(TAG, "onTaskAdded");
    }

    @Override
    public void onTaskUpdated(@NonNull Task task) {
        Log.d(TAG, "onTaskUpdated");

    }

    @Override
    public void onTaskRemoved(long l) {
        Log.d(TAG, "onTaskRemoved");

    }

    @Override
    public void onTaskCanceled(long l, @NonNull String s, @NonNull CancellationReason cancellationReason) {
        Log.d(TAG, "onTaskCanceled");

    }

    @Override
    public void onTasksAdded(@NonNull Collection<Task> collection) {
        Log.d(TAG, "onTasksAdded");

    }

    @Override
    public void onTasksUpdated(@NonNull Collection<Task> collection) {
        Log.d(TAG, "onTasksUpdated");

    }

    @Override
    public void onTasksRemoved(@NonNull Collection<Long> collection) {
        Log.d(TAG, "onTasksRemoved");

    }

    @Override
    public void onTasksLoaded(@NonNull Collection<Task> collection) {
        Log.d(TAG, "onTasksLoaded");

    }

    @Override
    public void onTaskStarted(@NonNull Task task, int i) {
        Log.d(TAG, "onTaskStarted");

    }

    @Override
    public void onWayPointArrived(@NonNull Waypoint waypoint) {
        Log.d(TAG, "onWayPointArrived");

    }

    @Override
    public void onWayPointUpdated(@NonNull Task task, @NonNull Waypoint waypoint) {
        Log.d(TAG, "onWayPointUpdated");

    }

    @Override
    public void onNoteAdded(long l, long l1, NoteData noteData) {
        Log.d(TAG, "onNoteAdded");

    }

    @Override
    public void onPendingTaskDataUpdated(@NonNull PendingTasksData pendingTasksData) {
        Log.d(TAG, "onPendingTaskDataUpdated");

    }

    @Override
    public void onNextTaskAvailable(@NonNull Task task) {
        Log.d(TAG, "onNextTaskAvailable");

    }

    @Override
    public void onTaskDone(@NonNull Task task) {
        Log.d(TAG, "onTaskDone");

    }

    @Override
    public void onGrabTaskAdded(@NonNull Task task) {
        Log.d(TAG, "onGrabTaskAdded");

    }

    @Override
    public void onWaypointCheckedIn(@NonNull Waypoint wayPoint) {
        Log.d(TAG, "onWaypointCheckedIn");
    }

    @Override
    public void onFutureTaskListUpdated(@NonNull List<Task> futureTasks) {

    }

    @Override
    public void onMassTasksRemove(@NonNull List<Task> removedTasks, @Nullable CancellationReason cancellationReason) {

    }

    @Override
    public void onWayPointAdded(@NonNull Task task, @NonNull Waypoint waypoint) {

    }

    @Override
    public void onWaypointViewed(long taskId, long wayPointId) {

    }

    public boolean isFroeground() {
        return mIsForeground;
    }

    public void setOnPermissionsResultListener(PermissionVerifier.OnPermissionsResultListener onPermissionsResultListener) {
        this.onPermissionsResultListener = onPermissionsResultListener;
    }
}
