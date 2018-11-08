package com.bringg.exampleapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.adapters.TasksAdapter;
import com.bringg.exampleapp.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import driver_sdk.BringgSDKClient;
import driver_sdk.models.CancellationReason;
import driver_sdk.models.Task;
import driver_sdk.models.User;
import driver_sdk.models.Waypoint;
import driver_sdk.models.tasks.PendingTasksData;
import driver_sdk.tasks.GetTasksResultCallback;
import driver_sdk.tasks.OpenTasksResult;
import driver_sdk.tasks.RefreshTasksResultCallback;
import driver_sdk.tasks.TaskEventListener;

public class TaskListActivity extends ShiftStateAwareActivity implements TaskEventListener {

    public static final String BASE_HOST = "https://app.bringg.com";
    private static final int REQUEST_CODE_LOGIN_ACTIVITY = 1;
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

        initActionBar();
        initDrawer();
        initRecycleView();
        initSwipeRefreshItem();

        // we may start when already logged in if the user already logged in on a previous session
        // when not logged in we will start LoginActivity, otherwise we can proceed showing the task list
        if (!isLoggedIn()) {
            startLoginActivityForResult();
        } else {
            onUserLoginStateChanged();
        }

        // we may start when the user is already on shift or off shift
        // update the UI with current shift state
        onShiftStateChanged(isOnShift());
    }

    private void startLoginActivityForResult() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN_ACTIVITY);
    }

    /**
     * update the UI with current user details
     */
    private void onUserLoginStateChanged() {

        // get the user details from the sdk
        User user = BringgSDKClient.getInstance().user().getCurrentUser();

        // user will be null when we are not logged in
        if (user == null) {
            // we need to clear current UI from a previous logged in user data that we might had
            mTvUserName.setText(null);
            mImgUser.setImageDrawable(null);
            setTaskListLocally(null);
            return;
        }

        // update the UI with current user data
        mTvUserName.setText(user.getName());
        String img = user.getImageUrl();
        if (!TextUtils.isEmpty(img)) {
            if (!img.contains("http"))
                img = BASE_HOST + img;
            Picasso.get().load(img).transform(new CircleTransform()).into(mImgUser);
        }

        // update the UI with current user task list
        updateTaskList();
        onShiftStateChanged(BringgSDKClient.getInstance().shiftState().isOnShift());
    }

    void updateTaskList() {
        if (isDestroyed())
            return;

        // this gets the current task list
        // the sdk will return a cached version if exists and up-to-date and will fetch the list from remote otherwise
        // to explicitly refresh the list from the server use BringgSDKClient.getInstance().taskActions().refreshTasks()
        BringgSDKClient.getInstance().taskActions().getTasks(new GetTasksResultCallback() {
            @Override
            public void onTasksResult(@NonNull List<Task> tasks, long lastTimeUpdated) {
                setTaskListLocally(tasks);
            }
        });
    }

    private void setTaskListLocally(@Nullable List<Task> tasks) {
        hideLoadingProgress();
        mSwipeRefreshLayout.setRefreshing(false);

        mTasks.clear();
        if (tasks != null) {
            mTasks.addAll(tasks);
        }

        if (mTasks.isEmpty()) {
            mRecycleView.setVisibility(View.GONE);
            mTvListEmpty.setVisibility(View.VISIBLE);
        } else {
            mTvListEmpty.setVisibility(View.GONE);
            mRecycleView.setVisibility(View.VISIBLE);
        }

        mTasksAdapter.notifyDataSetChanged();
    }


    /**
     * current user log out, immediately start the LoginActivity to enable user switching
     */
    private void logOut() {
        // user pressed logout - update the UI
        onUserLoginStateChanged();
        // call sdk to logout current user
        BringgSDKClient.getInstance().loginActions().logout();
        // auto-start LoginActivity for a new login
        startLoginActivityForResult();
    }

    /**
     * called from ShiftAwareActivity when shift state changes
     * here we update our UI state accordingly
     *
     * @param isOnShift the updated current shift state
     */
    @Override
    protected void onShiftStateChanged(boolean isOnShift) {
        boolean isLoggedIn = BringgSDKClient.getInstance().loginState().isLoggedIn();
        mNavigationView.getMenu().findItem(R.id.nav_end_shift).setVisible(isLoggedIn && isOnShift);
        mNavigationView.getMenu().findItem(R.id.nav_start_shift).setVisible(isLoggedIn && !isOnShift);
        if (isOnShift) {
            BringgSDKClient.getInstance().taskEvents().registerTaskEventListener(this);
        } else {
            BringgSDKClient.getInstance().taskEvents().unRegisterTaskEventListener(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        BringgSDKClient.getInstance().taskEvents().unRegisterTaskEventListener(this);
    }

    /**
     * called from:
     * 1. ShiftAwareActivity when shift state change request generated an error.
     * 2. Refresh task list by swipe to refresh generated an error.
     * we display a snackbar with the message just for debugging purposes here,
     * implementations should probably notify the user and give UI to retry or solve the issue (check connection state, etc.)
     *
     * @param message error message text
     */
    @Override
    protected void showResponseError(@NonNull String message) {
        Snackbar.make(mTvListEmpty, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * here we handle the result from LoginActivity
     * 1. we update the UI anyway (delete previous user details from the UI if we had one)
     * 2. if we are not logged in yet (user canceled, etc.) we show a retry UI
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_LOGIN_ACTIVITY:
                onUserLoginStateChanged();
                if (!BringgSDKClient.getInstance().loginState().isLoggedIn()) {
                    Snackbar.make(mTvListEmpty, "Login failed", Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startLoginActivityForResult();
                        }
                    }).show();
                }
                break;
        }
    }

    private void initSwipeRefreshItem() {
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);

                // this call explicitly fetch the tasks from the server and refreshes the local task list
                // we can listen to the specific call result using the callback
                // we can ignore the callback results and use global TaskEventListener implementation
                BringgSDKClient.getInstance().taskActions().refreshTasks(new RefreshTasksResultCallback() {

                    @Override
                    public void onRefreshTasksSuccess(@NonNull OpenTasksResult openTasksResult) {

                        // get the task list from the result
                        List<Task> tasks = openTasksResult.getTasks();
                        // tasks may be null - we may only get pending tasks data
                        int taskListSize = tasks == null ? 0 : tasks.size();

                        // we may also get the different pending task counts from the result
                        /*
                        PendingTasksData pendingTasksData = openTasksResult.getPendingTasksData();
                        if (pendingTasksData != null) {
                            int beingPrepared = pendingTasksData.getBeingPrepared();
                            int driversInQueue = pendingTasksData.getDriversInQueue();
                            int ready = pendingTasksData.getReady();
                        }
                        */
                        Log.i(TAG, "task list refreshed from remote, new tasks count=" + taskListSize);

                        setTaskListLocally(tasks);
                    }

                    @Override
                    public void onRefreshTasksFailure(int error) {
                        Log.e(TAG, "task list refresh failed, errorCode=" + error);
                        showResponseError("task list refresh failed");
                    }
                });
            }
        });
    }

    private void initActionBar() {
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_end_shift:
                        endShift();
                        break;
                    case R.id.nav_start_shift:
                        startShift();
                        break;
                    case R.id.nav_change_account:
                        logOut();
                        break;
                }
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolBar, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(drawerToggle);
        mImgUser = mNavigationView.getHeaderView(0).findViewById(R.id.nav_img_account);
        mTvUserName = mNavigationView.getHeaderView(0).findViewById(R.id.nav_tv_header);
    }

    private void initRecycleView() {

        mTvListEmpty = findViewById(R.id.tv_empty_list_task);
        mTvListEmpty.setVisibility(View.GONE);

        mRecycleView = findViewById(R.id.recycle_view);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecycleView.setLayoutManager(lm);
        mTasks = new ArrayList<>();
        mTasksAdapter = new TasksAdapter(mTasks, new TasksAdapterListenerImpl());
        mRecycleView.setAdapter(mTasksAdapter);
    }

    // ---------------------------------- TaskEventListener implementation ---------------------------------- //
    // this interface is called from Bringg SDK when tasks are updated/added/removed from the current task list
    // the events may be a result of user actions or remote updates
    // we use a naive implementation here for demo purposes that will just refresh the whole task list
    // real implementation should use the proper recyclerView adapter methods (remove, update, etc.)
    @Override
    public void onTaskAdded(@NonNull Task newTask) {
        Log.d(TAG, "onTaskAdded");
        updateTaskList();
    }

    @Override
    public void onTaskUpdated(@NonNull Task task) {
        Log.d(TAG, "onTaskUpdated");
        updateTaskList();
    }

    @Override
    public void onTaskRemoved(long taskId) {
        Log.d(TAG, "onTaskRemoved");
        updateTaskList();
    }

    @Override
    public void onTaskCanceled(long id, @Nullable String externalId, @Nullable CancellationReason cancellationReason) {
        Log.d(TAG, "onTaskCanceled");
        updateTaskList();
    }

    @Override
    public void onTasksAdded(@NonNull Collection<Task> tasks) {
        Log.d(TAG, "onTasksAdded");
        updateTaskList();
    }

    @Override
    public void onTasksUpdated(@NonNull Collection<Task> tasks) {
        Log.d(TAG, "onTasksUpdated");
        updateTaskList();
    }

    @Override
    public void onTasksRemoved(@NonNull Collection<Long> removedTaskIds) {
        Log.d(TAG, "onTasksRemoved");
        updateTaskList();
    }

    @Override
    public void onPendingTaskDataUpdated(@NonNull PendingTasksData pendingTasksData) {
        Log.d(TAG, "onPendingTaskDataUpdated");
    }

    @Override
    public void onGrabTaskAdded(@NonNull Task task) {
        Log.d(TAG, "onGrabTaskAdded");
    }

    @Override
    public void onFutureTaskListUpdated(@NonNull List<Task> futureTasks) {
        Log.d(TAG, "onFutureTaskListUpdated");
    }

    @Override
    public void onMassTasksRemove(@NonNull List<Task> removedTasks, @Nullable CancellationReason cancellationReason) {
        Log.d(TAG, "onMassTasksRemove");
        updateTaskList();
    }

    @Override
    public void onWayPointAdded(@NonNull Task task, @NonNull Waypoint waypoint) {
        Log.d(TAG, "onWayPointAdded");
        updateTaskList();
    }

    private class TasksAdapterListenerImpl implements TasksAdapter.TasksAdapterListener {
        @Override
        public void onItemSelected(long taskId) {
            startActivity(TaskActivity.getIntent(TaskListActivity.this, taskId));
        }
    }
}