package com.bringg.exampleapp.shifts;

import android.content.Intent;
import android.os.Bundle;

import com.bringg.exampleapp.BaseActivity;

import driver_sdk.connection.services.RequestQueueService;

import static com.bringg.exampleapp.BringgProvider.EMPTY_USER;

abstract public class ShiftHelperActivity extends BaseActivity {
    private static ShiftHelper mShiftHelper;
    private ShiftHelper.ShiftStateHelperListener mShiftStateHelperListener = new ShiftStateHelperListenerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mShiftHelper == null)
            mShiftHelper = new ShiftHelper(this, mShiftStateHelperListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mShiftHelper.register();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mShiftHelper.unregister();
    }

    protected void toggleShift() {
        mShiftHelper.toggleShift();
    }

    protected abstract void notifyShiftStateChanged(ShiftHelper.ShiftState state);

    protected boolean isLoggedIn() {
        return  mBringgProvider.getClient().loginState().isLoggedIn();
    }

    protected ShiftHelper.ShiftState getShiftState() {
        return mShiftHelper.getState();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mShiftHelper.getState() != ShiftHelper.ShiftState.SHIFT_ON)
            stopService(new Intent(this, RequestQueueService.class));
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
