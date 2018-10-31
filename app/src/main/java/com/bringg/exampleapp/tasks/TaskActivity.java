package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.activity.ShiftStateAwareActivity;

import java.util.ArrayList;
import java.util.List;

import driver_sdk.BringgSDKClient;
import driver_sdk.content.StartTaskResult;
import driver_sdk.content.callback.StartTaskCallback;
import driver_sdk.models.Task;
import driver_sdk.models.TaskState;
import driver_sdk.models.Waypoint;
import driver_sdk.shift.StartShiftResultCallback;
import driver_sdk.tasks.TaskActionCallback;

public class TaskActivity extends ShiftStateAwareActivity implements WayPointFragment.InteractionCallback {

    private static final String EXTRA_TASK_ID = "com.bringg.exampleapp.tasks.EXTRA_TASK_ID";

    private Task mTask;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapterViewPager;
    private List<WayPointFragment> mListFragments;

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
        // task could be null if it was removed/canceled etc.
        if (mTask == null) {
            finish();
            return;
        }

        initActionBar();
        initViewPager();
    }

    private void updateCurrentFragment() {
        mListFragments.get(mViewPager.getCurrentItem()).updateViews();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void initActionBar() {
        Toolbar mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_white_24dp);
        getSupportActionBar().setTitle(mTask.getTitle());
    }

    private void initViewPager() {
        mViewPager = findViewById(R.id.viewpager);
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
            fragments.add(WayPointFragment.newInstance(waypoint.getTaskId(), waypoint.getId()));
        }
        return fragments;
    }

    private void scrollToFragmentByWayPointId(long wayPointId) {
        List<Waypoint> waypoints = new ArrayList<>(mTask.getWayPoints());
        mViewPager.setCurrentItem(waypoints.indexOf(mTask.getWayPointById(wayPointId)), true);
    }

    // ---------------------------------- WayPointFragment.InteractionCallback implementation ---------------------------------- //

    /**
     * fragment reported that it couldn't get valid task/waypoint by the id's that were sent to it
     * this would happen if the task/waypoint was canceled/removed
     *
     * @param task       the task if it was found
     * @param waypointId the waypoint id that failed to be found
     */
    @Override
    public void onWaypointFragmentDataMissing(@Nullable Task task, long waypointId) {
        finish();
    }

    /**
     * fragment reported that the waypoint is done
     * the user has left the site and is ready to proceed to the next waypoint
     *
     * @param wayPointId the completed waypoint id
     */
    @Override
    public void onWaypointDone(long wayPointId) {

    }

    @Override
    public void onTaskDone(long taskId) {
        finish();
    }

    /**
     * fragment reported that the waypoint is done and the next waypoint for this task has been automatically started by Bringg SDK
     *
     * @param nextWayPointId the automatically started waypoint id
     */
    @Override
    public void onNextWaypointStarted(long nextWayPointId) {
        scrollToFragmentByWayPointId(nextWayPointId);
    }
    // ------------------------------------------------------------------------------------------------------------------------- //


    // ---------------------------------- shift aware activity implementation ---------------------------------- //

    @Override
    protected void onShiftStateChanged(boolean isOnShift) {
        // ignore here, we don't have shift state related UI
    }

    @Override
    protected void showResponseError(@NonNull String message) {
        Snackbar.make(mViewPager, message, Snackbar.LENGTH_LONG).show();
    }
    // --------------------------------------------------------------------------------------------------------- //


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
}
