package com.bringg.exampleapp.tasks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import driver_sdk.BringgSDKClient;
import driver_sdk.content.StartTaskResult;
import driver_sdk.content.callback.StartTaskCallback;
import driver_sdk.models.Task;
import driver_sdk.models.TaskState;
import driver_sdk.models.WayPointState;
import driver_sdk.tasks.LeaveWayPointActionCallback;
import driver_sdk.tasks.TaskActionCallback;

/**
 * This class demonstrates the basic actions that can be made on a task using BringgSDK api
 * Here you can see how the logic is handled on a fine level.
 * 1. Start task
 * 2. Arrived to a waypoint destination
 * 3. Left waypoint destination
 * 4. Navigate to waypoint
 * 5. Call waypoint customer
 * 6. Send waypoint customer a text message
 */
class BringgSdkTaskActionUtil {


    private static final int ERROR_NOT_IMPLEMENTED = 1000;
    private static final int ERROR_TASK_IS_FUTURE = 1001;
    private static final int ERROR_CAN_NOT_START_TASK = 1002;

    /**
     * Proceed to the next step in task lifecycle: Start -> Arrived to waypoint -> Left waypoint
     * Single task may have one or more waypoints
     * Task is completed once all waypoints are completed
     *
     * @param waypointPresenter UI presenter
     * @param task              Task
     * @param wayPointId        waypointId
     */
    public static void proceedToNextTaskStep(@NonNull WaypointPresenter waypointPresenter, @NonNull Task task, long wayPointId) {

        // task/waypoint actions will fail when not on shift
        // we verify shift state here so we can display UI to start shift and continue with the action
        if (!BringgSDKClient.getInstance().shiftState().isOnShift()) {
            waypointPresenter.showDialogNotInShift();
            return;
        }

        long taskId = task.getId();
        WayPointState currentWayPointState = task.getWayPointState(wayPointId);
        switch (currentWayPointState) {
            case NOT_STARTED:
                startTask(waypointPresenter, taskId);
                break;
            case DONE:
                waypointPresenter.handleWaypointDone(wayPointId);
                handleTaskState(waypointPresenter, task, taskId, wayPointId, currentWayPointState);
                break;
            case STARTED_AND_ON_SITE:
            case STARTED_AND_NOT_ON_SITE:
                handleWaypointState(waypointPresenter, currentWayPointState, taskId, wayPointId);
            default:
                waypointPresenter.onError(ERROR_NOT_IMPLEMENTED, "Waypoint state not yet implemented on this demo app, state=" + currentWayPointState);
                break;
        }

    }

    static void handleTaskState(@NonNull WaypointPresenter waypointPresenter, @NonNull Task task, long taskId, long wayPointId, WayPointState currentWayPointState) {
        TaskState currentTaskState = task.getTaskState();
        switch (currentTaskState) {
            case FREE:
                // TODO: implement grab free tasks
                return;
            case NOT_ACCEPTED:
                acceptTask(waypointPresenter, taskId);
            case NOT_STARTED:
            case AVAILABLE_TO_START:
                startTask(waypointPresenter, taskId);
                return;
            case DONE:
                // there are no more waypoints for this task - task is done, we can return to the task list view
                waypointPresenter.handleTaskDone(taskId);
                return;
            case FUTURE:
                waypointPresenter.onError(ERROR_TASK_IS_FUTURE, "This is a future task, it is not available to start yet.");
                break;
            case STARTED:
            case STARTED_AND_CURRENT_WAYPOINT_ON_SITE:
                handleWaypointState(waypointPresenter, currentWayPointState, taskId, wayPointId);
                break;
            default:
                waypointPresenter.onError(ERROR_NOT_IMPLEMENTED, "Task state not yet implemented on this demo app, state=" + currentTaskState);
                break;
        }
    }

