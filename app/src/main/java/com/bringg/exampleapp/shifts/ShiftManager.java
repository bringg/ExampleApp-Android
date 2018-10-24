package com.bringg.exampleapp.shifts;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.bringg.exampleapp.BringgProvider;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import driver_sdk.LeanBringgSDKClient;
import driver_sdk.connection.services.RequestQueueService;
import driver_sdk.shift.EndShiftCallback;
import driver_sdk.shift.GetShiftResultCallback;
import driver_sdk.shift.Shift;
import driver_sdk.shift.ShiftEventsListener;
import driver_sdk.shift.StartShiftResultCallback;

public class ShiftManager implements ShiftEventsListener {

    public static final String TAG = ShiftManager.class.getSimpleName();
    private final LeanBringgSDKClient mClient;
    public static final String ACTION_SHIFT_CHANGE = "com.bringg.exampleapp.ACTION_SHIFT_CHANGE";
    public static final String EXTRA_IN_SHIFT = "com.bringg.exampleapp.EXTRA_IN_SHIFT";
    public static final String EXTRA_SHIFT_ERROR = "com.bringg.exampleapp.EXTRA_SHIFT_ERROR";
    public static final String EXTRA_SHIFT_ERROR_MESSAGE = "com.bringg.exampleapp.EXTRA_SHIFT_ERROR_MESSAGE";


    private ShiftResultCallbackImpl mShiftResultCallback;
    private Context mContext;


    public static IntentFilter getIntentFilterShiftChanged() {
        return new IntentFilter(ACTION_SHIFT_CHANGE);
    }

    public ShiftManager(Context context, BringgProvider bringgProvider) {
        mContext = context;
        mClient = bringgProvider.getClient();
        bringgProvider.addShiftListener(this);
        mShiftResultCallback = new ShiftResultCallbackImpl();
    }

    public Shift getShift() {
        return mClient.shiftActions().getShift();
    }

    public void load() {
        mClient.shiftActions().getShiftStatusFromRemote(mShiftResultCallback);
    }

    public void updateShiftState(boolean inShift) {
        if (inShift)
            mClient.shiftActions().startShiftAndWaitForApproval(true, mShiftResultCallback);
        else
            mClient.shiftActions().endShift(mShiftResultCallback);
    }

    private void notifyShiftUpdate(Shift shift) {
        Intent intent = new Intent();
        intent.setAction(ACTION_SHIFT_CHANGE);
        intent.putExtra(EXTRA_IN_SHIFT, shift.on);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

    }

    @Override
    public void onShiftEnded(long l, @NonNull String s) {
        notifyShiftUpdate(getShift());
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
            Log.d(TAG, "onEndShiftSuccess");
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
