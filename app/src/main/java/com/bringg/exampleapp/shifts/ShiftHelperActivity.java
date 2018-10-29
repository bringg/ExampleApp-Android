package com.bringg.exampleapp.shifts;

import android.os.Bundle;

import com.bringg.exampleapp.BaseActivity;
import com.bringg.exampleapp.BringgApp;

abstract public class ShiftHelperActivity extends BaseActivity {
    private ShiftHelper mShiftHelper;
    private ShiftHelper.ShiftStateHelperListener mShiftStateHelperListener = new ShiftStateHelperListenerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShiftHelper = ((BringgApp) getApplication()).getShiftHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mShiftHelper.register(mShiftStateHelperListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mShiftHelper.unregister(mShiftStateHelperListener);
    }

    protected void toggleShift() {
        mShiftHelper.toggleShift();
    }

    protected abstract void notifyShiftStateChanged(ShiftHelper.ShiftState state);

    protected boolean isLoggedIn() {
        return mBringgProvider.getClient().loginState().isLoggedIn();
    }

    protected ShiftHelper.ShiftState getShiftState() {
        return mShiftHelper.getState();
    }

    protected void getShiftStateFromRemote() {
        mShiftHelper.getShiftStatusFromRemote();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class ShiftStateHelperListenerImpl implements ShiftHelper.ShiftStateHelperListener {
        @Override
        public void onStateChanged(ShiftHelper shiftHelper, ShiftHelper.ShiftState state, ShiftHelper.ShiftState oldState) {
            notifyShiftStateChanged(state);
        }

        @Override
        public void onError(String message) {
            toast(message);
        }
    }
}
