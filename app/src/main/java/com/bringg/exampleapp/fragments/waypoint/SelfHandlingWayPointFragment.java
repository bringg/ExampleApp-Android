package com.bringg.exampleapp.fragments.waypoint;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bringg.exampleapp.BringgSdkTaskActionUtil;
import com.bringg.exampleapp.WaypointPresenter;

public class SelfHandlingWayPointFragment extends WaypointFragmentBase implements WaypointPresenter {

    private static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";
    private static final String EXTRA_WAY_POINT_ID = "com.bringg.exampleapp.tasks.EXTRA_WAY_POINT_ID";

    public static WaypointFragmentBase newInstance(long taskId, long wayPointId) {
        Bundle args = new Bundle();
        SelfHandlingWayPointFragment fragment = new SelfHandlingWayPointFragment();
        args.putLong(EXTRA_TASK_ID, taskId);
        args.putLong(EXTRA_WAY_POINT_ID, wayPointId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void handleNavigateToWaypointDestination(@NonNull Context context) {
        BringgSdkTaskActionUtil.navigate(context, mWaypoint.getLat(), mWaypoint.getLng());
    }

    @Override
    protected void handleCallCustomer(@NonNull Context context) {
        BringgSdkTaskActionUtil.makeCall(context, mWaypoint.getPhone());
    }

    @Override
    protected void handleContactCustomerBySms(@NonNull Context context) {
        BringgSdkTaskActionUtil.contactMessage(context, mWaypoint.getPhone());
    }

    @Override
    protected void handleWaypointAction(@NonNull Context context) {
        BringgSdkTaskActionUtil.proceedToNextTaskStep(this, mTask, mWaypoint.getId());
    }

    // -------------------------------------- presenter implementation -------------------------------------/

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    public void handleNextWaypointStarted(long nextWayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onNextWaypointStarted(nextWayPointId);
        }
    }

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    @Override
    public void handleTaskDone(long taskId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onTaskDone(taskId);
        }
    }

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    @Override
    public void handleWaypointDone(long wayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onWaypointDone(wayPointId);
        }
    }
    // ---------------------------------------------------------------------------------------------------------------/
}
