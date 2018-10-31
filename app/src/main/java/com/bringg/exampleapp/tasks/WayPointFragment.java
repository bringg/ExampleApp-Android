package com.bringg.exampleapp.tasks;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import driver_sdk.models.Waypoint;

import static com.bringg.exampleapp.BringgProvider.BASE_HOST;

public class WayPointFragment extends Fragment implements View.OnClickListener {

    private static final String EXTRA_WAY_POINT_ID = "com.bringg.exampleapp.tasks.EXTRA_WAY_POINT_ID";
    private static final int REQUEST_PERMISSION_CALL_PHONE = 1;
    private TextView mTvAddress;
    private TextView mTvScheduledFor;
    private TextView mTvUserName;
    private ImageView mImgProfile;
    private View mBtnContactPhone;
    private View mBtnContactMessage;
    private Button mBtnAction;
    private TaskListener mTaskListener;
    private long mWayPointId;

    public static WayPointFragment newInstance(long wayPointId) {

        Bundle args = new Bundle();
        WayPointFragment fragment = new WayPointFragment();
        args.putLong(EXTRA_WAY_POINT_ID, wayPointId);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mWayPointId = getArguments().getLong(EXTRA_WAY_POINT_ID);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TaskListener)
            mTaskListener = (TaskListener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mTaskListener = null;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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

        Waypoint waypoint = getWayPoint();
        if (waypoint != null) {
            mTvAddress.setText(waypoint.getAddress());
            mTvScheduledFor.setText(Utils.isoToStringDate(waypoint.getScheduledAt()));
            mTvUserName.setText(waypoint.getCustomer().name);
            String imgUrl = waypoint.getCustomer().imageUrl;
            if (TextUtils.isEmpty(imgUrl))
                return;
            if (!imgUrl.contains("http"))
                imgUrl = BASE_HOST + imgUrl;
            Picasso.get().load(imgUrl).transform(new CircleTransform()).into(mImgProfile);
        }
        updateViews();
    }

    protected void updateViews() {
        if (!isAdded())
            return;
        TaskHelper.TaskState taskState = getTaskState();
        if (taskState == null)
            return;
        updateViewsByTaskState(taskState);

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
                action();
                break;
        }
    }

    private void updateViewsByTaskState(TaskHelper.TaskState taskState) {
        switch (taskState) {

            case NOT_STARTED:
                mBtnAction.setVisibility(View.GONE);
                break;
            case STARTED:
                TaskHelper.WayPointState wayPointState = getWayPointState();
                if (wayPointState == null)
                    return;
                updateViewsByWayPoinyState(wayPointState);
                break;
            case DONE:
                break;
        }
    }

    private void updateViewsByWayPoinyState(TaskHelper.WayPointState wayPointState) {
        switch (wayPointState) {
            case NO_STARTED:
                mBtnAction.setVisibility(View.GONE);
                break;
            case STARTED:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.arrived);
                mBtnAction.setEnabled(true);
                break;
            case ARRIVE:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.leave);
                mBtnAction.setEnabled(true);
                break;
            case LEAVE:
                mBtnAction.setVisibility(View.VISIBLE);
                mBtnAction.setText(R.string.completed);
                mBtnAction.setEnabled(false);
                break;
        }
    }

    private TaskHelper.WayPointState getWayPointState() {
        if (mTaskListener != null)
            return mTaskListener.getWayPointState(mWayPointId);
        return null;
    }

    private TaskHelper.TaskState getTaskState() {
        if (mTaskListener != null)
            return mTaskListener.getTaskState();
        return null;
    }

    private void action() {
        if (mTaskListener != null)
            mTaskListener.actionWayPoint(mWayPointId);
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
        if (getWayPoint() == null)
            return;
        String number = "tel:" + getWayPoint().getPhone();
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
        startActivity(callIntent);
    }

    private void contactMessage() {
        if (getWayPoint() == null)
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setType("vnd.android-dir/mms-sms");
        intent.putExtra("address", getWayPoint().getPhone());
        intent.putExtra("sms_body", "message");

        startActivity(intent);
    }

    private void navigate() {
        if (getWayPoint() == null)
            return;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(new StringBuilder("geo:").append(getWayPoint().getLat()).append(",").append(getWayPoint().getLng()).toString()));
        startActivity(intent);
    }

    private Waypoint getWayPoint() {
        if (mTaskListener == null)
            return null;
        return mTaskListener.getWayPointById(mWayPointId);
    }
}
