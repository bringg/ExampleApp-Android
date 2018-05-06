package com.bringg.exampleapp;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import driver_sdk.LeanBringgSDKClient;
import driver_sdk.shift.EndShiftCallback;
import driver_sdk.shift.GetShiftResultCallback;
import driver_sdk.shift.Shift;
import driver_sdk.shift.StartShiftResultCallback;

import static com.bringg.exampleapp.BringgProvider.EMPTY_USER;

public class ShiftManager {

    private final LeanBringgSDKClient mClient;
    public static final String ACTION_SHIFT_CHANGE = "com.bringg.exampleapp.ACTION_SHIFT_CHANGE";
    public static final String EXTRA_IN_SHIFT = "com.bringg.exampleapp.EXTRA_IN_SHIFT";
    public static final String EXTRA_SHIFT_ERROR = "com.bringg.exampleapp.EXTRA_SHIFT_ERROR";
    public static final String EXTRA_SHIFT_ERROR_MESSAGE = "com.bringg.exampleapp.EXTRA_SHIFT_ERROR_MESSAGE";


    private ShiftResultCallbackImpl mShiftResultCallback;
    private Context mContex;


    public static IntentFilter getIntentFilterShiftChanged() {
        return new IntentFilter(ACTION_SHIFT_CHANGE);
    }

    public ShiftManager(Context context, BringgProvider bringgProvider) {
        mContex = context;
        mClient = bringgProvider.getClient();
        mShiftResultCallback = new ShiftResultCallbackImpl();
        if (mClient.getUserId() != EMPTY_USER)
            load();

    }

    public Shift getShift() {
        return mClient.getShift();
    }

    public void load() {
        mClient.getShiftStatusFromRemote(mShiftResultCallback);
    }

    public void updateShiftState(boolean inShift) {
        if (inShift)
            mClient.startShiftAndWaitForApproval(true, mShiftResultCallback);
        else
            mClient.endShift(mShiftResultCallback);
    }

    private void notifyShiftUpdate(Shift shift) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHIFT_CHANGE);
        intent.putExtra(EXTRA_IN_SHIFT, shift.on);
        LocalBroadcastManager.getInstance(mContex).sendBroadcast(intent);

    }

    private class ShiftResultCallbackImpl implements GetShiftResultCallback, StartShiftResultCallback, EndShiftCallback {

        @Override
        public void onGetShiftStatusResult(@NotNull Shift shift) {
            notifyShiftUpdate(shift);

        }

        @Override
        public void onGetShiftStatusFailed(@Nullable Shift lastKnownShift) {

        }

        @Override
        public void onShiftStarted() {
            notifyShiftUpdate(getShift());

        }

        @Override
        public void onShiftStartFailed(int responseCode) {

        }

        @Override
        public void onEndShiftSuccess() {
            notifyShiftUpdate(getShift());
        }

        @Override
        public void onEndShiftFailure(int error) {

        }

        @Override
        public void onEndShiftRetryLater() {

        }
    }

}
