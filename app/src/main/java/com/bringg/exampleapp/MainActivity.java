package com.bringg.exampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.Collection;
import java.util.List;

import driver_sdk.models.CancellationReason;
import driver_sdk.models.Task;
import driver_sdk.models.User;
import driver_sdk.tasks.GetTasksResultCallback;
import driver_sdk.tasks.OpenTasksResult;
import driver_sdk.tasks.RefreshTasksResultCallback;

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
    private TasksResultCallbackImpl mTasksResultCallback;

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
        }
    }

    private void initSwipeRefresItem() {
        mTasksResultCallback = new TasksResultCallbackImpl();
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    private void refreshItems() {
        mSwipeRefreshLayout.setRefreshing(true);
        showLoadingProgress();
        mTvListEmpty.setVisibility(View.GONE);
        mTasks.clear();
        //mTasksAdapter.notifyDataSetChanged();
        mBringgProvider.getClient().taskActions().refreshTasks(mTasksResultCallback);

    }

    private void onUserLogin() {
        getUser();
        getShiftStateFromRemote();
        refreshItems();
    }

    private void getUser() {
        User user = mBringgProvider.getClient().user().getCurrentUser();
        if (user == null)
            return;
        mTvUserName.setText(user.getName());
        String img = user.getImageUrl();
        if (!TextUtils.isEmpty(img)) {
            if (!img.contains("http"))
                img = BASE_HOST + img;
            Picasso.get().load(img).transform(new CircleTransform()).into(mImgUser);
        }
    }

    @Override
    public void onTasksLoaded(@NonNull Collection<driver_sdk.models.Task> tasks) {
        super.onTasksLoaded(tasks);
        mTasksResultCallback.onTaskResult(new ArrayList<>(tasks));
    }

    @Override
    public void onTasksUpdated(@NonNull Collection<driver_sdk.models.Task> collection) {
        super.onTasksUpdated(collection);
        mTasksResultCallback.onTaskResult(new ArrayList<>(collection));
    }

    @Override
    public void onTaskRemoved(long taskId) {
        super.onTaskRemoved(taskId);
        mTasks.remove(getLocalTaskById(taskId));
        notifyDataSetChanged();
        if (mTasks.isEmpty())
            mTvListEmpty.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTaskCanceled(long taskId, @NonNull String s, @NonNull CancellationReason cancellationReason) {
        super.onTaskCanceled(taskId, s, cancellationReason);
        toast("Task canceled " + s + " " + cancellationReason.getReason());
        onTaskRemoved(taskId);
    }

    private driver_sdk.models.Task getLocalTaskById(long taskId) {
        for (Task task : mTasks) {
            if (taskId == task.getId())
                return task;
        }
        return null;
    }

    @Override
    public void onTaskAdded(@NonNull Task task) {
        if (mTvListEmpty.getVisibility() == View.VISIBLE)
            mTvListEmpty.setVisibility(View.GONE);
        super.onTaskAdded(task);
        mTasks.add(task);
        mTasksAdapter.notifyItemRangeInserted(mTasks.size() - 1, 1);
    }

    private void notifyDataSetChanged() {
        mTasksAdapter.notifyDataSetChanged();
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
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
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
        mImgUser = mNavigationView.getHeaderView(0).findViewById(R.id.nav_img_account);
        mTvUserName = mNavigationView.getHeaderView(0).findViewById(R.id.nav_tv_header);

    }

    private void logOut() {
        mBringgProvider.getClient().loginActions().logout();
        startLoginActivityForResult();
    }

    private void initActionBar() {
        mToolBar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        findViewById(R.id.btn_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* LoggerDebug.get().clear();
                Intent intent = new Intent(MainActivity.this, RequestQueueService.class);
                intent.setAction(ACTION_DELETE_DB);
                startService(intent);*/
            }
        });
        findViewById(R.id.btn_log).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.yossibarel.logger");
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }
            }
        });


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
                notifyDataSetChanged();
                break;
        }
    }


    private class TasksAdapterListenerImpl implements TasksAdapter.TasksAdapterListener {

        @Override
        public void onItemSelected(Task task) {
            startActivityForResult(TaskActivity.getIntent(MainActivity.this, task.getId()), REQUEST_CODE_TASK_ACTIVITY);
        }
    }

    private class TasksResultCallbackImpl implements GetTasksResultCallback, RefreshTasksResultCallback {

        void onTaskResult(List<Task> tasks) {
            if (isDestroyed())
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
            mTasks.clear();
            mTasksAdapter.notifyDataSetChanged();
            mTasks.addAll(tasks);
            mTasksAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTasksResult(@NonNull List<Task> tasks, long lastTimeUpdated) {
            onTaskResult(tasks);
        }

        @Override
        public void onRefreshTasksFailure(int error) {
            if (isFinishing())
                return;
            hideLoadingProgress();
            toast("error");
        }

        @Override
        public void onRefreshTasksSuccess(@NonNull OpenTasksResult openTasksResult) {
            onTaskResult(openTasksResult.getTasks());
        }
    }
}
