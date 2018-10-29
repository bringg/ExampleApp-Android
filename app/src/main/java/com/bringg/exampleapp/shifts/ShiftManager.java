package com.bringg.exampleapp.shifts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.bringg.exampleapp.BringgProvider;

import driver_sdk.LeanBringgSDKClient;
import driver_sdk.shift.EndShiftCallback;
import driver_sdk.shift.GetShiftResultCallback;
import driver_sdk.shift.Shift;
import driver_sdk.shift.ShiftEventsListener;
import driver_sdk.shift.StartShiftResultCallback;

public class ShiftManager implements ShiftEventsListener {

    private static final String TAG = ShiftManager.class.getSimpleName();
    private final LeanBringgSDKClient mClient;


    private ShiftResultCallbackImpl mShiftResultCallback;
    private OnShiftUpdateListener mListener;


    public ShiftManager(BringgProvider bringgProvider) {
        mClient = bringgProvider.getClient();
        bringgProvider.addShiftListener(this);
        mShiftResultCallback = new ShiftResultCallbackImpl();
    }

    void setOnShiftUpdateListener(@NonNull OnShiftUpdateListener listener) {
        mListener = listener;
    }

    public Shift getShift() {
        return mClient.shiftActions().getShift();
    }

    public void getShiftStatusFromRemote() {
        mClient.shiftActions().getShiftStatusFromRemote(mShiftResultCallback);
    }

    void updateShiftState(boolean inShift) {
        if (inShift)
            mClient.shiftActions().startShiftAndWaitForApproval(true, mShiftResultCallback);
        else
            mClient.shiftActions().endShift(mShiftResultCallback);
    }

    @Override
    public void onShiftEnded(long l, @NonNull String s) {
        notifyShiftUpdate(getShift());
    }

    private void notifyShiftUpdate(@NonNull Shift shift) {
        if (mListener != null)
            mListener.onShiftUpdate(shift);
    }

    private class ShiftResultCallbackImpl implements GetShiftResultCallback, StartShiftResultCallback, EndShiftCallback {

        @Override
        public void onGetShiftStatusResult(@NonNull Shift shift) {
            Log.d(TAG, "onGetShiftStatusResult");
            notifyShiftUpdate(shift);
        }

        @Override
        public void onGetShiftStatusFailed(@Nullable Shift lastKnownShift) {
            Log.d(TAG, "onGetShiftStatusFailed");
        }

        @Override
        public void onShiftStarted() {
            Log.d(TAG, "onShiftStarted");
            notifyShiftUpdate(getShift());
        }

        @Override
        public void onShiftStartFailed(int responseCode) {
            Log.d(TAG, "onShiftStartFailed responseCode:" + responseCode);
        }

        @Override
        public void onEndShiftSuccess() {
            Log.d(TAG, "onEndShiftSuccess");
            notifyShiftUpdate(getShift());
        }

        @Override
        public void onEndShiftFailure(int error) {
            Log.d(TAG, "onEndShiftFailure error:" + error);

        }

        @Override
        public void onEndShiftRetryLater() {
            Log.d(TAG, "onEndShiftRetryLater");
        }
    }

    public interface OnShiftUpdateListener {
        void onShiftUpdate(@NonNull Shift shift);
    }
}
