package com.bringg.exampleapp.tasks;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.CircleTransform;
import com.bringg.exampleapp.utils.Utils;
import com.squareup.picasso.Picasso;

import driver_sdk.BringgSDKClient;
import driver_sdk.content.StartTaskResult;
import driver_sdk.content.callback.StartTaskCallback;
import driver_sdk.models.Task;
import driver_sdk.models.TaskState;
import driver_sdk.models.WayPointState;
import driver_sdk.models.Waypoint;
import driver_sdk.shift.StartShiftResultCallback;
import driver_sdk.tasks.LeaveWayPointActionCallback;
import driver_sdk.tasks.TaskActionCallback;

import static com.bringg.exampleapp.BringgProvider.BASE_HOST;

public class WayPointFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "WayPointFragment";

    private static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";
    private static final String EXTRA_WAY_POINT_ID = "com.bringg.exampleapp.tasks.EXTRA_WAY_POINT_ID";
    private static final int REQUEST_PERMISSION_CALL_PHONE = 1;
    private TextView mTvAddress;
    private TextView mTvScheduledFor;
    private TextView mTvUserName;
    private ImageView mImgProfile;
    private View mBtnContactPhone;
    private View mBtnContactMessage;
    private Button mBtnAction;
    @NonNull
    private Task mTask;
    @NonNull
    private Waypoint mWaypoint;
    @Nullable
    private InteractionCallback mInteractionCallback;

    public static WayPointFragment newInstance(long taskId, long wayPointId) {
        Bundle args = new Bundle();
        WayPointFragment fragment = new WayPointFragment();
        args.putLong(EXTRA_TASK_ID, taskId);
        args.putLong(EXTRA_WAY_POINT_ID, wayPointId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InteractionCallback) {
            mInteractionCallback = (InteractionCallback) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

            long taskId = getArguments().getLong(EXTRA_TASK_ID);
            long waypointId = getArguments().getLong(EXTRA_WAY_POINT_ID);

            Waypoint waypoint = null;
            // get the task by taskId from the SDK, might be null if removed or canceled
            Task task = BringgSDKClient.getInstance().taskActions().getTaskById(taskId);
            if (task != null) {
                // get the waypoint by waypointId from the SDK, might be null if removed or canceled
                waypoint = task.getWayPointById(waypointId);
            }

            if (task == null || waypoint == null) {
                if (mInteractionCallback != null) {
                    mInteractionCallback.onWaypointFragmentDataMissing(task, waypointId);
                }
            } else {
                mTask = task;
                mWaypoint = waypoint;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_way_point, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        initViews();
    }

    private void findViews(View view) {
        mTvAddress = view.findViewById(R.id.tv_way_point_address);
        mTvScheduledFor = view.findViewById(R.id.tv_way_point_scheduled_for);
        mTvUserName = view.findViewById(R.id.tv_way_point_user_name);
        mImgProfile = view.findViewById(R.id.img_way_point_user);
        mBtnContactPhone = view.findViewById(R.id.btn_way_point_contact_phone);
        mBtnContactMessage = view.findViewById(R.id.btn_way_point_contact_message);
        mBtnAction = view.findViewById(R.id.btn_way_point_action);
    }

    private void initViews() {

        mTvAddress.setOnClickListener(this);
        mBtnContactPhone.setOnClickListener(this);
        mBtnContactMessage.setOnClickListener(this);
        mBtnAction.setOnClickListener(this);

        mTvAddress.setText(mWaypoint.getAddress());
        mTvScheduledFor.setText(Utils.isoToStringDate(mWaypoint.getScheduledAt()));
        mTvUserName.setText(mWaypoint.getCustomer().name);
        String imgUrl = mWaypoint.getCustomer().imageUrl;
        if (TextUtils.isEmpty(imgUrl))
            return;
        if (!imgUrl.contains("http"))
            imgUrl = BASE_HOST + imgUrl;
        Picasso.get().load(imgUrl).transform(new CircleTransform()).into(mImgProfile);

        updateViews();
    }

    protected void updateViews() {
        if (!isAdded())
            return;

        // task/waypoint actions will fail when not on shift
        // we verify shift state here so we can display UI to start shift and continue with the action
        if (!BringgSDKClient.getInstance().shiftState().isOnShift()) {
            mBtnAction.setText(R.string.start_shift);
            mBtnAction.setVisibility(View.VISIBLE);
            return;
        }
        updateViewsByTaskState(mTask.getTaskState());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                Toast.makeText(getActivity(), "Sorry!!! Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_way_point_address:
                navigate();
                break;
            case R.id.btn_way_point_contact_message:
                contactMessage();
                break;
            case R.id.btn_way_point_contact_phone:
                contactPhone();
                break;
            case R.id.btn_way_point_action:
                actionWayPoint();
                break;
        }
    }

    private void updateViewsByTaskState(TaskState taskState) {
        switch (taskState) {
            case NOT_STARTED:
                mBtnAction.setVisibility(View.GONE);
                break;
            case STARTED:
            case STARTED_AND_CURRENT_WAYPOINT_ON_SITE:
                WayPointState wayPointState = mTask.getWayPointState(mWaypoint.getId());
                updateViewsByWayPointState(wayPointState);
            case DONE:
                break;
        }
    }

    private void updateViewsByWayPointState(WayPointState wayPointState) {
        switch (wayPointState) {
            case NOT_STARTED:
                mBtnAction.setVisibility(View.GONE);
                break;
            case STARTED_AND_NOT_ON_SITE:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.arrived);
                mBtnAction.setEnabled(true);
                break;
            case STARTED_AND_ON_SITE:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.leave);
                mBtnAction.setEnabled(true);
                break;
            case DONE:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.completed);
                mBtnAction.setEnabled(false);
                break;
        }
    }

    private void contactPhone() {
        if (!askPermission(Manifest.permission.CALL_PHONE, REQUEST_PERMISSION_CALL_PHONE))
            makeCall();
    }

    private boolean askPermission(String manifestPermission, int requestCode) {
        if (Utils.isNeedAskRuntimePermission() && ContextCompat.checkSelfPermission(getContext(), manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{manifestPermission}, requestCode);
            return true;
        }
        return false;
    }

    private void makeCall() {
        String number = "tel:" + mWaypoint.getPhone();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        startActivity(callIntent);
    }

    private void contactMessage() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("address", mWaypoint.getPhone());
        intent.putExtra("sms_body", "message");

        startActivity(intent);
    }

    private void navigate() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:" + mWaypoint.getLat() + "," + mWaypoint.getLng()));
        startActivity(intent);
    }

    // task/waypoint actions handling
    public void actionWayPoint() {

        long wayPointId = mWaypoint.getId();
        // task/waypoint actions will fail when not on shift
        // we verify shift state here so we can display UI to start shift and continue with the action
        if (!BringgSDKClient.getInstance().shiftState().isOnShift()) {
            showDialogNotInShift();
            return;
        }

        WayPointState currentWayPointState = mTask.getWayPointState(wayPointId);
        switch (currentWayPointState) {
            case NOT_STARTED:
                // task has not started, use Bringg SDK to start it
                startTask();
                break;
            case STARTED_AND_NOT_ON_SITE:
                // task has started
                // next step happens when the user arrives to the current waypoint destination
                // use Bringg SDK to notify when the user is on-site
                BringgSDKClient.getInstance().taskActions().arriveToWayPoint(mTask.getId(), wayPointId, new TaskActionCallback() {
                    @Override
                    public void onActionDone() {
                        updateViews();
                    }

                    @Override
                    public void onActionFailed(int errorCode) {
                        showError("ArriveToWayPoint failed, errorCode=" + errorCode);
                    }
                });
                break;
            case STARTED_AND_ON_SITE:
                // user has arrived to the current waypoint destination and is now leaving the site
                // user Bringg SDK to notify when the user is done with the current waypoint and is ready to proceed to the next waypoint/task
                BringgSDKClient.getInstance().taskActions().leaveWayPoint(mTask.getId(), wayPointId, new LeaveWayPointActionCallback() {
                    @Override
                    public void onTaskDone() {
                        // there are no more waypoints for this task - task is done, we can return to the task list view
                        WayPointFragment.this.onTaskDone(mTask.getId());
                    }

                    @Override
                    public void onNextWayPointStarted(long nextWayPointId) {
                        // current waypoint is done, sdk automatically started the next waypoint of this task
                        onNextWaypointStarted(nextWayPointId);
                    }

                    @Override
                    public void onActionFailed(int errorCode) {
                        showError("LeaveWayPoint failed, errorCode=" + errorCode);
                    }
                });
                break;
            case DONE:
                // this waypoint is done
                // we should proceed to the next waypoint/task if available
                onWaypointDone(wayPointId);
                break;
        }
    }

    private void startTask() {
        BringgSDKClient.getInstance().taskActions().startTask(mTask.getId(), new StartTaskCallback() {
            @Override
            public void onStartTaskResult(@NonNull StartTaskResult startTaskResult) {
                switch (startTaskResult) {
                    case SUCCESS:
                        // task started successfully
                        updateViews();
                        break;
                    case FAILED_BLOCKED_BY_TASK_WITH_HIGHER_PRIORITY:
                        // user can't start this task, user should start higher priority task first
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_TASK_WITH_HIGHER_PRIORITY_FOUND:
                        // there is a task with higher priority, implementation should decide whether to ignore this state and start the task anyway/notify the user/block starting
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_IS_LINKED_TASK_AND_PREVIOUS_NOT_COMPLETED:
                        // user can't start this task, user should complete the previous linked task first
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case TASK_NOT_ACCEPTED:
                        // this task was not accepted by the user, show accept UI so the user can accept it
                        acceptTask();
                        break;
                    case FAILED_EARLY_FOR_START_TASK:
                        // the user is too early for starting this task, implementation should show a message with the task time window to the user
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_IS_FUTURE:
                        // this task is planned for the future and can't be started
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case FAILED_NO_ACTIVE_SHIFT:
                        // shift state is currently off, show start shift UI
                        showDialogNotInShift();
                        break;
                    case FAILED_TASK_NOT_FOUND_LOCALLY:
                        // task was removed/canceled and can't be found locally
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                    case INTERNAL_START_FAILURE:
                        // internal Bringg SDK error when trying to start the task, shouldn't happen
                        showError("Error starting task, startTaskResult=" + startTaskResult.name());
                        break;
                }
            }
        });
    }

    public void acceptTask() {
        BringgSDKClient.getInstance().taskActions().acceptTask(mTask.getId(), new TaskActionCallback() {
            @Override
            public void onActionDone() {
                // task accepted successfully
                updateViews();
            }

            @Override
            public void onActionFailed(int errorCode) {
                showError("Accept task failed, errorCode=" + errorCode);
            }
        });
    }

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    private void onNextWaypointStarted(long nextWayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onNextWaypointStarted(nextWayPointId);
        }
    }

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    private void onTaskDone(long taskId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onTaskDone(taskId);
        }
    }

    // the resulting task/waypoint events
    // will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
    // implementation can choose between listening to a specific event callback and registering a global TaskActionEventListener (or even both)
    private void onWaypointDone(long wayPointId) {
        if (mInteractionCallback != null) {
            mInteractionCallback.onWaypointDone(wayPointId);
        }
    }

    private void showDialogNotInShift() {
        Context context = getContext();
        if (context == null) return;
        new AlertDialog.Builder(context).
                setTitle(R.string.not_in_shift_title).
                setMessage(R.string.not_in_shift_message).
                setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startShift();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    // use Bringg SDK to start shift
    private void startShift() {
        BringgSDKClient.getInstance().shiftActions().startShiftAndWaitForApproval(true, new StartShiftResultCallback() {
            @Override
            public void onShiftStarted() {
                updateViews();
            }

            @Override
            public void onShiftStartFailed(int responseCode) {
                showError("Error starting shift, responseCode=" + responseCode);
            }
        });
    }

    private void showError(@NonNull String message) {
        Snackbar.make(mBtnAction, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * this interface is used to update the UI when waypoint/task state is changed
     * we used this inside our Bringg SDK task action callbacks we implemented on this fragment
     * the resulting task/waypoint events will be fired to all registered TaskActionEventListener automatically by Bringg SDK.
     */
    public interface InteractionCallback {

        void onWaypointFragmentDataMissing(@Nullable Task task, long waypointId);

        void onWaypointDone(long wayPointId);

        void onTaskDone(long taskId);

        void onNextWaypointStarted(long nextWayPointId);
    }
}
