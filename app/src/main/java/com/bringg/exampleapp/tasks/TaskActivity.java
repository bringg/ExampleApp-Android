package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.activity.ShiftStateAwareActivity;

import java.util.ArrayList;
import java.util.List;

import driver_sdk.BringgSDKClient;
import driver_sdk.models.Task;
import driver_sdk.models.Waypoint;

public class TaskActivity extends ShiftStateAwareActivity implements TaskListener {

    private static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";

    private Task mTask;
    private Button mBtnStartTask;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapterViewPager;
    private List<WayPointFragment> mListFragments;
    private TaskHelper mTaskHelper;
    private Toolbar mToolBar;

    public static Intent getIntent(Context context, long taskId) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        long taskId = getIntent().getLongExtra(EXTRA_TASK_ID, 0);
        if (taskId == 0)
            finish();
        mTask = BringgSDKClient.getInstance().taskActions().getTaskById(taskId);
        initActionBar();
        findViews();
        initViews();
        mTaskHelper = new TaskHelper(this, mTask, new TaskHelperListenerImpl());
        updateView(mTaskHelper.getTaskState());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initActionBar() {
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_white_24dp);
        getSupportActionBar().setTitle(mTask.getTitle());
    }

    private void updateView(TaskHelper.TaskState taskState) {
        switch (taskState) {
            case ACCEPTED:
            case NOT_STARTED:
                mBtnStartTask.setVisibility(View.VISIBLE);
                break;
            case STARTED:
                mBtnStartTask.setVisibility(View.GONE);
                break;
            case DONE:
                finish();
                return;
        }
        updateCurrentFragment();
    }

    private void initViews() {
        initViewPager();
        mBtnStartTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTask();
            }
        });
    }

    private void startTask() {
        if (!isOnShift()) {
            showDialogNotInShift();
            return;
        }
        if (isTaskAccepted())
            mTaskHelper.startTask();
    }

    private boolean isTaskAccepted() {
        if (!mTask.isAccepted()) {
            showDialogAcceptTask();
            return false;
        }
        return true;
    }

    private void initViewPager() {
        mAdapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), mListFragments = createFragments());
        mViewPager.setAdapter(mAdapterViewPager);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mAdapterViewPager.getItem(position).updateViews();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        for (Waypoint waypoint : mTask.getWayPoints()) {
            if (!waypoint.isDone()) {
                scrollToFragmentByWayPointId(waypoint.getId());
                break;
            }
        }
    }

    private List<WayPointFragment> createFragments() {
        List<WayPointFragment> fragments = new ArrayList<>();
        for (Waypoint waypoint : mTask.getWayPoints()) {
            fragments.add(WayPointFragment.newInstance(waypoint.getId()));
        }
        return fragments;
    }

    private void showDialogNotInShift() {
        new AlertDialog.Builder(this).
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

    private void showDialogAcceptTask() {
        new AlertDialog.Builder(this).
                setTitle(R.string.task_not_accept_title).
                setMessage(R.string.task_not_accept_message).
                setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTaskHelper.acceptTask();
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.no_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void findViews() {
        mViewPager = findViewById(R.id.viewpager);
        mBtnStartTask = findViewById(R.id.btn_way_point_action);
    }

    private void scrollToFragmentByWayPointId(long wayPointId) {
        List<Waypoint> waypoints = new ArrayList<>(mTask.getWayPoints());
        mViewPager.setCurrentItem(waypoints.indexOf(mTask.getWayPointById(wayPointId)), true);
    }

    private void updateCurrentFragment() {
        mListFragments.get(mViewPager.getCurrentItem()).updateViews();
    }

    //****  TaskListener implementation  ****//

    @Override
    public void actionWayPoint(long wayPointId) {
        if (!isOnShift()) {
            showDialogNotInShift();
            return;
        }
        mTaskHelper.actionWayPoint(wayPointId);
    }

//    @Override
//    public void onTaskRemoved(long l) {
//        super.onTaskRemoved(l);
//        if (l == getTaskId())
//            onBackPressed();
//    }

    @Override
    public TaskHelper.TaskState getTaskState() {
        return mTaskHelper.getTaskState();
    }

    @Override
    public TaskHelper.WayPointState getWayPointState(long wayPointId) {
        return mTaskHelper.getWayPointState(wayPointId);
    }

    @Override
    public Waypoint getWayPointById(long wayPointId) {
        if (mTask == null)
            return null;
        return mTask.getWayPointById(wayPointId);
    }

    @Override
    protected void onShiftStateChanged(boolean isOnShift) {

    }

    @Override
    protected void showResponseError(@NonNull String message) {
        Snackbar.make(mViewPager, message, Snackbar.LENGTH_LONG).show();
    }

    //****************************************//
    private class MyPagerAdapter extends FragmentPagerAdapter {

        private final List<WayPointFragment> mFragmentsItems;

        public MyPagerAdapter(FragmentManager fragmentManager, List<WayPointFragment> fragments) {
            super(fragmentManager);
            mFragmentsItems = fragments;
        }

        @Override
        public int getCount() {
            return mTask.getWayPoints().size();
        }

        @Override
        public WayPointFragment getItem(int position) {
            return mFragmentsItems.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return ((Waypoint) mTask.getWayPoints().toArray()[position]).getCompanyName();
        }
    }

    private class TaskHelperListenerImpl implements TaskHelper.TaskStateHelperListener {
        @Override
        public void onTaskStateChanged(long taskId, TaskHelper.TaskState state) {
            if (taskId != mTask.getId())
                return;
            if (isFinishing())
                return;
            updateView(state);
        }

        @Override
        public void onWayPointStateChanged(long wayPointId, TaskHelper.WayPointState state) {
            if (isFinishing())
                return;
            if (state == TaskHelper.WayPointState.STARTED) {
                scrollToFragmentByWayPointId(wayPointId);
            }
            updateCurrentFragment();

        }

        @Override
        public void onError(String error) {
            Toast.makeText(TaskActivity.this, error, Toast.LENGTH_SHORT).show();
        }
    }
}
