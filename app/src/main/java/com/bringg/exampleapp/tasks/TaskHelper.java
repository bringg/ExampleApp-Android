package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.util.Log;

import com.bringg.exampleapp.BringgApp;
import com.bringg.exampleapp.BringgProvider;

import driver_sdk.models.Task;
import driver_sdk.models.Waypoint;
import driver_sdk.tasks.LeaveWayPointActionCallback;
import driver_sdk.tasks.TaskActionCallback;

import static driver_sdk.models.Task.Status.STATUS_CHECKED_IN;
import static driver_sdk.models.Task.Status.STATUS_DONE;
import static driver_sdk.models.Task.Status.STATUS_STARTED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_LINKED_TASK_NOT_COMPLETED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_NOT_CURRENT_WAY_POINT;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_ALREADY_STARTED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_IN_FUTURE;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_NOT_FOUND;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_NOT_STARTED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_NOT_STARTED_UNKNOWN_ERROR;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TASK_WITH_HIGHER_PRIORITY_FOUND;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_TOO_EARLY_TO_START_TASK;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_ALREADY_ARRIVED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_ALREADY_DONE;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_AT_HOME;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_CHECK_IN_FAILED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_NOT_ARRIVED;
import static driver_sdk.tasks.TaskActionCallback.ERROR_CODE_WAY_POINT_NOT_FOUND;

public class TaskHelper {


    public enum TaskState {
        NOT_STARTED, ACCEPTED, STARTED, DONE
    }

    public enum WayPointState {
        NO_STARTED, STARTED, ARRIVE, LEAVE, UNKNOWN
    }

    private TaskState mState;
    private final TaskStateHelperListener mListener;
    private final Context mContext;
    private final Task mTask;
    private BringgProvider mBringgProvider;


    public TaskHelper(Context context, Task task, TaskHelper.TaskStateHelperListener listener) {
        mTask = task;
        mListener = listener;
        updateStateFromTask();
        mContext = context.getApplicationContext();
        if (mContext instanceof BringgApp) {
            mBringgProvider = ((BringgApp) mContext).getBringg();
        }
    }

    public TaskState getTaskState() {
        return mState;
    }

    public void actionWayPoint(long wayPointId) {
        switch (getWayPointState(wayPointId)) {
            case NO_STARTED:
                break;
            case STARTED:
                arriveWayPoint(wayPointId);
                break;
            case ARRIVE:
                leaveWayPoint(wayPointId);
                break;
            case LEAVE:
                break;
        }
    }

    public WayPointState getWayPointState(long wayPointId) {
        Waypoint waypoint = mTask.getWayPointById(wayPointId);
        if (waypoint == null)
            return WayPointState.UNKNOWN;
        if (waypoint.isDone())
            return WayPointState.LEAVE;
        if (!waypoint.isStarted())
            return WayPointState.NO_STARTED;
        if (!waypoint.isCheckedIn())
            return WayPointState.STARTED;
        return WayPointState.ARRIVE;
    }

    public void acceptTask() {
        if (mTask.isAccepted())
            return;
        ((driver_sdk.models.tasks.Task) mTask).accept();
        mBringgProvider.getClient().taskActions().acceptTask(mTask.getId(), new TaskActionCallbackImpl(TaskActionCallbackImpl.TYPE_ACCEPT_TASK));
    }

    public void startTask() {
        if (mTask.isStarted())
            return;
        mBringgProvider.getClient().taskActions().startTask(mTask.getId(), new TaskActionCallbackImpl(TaskActionCallbackImpl.TYPE_START_TASK));
    }

    private void startWayPoint(long wayPointId) {
        Waypoint waypoint = mTask.getWayPointById(wayPointId);
        if (waypoint == null)
            return;
        if (mListener != null)
            mListener.onWayPointStateChanged(wayPointId, WayPointState.STARTED);
    }

    public void arriveWayPoint(long wayPointId) {
        mBringgProvider.getClient().taskActions().arriveToWayPoint(mTask.getId(), wayPointId, new WayPointActionCallbackImpl(wayPointId));

    }

    private void notifyArriveWayPoint(long wayPointId) {
        if (mListener != null)
            mListener.onWayPointStateChanged(wayPointId, WayPointState.ARRIVE);
    }

    public void leaveWayPoint(long wayPointId) {
        mBringgProvider.getClient().taskActions().leaveWayPoint(mTask.getId(), wayPointId, new LeaveWayPointActionCallbackImpl(wayPointId));
    }

