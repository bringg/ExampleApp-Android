package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

import driver_sdk.BringgSDKClient;
import driver_sdk.content.WaypointActionUtil;
import driver_sdk.models.configuration.TaskActionItem;
import driver_sdk.models.tasks.flow.ProceedTaskCallback;
import driver_sdk.models.tasks.flow.TaskFlow;

/**
 * This fragment uses the TaskFlow object implementation which automates the task progress
 * 1. Get TaskFlow object from Bringg SDK using BringgSDKClient.getInstance().taskActions().getTaskFlow()
 * 2. Call taskFlow.proceedToNextTaskStep() when current step is done.
 * 3. The implementation will try to proceed to the next state
 * 4. The callback will be invoked with the action result when successful or with the error that prevents going forward with this task
 */
public class TaskFlowFragment extends WaypointFragmentBase implements ProceedTaskCallback {

    private TaskFlow mTaskFlow;

    public static WaypointFragmentBase newInstance(long taskId, long wayPointId) {
        Bundle args = new Bundle();
        TaskFlowFragment fragment = new TaskFlowFragment();
        args.putLong(EXTRA_TASK_ID, taskId);
        args.putLong(EXTRA_WAY_POINT_ID, wayPointId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTaskFlow = BringgSDKClient.getInstance().taskActions().getTaskFlow(mTask.getId());
    }

    @Override
    protected void handleNavigateToWaypointDestination(@NonNull Context context) {
        WaypointActionUtil.navigate(context, mWaypoint);
    }

    @Override
    protected void handleCallCustomer(@NonNull Context context) {
        WaypointActionUtil.makeCall(context, mWaypoint);
    }

    @Override
    protected void handleContactCustomerBySms(@NonNull Context context) {
        WaypointActionUtil.contactMessage(context, mWaypoint);
    }

    @Override
    protected void handleWaypointAction(@NonNull Context context) {
        mTaskFlow.proceedToNextTaskStep(this, mTask, mWaypoint.getId());
    }

    @Override
    public void onTaskDone(long taskId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onTaskDone(taskId);
        }
    }

    @Override
    public void onNextWaypointStarted(long nextWayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onNextWaypointStarted(nextWayPointId);
        }
    }

    @Override
    public void onWaypointDone(long wayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onWaypointDone(wayPointId);
        }
    }

    @Override
    public void onArrivedToWaypoint(long taskId, long wayPointId) {
        updateViews();
    }

    @Override
    public void onTaskStarted(long taskId) {
        updateViews();
    }

    @Override
    public void onTaskAccepted(long taskId) {
        updateViews();
    }

    @Override
    public void onShiftNotActiveError(@NonNull String message) {
        showDialogNotInShift();
    }

    @Override
    public void onMandatoryActionsNotCompleted(@NonNull Set<TaskActionItem> mandatoryRulesSet) {
        showMandatoryActionsNotCompleted(mandatoryRulesSet);
    }
}
