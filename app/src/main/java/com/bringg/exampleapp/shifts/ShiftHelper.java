package com.bringg.exampleapp.shifts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bringg.exampleapp.BringgApp;

import driver_sdk.connection.services.RequestQueueService;
import driver_sdk.shift.Shift;

public class ShiftHelper {


    private boolean mIsStart;

    public enum ShiftState {
        SHIFT_ON,
        SHIFT_OFF,
        LOADING
    }

    private final Context mContext;
    private final ShiftStateHelperListener mListener;
    private ShiftState mState;
    private ShiftManager mShiftManager;
    private BroadcastReceiver mReceiver;

    public ShiftHelper(Context context, ShiftStateHelperListener listener) {
        mContext = context;
        mListener = listener;
        mReceiver = new BroadcastReceiverShiftChangeImpl();
        if (mContext.getApplicationContext() instanceof BringgApp) {
            mShiftManager = ((BringgApp) mContext.getApplicationContext()).getShiftManager();
        }
    }

    public void toggleShift() {
        if (mShiftManager.getShift() == null)
            return;
        if (mShiftManager.getShift().on) {
            mShiftManager.updateShiftState(false);
            setState(ShiftState.LOADING);
        } else {
            mShiftManager.updateShiftState(true);
            setState(ShiftState.LOADING);
        }
    }


    public ShiftState getState() {
        return mState;
    }

    public void start() {
        if (!mIsStart) {
            mIsStart = true;
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, ShiftManager.getIntentFilterShiftChanged());
            updateState();
        }
    }

    public void stop() {
        if (mIsStart) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
            mIsStart = false;
        }

    }

    private void updateState() {
        Shift shift = mShiftManager.getShift();
        if (shift == null)
            return;
        if (shift.on)
            setState(ShiftState.SHIFT_ON);
        else
            setState(ShiftState.SHIFT_OFF);
    }

    private void setState(ShiftState state) {
        if (mState == state)
            return;
        ShiftState oldState = mState;
        mState = state;
        if (mState == ShiftState.SHIFT_ON)
            startShiftService();
        else if (mState == ShiftState.SHIFT_OFF) {
            stopShiftService();
            stopQueueService();
        }
        if (mListener != null)
            mListener.onStateChange(this, mState, oldState);
    }

    private void stopQueueService() {
        mContext.stopService(new Intent(mContext, RequestQueueService.class));
    }

    private void startShiftService() {
        mContext.startService(new Intent(mContext, ShiftService.class));
    }

    private void stopShiftService() {
        mContext.stopService(new Intent(mContext, ShiftService.class));

    }

    public interface ShiftStateHelperListener {
        void onStateChange(ShiftHelper shiftHelper, ShiftState state, ShiftState oldState);
    }

    private class BroadcastReceiverShiftChangeImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    }


}
