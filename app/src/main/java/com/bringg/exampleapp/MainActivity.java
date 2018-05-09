package com.bringg.exampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bringg.exampleapp.adapters.TasksAdapter;
import com.bringg.exampleapp.login.LoginActivity;
import com.bringg.exampleapp.shifts.ShiftHelper;
import com.bringg.exampleapp.shifts.ShiftHelperActivity;
import com.bringg.exampleapp.tasks.TaskActivity;
import com.bringg.exampleapp.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import driver_sdk.models.User;
import driver_sdk.models.tasks.Task;
import driver_sdk.tasks.GetTasksResultCallback;

import static com.bringg.exampleapp.BringgProvider.BASE_HOST;

public class MainActivity extends ShiftHelperActivity {

    private static final int REQUEST_CODE_LOGIN_ACTIVITY = 1;
    private static final int REQUEST_CODE_TASK_ACTIVITY = 2;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private ImageView mImgUser;
    private TextView mTvUserName;
    private NavigationView mNavigationView;
    private List<Task> mTasks;
    private RecyclerView mRecycleView;
    private TasksAdapter mTasksAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private View mTvListEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initActionBar();
        initDrawer();
        initRecycleView();
        initSwipeRefresItem();
        if (!isLoggedIn()) {
            startLoginActivityForResult();
        } else {
            onUserLogin();
        }
    }

    private void initViews() {
        mTvListEmpty = findViewById(R.id.tv_empty_list_task);
        mTvListEmpty.setVisibility(View.GONE);
    }

    private void startLoginActivityForResult() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN_ACTIVITY);
    }

    @Override
    protected void notifyShiftStateChanged(ShiftHelper.ShiftState state) {
        switch (state) {

            case SHIFT_ON:
                hideLoadingProgress();
                mNavigationView.getMenu().findItem(R.id.nav_end_shift).setVisible(true);
                mNavigationView.getMenu().findItem(R.id.nav_start_shift).setVisible(false);
                break;
            case SHIFT_OFF:
                hideLoadingProgress();
                mNavigationView.getMenu().findItem(R.id.nav_end_shift).setVisible(false);
                mNavigationView.getMenu().findItem(R.id.nav_start_shift).setVisible(true);
                break;
            case LOADING:
                showLoadingProgress();
                break;
        }
    }

    private void initSwipeRefresItem() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    private void refreshItems() {
        showLoadingProgress();
        mTvListEmpty.setVisibility(View.GONE);

        mTasks.clear();
        mBringgProvider.getClient().getTasks(new TasksResultCallbackImpl());

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void onUserLogin() {
        User user = mBringgProvider.getClient().getUser();
        mTvUserName.setText(user.getName());
        String img = user.getImageUrl();
        if (!TextUtils.isEmpty(img)) {
            if (!img.contains("http"))
                img = new StringBuilder(BASE_HOST).append(img).toString();
            Picasso.with(this).load(img).transform(new CircleTransform()).into(mImgUser);
        }
        refreshItems();

    }

    private void initRecycleView() {
        mRecycleView = findViewById(R.id.recycle_view);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(lm);
        mTasks = new ArrayList<>();
        mTasksAdapter = new TasksAdapter(mTasks, new TasksAdapterListenerImpl());
        mRecycleView.setAdapter(mTasksAdapter);
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_end_shift:
                    case R.id.nav_start_shift:
                        toggleShift();
                        break;
                    case R.id.nav_change_account:
                        logOut();
                        break;
                }
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mImgUser = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_img_account);
        mTvUserName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.nav_tv_header);

    }

    private void logOut() {
        mBringgProvider.getClient().logout();
        startLoginActivityForResult();

    }


    private void initActionBar() {
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOGIN_ACTIVITY:
                if (resultCode != RESULT_OK)
                    finish();
                else
                    onUserLogin();
                break;
            case REQUEST_CODE_TASK_ACTIVITY:
                refreshItems();
                break;
        }

    }


    private class TasksAdapterListenerImpl implements TasksAdapter.TasksAdapterListener {

        @Override
        public void onItemSelected(Task task) {

            startActivityForResult(TaskActivity.getIntent(MainActivity.this, task), REQUEST_CODE_TASK_ACTIVITY);
        }
    }

    private class TasksResultCallbackImpl implements GetTasksResultCallback {

        @Override
        public void onTasksResult(List<Task> tasks, long lastTimeUpdated) {
            if (isFinishing())
                return;
            hideLoadingProgress();
            if (tasks.isEmpty()) {
                mRecycleView.setVisibility(View.GONE);
                mTvListEmpty.setVisibility(View.VISIBLE);
            } else {
                mTvListEmpty.setVisibility(View.GONE);
                mRecycleView.setVisibility(View.VISIBLE);
            }
            mSwipeRefreshLayout.setRefreshing(false);
            mTasks.addAll(tasks);
            mTasksAdapter.notifyDataSetChanged();
        }
    }

}
