package com.bringg.exampleapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bringg.exampleapp.login.LoginActivity;
import com.bringg.exampleapp.utils.CircleTransform;
import com.squareup.picasso.Picasso;

import driver_sdk.models.User;

import static com.bringg.exampleapp.BringgProvider.BASE_HOME;

public class MainActivity extends BaseActivity {

    private static final int REQUEST_CODE_LOGIN = 1;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private ImageView mImgUser;
    private TextView mTvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initActionBar();
        initDrawer();

        if (!isLoggedIn()) {
            startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
        } else {
            onUserLogin();
        }
    }

    private void onUserLogin() {
        User user = mBringgProvider.getClient().getUser();
        mTvUserName.setText(user.getName());
        String img = user.getImageUrl();
        if (!TextUtils.isEmpty(img)) {
            Picasso.with(this).load(new StringBuilder(BASE_HOME).append(user.getImageUrl()).toString()).transform(new CircleTransform()).into(mImgUser);
        }

    }

    private void initDrawer() {
        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
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
        mImgUser = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.nav_img_account);
        mTvUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_tv_header);

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
        if (requestCode == REQUEST_CODE_LOGIN) {
            if (resultCode != RESULT_OK)
                finish();
            else
                onUserLogin();

        }
    }
}
