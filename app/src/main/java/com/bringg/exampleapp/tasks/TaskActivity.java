package com.bringg.exampleapp.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.shifts.ShiftHelperActivity;
import com.bringg.exampleapp.shifts.ShiftHelper;

import java.util.ArrayList;
import java.util.List;

import driver_sdk.models.tasks.Task;
import driver_sdk.models.tasks.Waypoint;

public class TaskActivity extends ShiftHelperActivity implements TaskListener {

    protected static final String EXTRA_TASK = "com.bringg.exampleapp.tasks.EXTRA_TASK";

    private boolean mIsStartShiftForTask;
    private Task mTask;
    private Button mBtnStartTask;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapterViewPager;
    private List<WayPointFragment> mListFragments;
    private TaskHelper mTaskHelper;

    public static Intent getIntent(Context context, Task task) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        mTask = getIntent().getParcelableExtra(EXTRA_TASK);
        findViews();
        initViews();
        mTaskHelper = new TaskHelper(this, mTask, new TaskHelperListenerImpl());
        updateView(mTaskHelper.getTaskState());
    }

    @Override
    protected void notifyShiftStateChanged(ShiftHelper.ShiftState state) {
        if (mIsStartShiftForTask && state == ShiftHelper.ShiftState.SHIFT_ON) {
            mTaskHelper.startTask();
            mIsStartShiftForTask = false;
        }
    }

    private void updateView(TaskHelper.TaskState taskState) {
        switch (taskState) {
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
        if (isInShift())
            mTaskHelper.startTask();
    }

    private boolean isInShift() {
        if (getShiftState() == ShiftHelper.ShiftState.SHIFT_OFF) {
            showDialogNotInShift();
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
                        mIsStartShiftForTask = true;
                        toggleShift();
                    }
                }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void findViews() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mBtnStartTask = findViewById(R.id.btn_way_point_action);
    }

    private void scrollToFragmentByWayPointId(long wayPointId) {
        mViewPager.setCurrentItem(mTask.getWayPoints().indexOf(mTask.getWayPointById(wayPointId)), true);
    }

    private void updateCurrentFragment() {
        mListFragments.get(mViewPager.getCurrentItem()).updateViews();
    }

    //****  TaskListener implementation  ****//

    @Override
    public void actionWayPoint(long wayPointId) {
        if (isInShift())
            mTaskHelper.actionWayPoint(wayPointId);
    }

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
            return mTask.getWayPoints().get(position).companyName;
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
