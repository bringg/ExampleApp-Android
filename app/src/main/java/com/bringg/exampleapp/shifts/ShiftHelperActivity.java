package com.bringg.exampleapp.shifts;

import android.os.Bundle;

import com.bringg.exampleapp.BaseActivity;
import com.bringg.exampleapp.shifts.ShiftHelper;

import static com.bringg.exampleapp.BringgProvider.EMPTY_USER;

abstract public class ShiftHelperActivity extends BaseActivity {
    private ShiftHelper mShiftHelper;
    private ShiftHelper.ShiftStateHelperListener mShiftStateHelperListener = new ShiftStateHelperListenerImpl();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShiftHelper = new ShiftHelper(this, mShiftStateHelperListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mShiftHelper.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mShiftHelper.stop();
    }

    protected void toggleShift() {
        mShiftHelper.toggleShift();
    }

    protected abstract void notifyShiftStateChanged(ShiftHelper.ShiftState state);

    protected boolean isLoggedIn() {
        return mBringgProvider.getClient().getUserId() != EMPTY_USER;
    }

    protected ShiftHelper.ShiftState getShiftState() {
        return mShiftHelper.getState();
    }

    private class ShiftStateHelperListenerImpl implements ShiftHelper.ShiftStateHelperListener {
        @Override
        public void onStateChange(ShiftHelper shiftHelper, ShiftHelper.ShiftState state, ShiftHelper.ShiftState oldState) {
            notifyShiftStateChanged(state);
        }
    }
}
