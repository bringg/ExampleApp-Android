package com.bringg.exampleapp.activity;

import android.support.annotation.NonNull;
import android.util.Log;

import driver_sdk.BringgSDKClient;
import driver_sdk.shift.EndShiftCallback;
import driver_sdk.shift.ShiftStateListener;
import driver_sdk.shift.StartShiftResultCallback;

/**
 * This activity listens to shift state changes and handles the basic start/end shift implementation
 * overriding activities may handle the shift state events they care about
 */
public abstract class ShiftStateAwareActivity extends BaseActivity implements ShiftStateListener {

    // notify overriding activity about shift state changes
    protected abstract void onShiftStateChanged(boolean isOnShift);

    // notify overriding activity about shift state change response errors
    protected abstract void showResponseError(@NonNull String message);

    @Override
    protected void onStart() {
        super.onStart();
        // register for shift state change events
        BringgSDKClient.getInstance().shiftState().registerShiftStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // un-register from the sdk to prevent memory leaks
        BringgSDKClient.getInstance().shiftState().unRegisterShiftStateListener(this);
    }

    protected boolean isOnShift() {
        // get current shift state from sdk
        return BringgSDKClient.getInstance().shiftState().isOnShift();
    }

    protected boolean isLoggedIn() {
        // get current login state from sdk
        return BringgSDKClient.getInstance().loginState().isLoggedIn();
    }

    protected void startShift() {
        // start shift locally and on server
        // this call is async and we will be notified via ShiftStateListener implementation about the result
        // the callback is used for specific call results and may be ignored on our case because we use the global ShiftStateListener implementation
        BringgSDKClient.getInstance().shiftActions().startShiftAndWaitForApproval(true, new StartShiftResultCallback() {
            @Override
            public void onShiftStarted() {
                // we don't have to do anything here, the shift state listener will be invoked and logic happens there
                Log.i(TAG, "shift start success");
            }

            @Override
            public void onShiftStartFailed(int responseCode) {
                // we don't have to do anything here, the shift state listener will be invoked and logic happens there
                Log.i(TAG, "shift start failed, code=" + responseCode);
            }
        });
    }

    protected void endShift() {
        // end shift locally and on server
        // this call is async and we will be notified via ShiftStateListener implementation about the result
        // the callback is used for specific call results and may be ignored on our case because we use the global ShiftStateListener implementation
        BringgSDKClient.getInstance().shiftActions().endShift(new EndShiftCallback() {
            @Override
            public void onEndShiftSuccess() {
                // we don't have to do anything here, the shift state listener will be invoked and logic happens there
                Log.i(TAG, "shift end success");
            }

            @Override
            public void onEndShiftFailure(int error) {
                // we don't have to do anything here, the shift state listener will be invoked and logic happens there
                Log.i(TAG, "shift end failed, error=" + error);
            }

            @Override
            public void onEndShiftRetryLater() {
                // we don't have to do anything here, the shift state listener will be invoked and logic happens there
                Log.i(TAG, "shift end failed with retriable error, sdk will retry automatically");
            }
        });
    }

    private void handleShiftStateChanged() {
        onShiftStateChanged(isOnShift());
    }

    // ---------------------------------- shift state listener implementation ---------------------------------- //

    // shift state change events:
    @Override
    public void onShiftStarted(long shiftId) {
        Log.i(TAG, "shift started success");
        hideLoadingProgress();
        handleShiftStateChanged();
    }

    @Override
    public void onShiftEnded() {
        Log.i(TAG, "shift ended locally");
        hideLoadingProgress();
        handleShiftStateChanged();
    }

    @Override
    public void onShiftEndedFromRemote(long shiftId, @NonNull String deviceId) {
        hideLoadingProgress();
        handleShiftStateChanged();
    }

    // shift request events:
    @Override
    public void onStartShiftRequestStarted() {
        showLoadingProgress();
    }

    // shift request error events:
    @Override
    public void onStartShiftFailed(int responseCode) {
        Log.e(TAG, "shift start failed, error code=" + responseCode);
        hideLoadingProgress();
        showResponseError("Failed stating shift on server");
    }

    @Override
    public void onEndShiftRequestSuccess() {
        Log.i(TAG, "shift end reported to the server successfully");
        hideLoadingProgress();
    }

    @Override
    public void onEndShiftAcknowledged() {
        Log.i(TAG, "shift end has been acknowledged by the server");
        hideLoadingProgress();
        handleShiftStateChanged();
    }

    @Override
    public void onEndShiftRequestFailed(int errorCode) {
        Log.e(TAG, "shift end reporting to the server failed, error=" + errorCode);
        hideLoadingProgress();
        showResponseError("shift end reporting to the server failed");
    }

    @Override
    public void onEndShiftRequestRetry() {
        Log.i(TAG, "shift end reporting to the server failed, sdk will retry internally");
    }

    // --------------------------------------------------------------------------------------------------------- //
}