    private void notifyLeaveWayPoint(long wayPointId) {
        if (mListener != null) {
            mListener.onWayPointStateChanged(wayPointId, WayPointState.LEAVE);
        }
    }

    private void setTaskState(TaskState state) {
        if (state == mState)
            return;
        this.mState = state;
        if (mListener != null)
            mListener.onTaskStateChanged(mTask.getId(), mState);
    }

    private void updateStateFromTask() {
        if (mTask.isDone())
            setTaskState(TaskState.DONE);
        else if (mTask.isStarted())
            setTaskState(TaskState.STARTED);
        else
            setTaskState(TaskState.NOT_STARTED);
    }

    private String getMessageFromError(int errorCode) {
        switch (errorCode) {
            case ERROR_CODE_TASK_NOT_FOUND:
                return "Task not found";
            case ERROR_CODE_TASK_ALREADY_STARTED:
                return "Task already started";
            case ERROR_CODE_TASK_NOT_STARTED:
                return "Task not started";
            case ERROR_CODE_TASK_IN_FUTURE:
                return "Task in future";
            case ERROR_CODE_WAY_POINT_ALREADY_ARRIVED:
                return "Way point already arrived";
            case ERROR_CODE_WAY_POINT_NOT_FOUND:
                return "Way point not found";
            case ERROR_CODE_WAY_POINT_NOT_ARRIVED:
                return "Way point not arrived";
            case ERROR_CODE_WAY_POINT_ALREADY_DONE:
                return "Way point already done";
            case ERROR_CODE_NOT_CURRENT_WAY_POINT:
                return "Way point  ";
            case ERROR_CODE_WAY_POINT_AT_HOME:
                return "Way point at home";
            case ERROR_CODE_TOO_EARLY_TO_START_TASK:
                return "Too early to start task";
            case ERROR_CODE_TASK_WITH_HIGHER_PRIORITY_FOUND:
                return "Task with higher priority found";
            case ERROR_CODE_LINKED_TASK_NOT_COMPLETED:
                return "Linked task not completed";
            case ERROR_CODE_TASK_NOT_STARTED_UNKNOWN_ERROR:
                return "Task not started unknown error";
            case ERROR_CODE_WAY_POINT_CHECK_IN_FAILED:
                return "Way point check in failed";
        }
        return "Unknown error ";
    }

    public interface TaskStateHelperListener {

        void onTaskStateChanged(long taskId, TaskState state);

        void onWayPointStateChanged(long wayPointId, WayPointState state);

        void onError(String error);

    }

    private class LeaveWayPointActionCallbackImpl implements LeaveWayPointActionCallback {
        private final long mWayPointId;

        public LeaveWayPointActionCallbackImpl(long wayPointId) {
            mWayPointId = wayPointId;
        }

        @Override
        public void onTaskDone() {
            notifyLeaveWayPoint(mWayPointId);
            setTaskState(TaskState.DONE);
        }

        @Override
        public void onNextWayPointStarted(long wayPointId) {
            notifyLeaveWayPoint(mWayPointId);
            startWayPoint(wayPointId);
        }

        @Override
        public void onActionFailed(int errorCode) {
            if (mListener != null)
                mListener.onError(getMessageFromError(errorCode));
        }
    }

    private class TaskActionCallbackImpl implements TaskActionCallback {
        final static int TYPE_START_TASK = 0;
        final static int TYPE_ACCEPT_TASK = 1;
        int mType;

        TaskActionCallbackImpl(int type) {
            mType = type;
        }

        @Override
        public void onActionDone() {
            switch (mType) {
                case TYPE_START_TASK:
                    setTaskState(TaskState.STARTED);
                    startWayPoint(((driver_sdk.models.tasks.Task) mTask).getFirstWayPoint().getId());
                    break;
                case TYPE_ACCEPT_TASK:
                    setTaskState(TaskState.ACCEPTED);
                    startTask();
                    break;
            }

        }

        @Override
        public void onActionFailed(int errorCode) {
            if (mListener != null)
                mListener.onError(getMessageFromError(errorCode));
        }
    }

    private class WayPointActionCallbackImpl implements TaskActionCallback {
        private final long mWayPointId;

        public WayPointActionCallbackImpl(long wayPointId) {
            mWayPointId = wayPointId;
        }

        @Override
        public void onActionDone() {
            notifyArriveWayPoint(mWayPointId);
        }

        @Override
        public void onActionFailed(int errorCode) {
            if (mListener != null)
                mListener.onError(getMessageFromError(errorCode));
        }
    }


}