    private static void handleWaypointState(@NonNull WaypointPresenter waypointPresenter, @NonNull WayPointState currentWayPointState, long taskID, long wayPointId) {
        switch (currentWayPointState) {
            case NOT_STARTED:
                // task has not started, use Bringg SDK to start it
                startTask(waypointPresenter, taskID);
                break;
            case STARTED_AND_NOT_ON_SITE:
                // task has started
                // next step happens when the user arrives to the current waypoint destination
                // use Bringg SDK to notify when the user is on-site
                BringgSDKClient.getInstance().taskActions().arriveToWayPoint(taskID, wayPointId, new TaskActionCallback() {
                    @Override
                    public void onActionDone() {
                        waypointPresenter.updateViews();
                    }

                    @Override
                    public void onActionFailed(int errorCode) {
                        waypointPresenter.onError(errorCode, "ArriveToWayPoint failed, errorCode=" + errorCode);
                    }
                });
                break;
            case STARTED_AND_ON_SITE:
                // user has arrived to the current waypoint destination and is now leaving the site
                // user Bringg SDK to notify when the user is done with the current waypoint and is ready to proceed to the next waypoint/task
                BringgSDKClient.getInstance().taskActions().leaveWayPoint(taskID, wayPointId, new LeaveWayPointActionCallback() {
                    @Override
                    public void onTaskDone() {
                        // there are no more waypoints for this task - task is done, we can return to the task list view
                        waypointPresenter.handleTaskDone(taskID);
                    }

                    @Override
                    public void onNextWayPointStarted(long nextWayPointId) {
                        // current waypoint is done, sdk automatically started the next waypoint of this task
                        waypointPresenter.handleNextWaypointStarted(nextWayPointId);
                    }

                    @Override
                    public void onActionFailed(int errorCode) {
                        waypointPresenter.onError(errorCode, "LeaveWayPoint failed, errorCode=" + errorCode);
                    }
                });
                break;
            case DONE:
                // this waypoint is done
                // we should proceed to the next waypoint/task if available
                waypointPresenter.handleWaypointDone(wayPointId);
                break;
        }
    }

    /**
     * Start task
     * This is the first step in the task lifecycle
     *
     * @param waypointPresenter UI presenter
     * @param taskId            task id
     */
    private static void startTask(@NonNull WaypointPresenter waypointPresenter, long taskId) {
        BringgSDKClient.getInstance().taskActions().startTask(taskId, new StartTaskCallback() {
            @Override
            public void onStartTaskResult(@NonNull StartTaskResult startTaskResult) {
                switch (startTaskResult) {
                    case SUCCESS:
                        // task started successfully
                        waypointPresenter.updateViews();
                        break;
                    case FAILED_BLOCKED_BY_TASK_WITH_HIGHER_PRIORITY:
                        // user can't start this task, user should start higher priority task first
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_TASK_WITH_HIGHER_PRIORITY_FOUND:
                        // there is a task with higher priority, implementation should decide whether to ignore this state and start the task anyway/notify the user/block starting
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_IS_LINKED_TASK_AND_PREVIOUS_NOT_COMPLETED:
                        // user can't start this task, user should complete the previous linked task first
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case TASK_NOT_ACCEPTED:
                        // this task was not accepted by the user, show accept UI so the user can accept it
                        acceptTask(waypointPresenter, taskId);
                        break;
                    case FAILED_EARLY_FOR_START_TASK:
                        // the user is too early for starting this task, implementation should show a message with the task time window to the user
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_IS_FUTURE:
                        // this task is planned for the future and can't be started
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_NO_ACTIVE_SHIFT:
                        // shift state is currently off, show start shift UI
                        waypointPresenter.showDialogNotInShift();
                        break;
                    case FAILED_TASK_NOT_FOUND_LOCALLY:
                        // task was removed/canceled and can't be found locally
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case INTERNAL_START_FAILURE:
                        // internal Bringg SDK error when trying to start the task, shouldn't happen
                        waypointPresenter.onError(ERROR_CAN_NOT_START_TASK, "Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                }
            }
        });
    }

    /**
     * Accept an un-accepted task
     * Tasks may be sent as an offer to the user and he must accept the task before starting it.
     *
     * @param waypointPresenter UI presenter
     * @param taskId            task id
     */
    private static void acceptTask(@NonNull WaypointPresenter waypointPresenter, long taskId) {
        BringgSDKClient.getInstance().taskActions().acceptTask(taskId, new TaskActionCallback() {
            @Override
            public void onActionDone() {
                // task accepted successfully
                waypointPresenter.updateViews();
            }

            @Override
            public void onActionFailed(int errorCode) {
                waypointPresenter.onError(errorCode, "Accept task failed, errorCode=" + errorCode);
            }
        });
    }

    /**
     * Navigate to waypoint destination
     *
     * @param context context
     * @param lat     waypoint latitude from waypoint.getLat()
     * @param lng     waypoint longitude from waypoint.getLng()
     */
    public static void navigate(@NonNull Context context, double lat, double lng) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:" + lat + "," + lng));
        context.startActivity(intent);
    }

    /**
     * Call the customer
     *
     * @param context context
     * @param phone   customer phone number from waypoint.getPhone()
     */
    public static void makeCall(@NonNull Context context, @NonNull String phone) {
        String number = "tel:" + phone;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            context.startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(number)));
        } else {
            context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(number)));
        }
    }

    /**
     * Send sms message to the customer
     *
     * @param context context
     * @param phone   customer phone number from waypoint.getPhone()
     */
    public static void contactMessage(@NonNull Context context, @NonNull String phone) {
        Uri uri = Uri.parse("smsto:" + phone);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        context.startActivity(intent);
    }

}
