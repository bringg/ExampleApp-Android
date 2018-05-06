package com.bringg.exampleapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import static com.bringg.exampleapp.BringgProvider.EMPTY_USER;

public abstract class BaseActivity extends AppCompatActivity {
    protected BringgProvider mBringgProvider;
    private AlertDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getApplication() instanceof BringgApp) {
            mBringgProvider = ((BringgApp) getApplication()).getBringg();
        }
    }

    protected boolean isLoggedIn() {
        return mBringgProvider.getClient().getUserId() != EMPTY_USER;
    }

    protected void showLoadingProgress() {
        hideLoadingProgress();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BaseActivity.this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setCancelable(false);
        mLoadingDialog = dialogBuilder.create();
        mLoadingDialog.show();
    }

    protected void hideLoadingProgress() {
        if (mLoadingDialog == null)
            return;
        mLoadingDialog.dismiss();
        mLoadingDialog = null;
    }
}
