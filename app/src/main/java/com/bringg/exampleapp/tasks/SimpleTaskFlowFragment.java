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

import driver_sdk.BringgSDKClient;
import driver_sdk.content.WaypointActionUtil;
import driver_sdk.models.Task;
import driver_sdk.models.TaskState;
import driver_sdk.models.WayPointState;
import driver_sdk.models.Waypoint;
import driver_sdk.models.tasks.flow.ProceedTaskCallback;
import driver_sdk.models.tasks.flow.TaskFlow;
import driver_sdk.shift.StartShiftResultCallback;

import static com.bringg.exampleapp.BringgProvider.BASE_HOST;

public class SimpleTaskFlowFragment extends Fragment implements ProceedTaskCallback {

    private static final String TAG = "SimpleTaskFlowFragment";
    private static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";
    private static final String EXTRA_WAY_POINT_ID = "com.bringg.exampleapp.tasks.EXTRA_WAY_POINT_ID";
    @NonNull
    protected Task mTask;
    @NonNull
    protected Waypoint mWaypoint;
    @Nullable
    protected InteractionCallback mInteractionCallback;
    private TextView mTvAddress;
    private TextView mTvScheduledFor;
    private TextView mTvUserName;
    private ImageView mImgProfile;
    private View mBtnContactPhone;
    private View mBtnContactMessage;
    private Button mBtnAction;
    private TaskFlow mSimpleTaskFlow;

    public static SimpleTaskFlowFragment newInstance(long taskId, long wayPointId) {
        Bundle args = new Bundle();
        SimpleTaskFlowFragment fragment = new SimpleTaskFlowFragment();
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
        mSimpleTaskFlow = BringgSDKClient.getInstance().taskActions().getSimpleTaskFlow();
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
                WaypointActionUtil.navigate(view.getContext(), mWaypoint);
            }
        });

        mBtnContactPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaypointActionUtil.makeCall(view.getContext(), mWaypoint);
            }
        });
        mBtnContactMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WaypointActionUtil.contactMessage(view.getContext(), mWaypoint);
            }
        });
        mBtnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSimpleTaskFlow.proceedToNextTaskStep(SimpleTaskFlowFragment.this, mTask, mWaypoint.getId());
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
    private void startShift() {
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

    // -------------------------------------- presenter implementation -------------------------------------/
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
    public void onError(int errorCode, String message) {
        Log.e(TAG, "got error from progress callback, errorCode=" + errorCode + ", message=" + message);
        Snackbar.make(mBtnAction, message, Snackbar.LENGTH_LONG).show();
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
    // ---------------------------------------------------------------------------------------------------------------/


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
