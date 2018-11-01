package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.CircleTransform;
import com.bringg.exampleapp.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.Set;

import driver_sdk.BringgSDKClient;
import driver_sdk.models.Task;
import driver_sdk.models.TaskState;
import driver_sdk.models.WayPointState;
import driver_sdk.models.Waypoint;
import driver_sdk.models.configuration.TaskActionItem;
import driver_sdk.shift.StartShiftResultCallback;

import static com.bringg.exampleapp.BringgProvider.BASE_HOST;

public abstract class WaypointFragmentBase extends Fragment {

    protected static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";
    protected static final String EXTRA_WAY_POINT_ID = "com.bringg.exampleapp.tasks.EXTRA_WAY_POINT_ID";
    private static final String TAG = "WaypointFragmentBase";
    @NonNull
    protected Task mTask;
    @NonNull
    protected Waypoint mWaypoint;
    @Nullable
    protected InteractionCallback mInteractionCallback;
    protected Button mBtnAction;
    private TextView mTvAddress;
    private TextView mTvScheduledFor;
    private TextView mTvUserName;
    private ImageView mImgProfile;
    private View mBtnContactPhone;
    private View mBtnContactMessage;

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

        mTvAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleNavigateToWaypointDestination(view.getContext());
            }
        });

        mBtnContactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleCallCustomer(view.getContext());
            }
        });
        mBtnContactMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleContactCustomerBySms(view.getContext());
            }
        });
        mBtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleWaypointAction(view.getContext());
            }
        });

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

    protected abstract void handleNavigateToWaypointDestination(@NonNull Context context);

    protected abstract void handleCallCustomer(@NonNull Context context);

    protected abstract void handleContactCustomerBySms(@NonNull Context context);

    protected abstract void handleWaypointAction(@NonNull Context context);

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

    // use Bringg SDK to start shift
    protected void startShift() {
        BringgSDKClient.getInstance().shiftActions().startShiftAndWaitForApproval(true, new StartShiftResultCallback() {
            @Override
            public void onShiftStarted() {
                updateViews();
            }

            @Override
            public void onShiftStartFailed(int responseCode) {
                onError(responseCode, "Error starting shift, responseCode=" + responseCode);
            }
        });
    }

    public void updateViews() {
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

    public void showDialogNotInShift() {
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

    public void showMandatoryActionsNotCompleted(Set<TaskActionItem> mandatoryRulesSet) {
        Context context = getContext();
        if (context == null) return;

        StringBuilder stringBuilder = new StringBuilder();
        for (TaskActionItem taskActionItem : mandatoryRulesSet) {
            stringBuilder.append('\n').append(taskActionItem.getTitle());
        }

        new AlertDialog.Builder(context).
                setTitle(R.string.incomplete_mandatory_actions).
                setMessage(stringBuilder).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void onError(int errorCode, @NonNull String message) {
        Log.e(TAG, "got error from progress callback, errorCode=" + errorCode + ", message=" + message);
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
