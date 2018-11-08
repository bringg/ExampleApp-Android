package com.bringg.exampleapp.views.shift;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bringg.exampleapp.R;

import driver_sdk.BringgSDKClient;
import driver_sdk.shift.EndShiftCallback;
import driver_sdk.shift.ShiftStateListener;
import driver_sdk.shift.StartShiftResultCallback;

public class ShiftControlView extends FrameLayout implements ShiftStateListener {

    private Button mBtnToggleShift;
    private TextView mTvShiftState;


    public ShiftControlView(Context context) {
        super(context);
        init();
    }

    public ShiftControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShiftControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BringgSDKClient.getInstance().shiftState().registerShiftStateListener(this);
        updateView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        BringgSDKClient.getInstance().shiftState().unRegisterShiftStateListener(this);
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shift_control, this, true);

        mTvShiftState = findViewById(R.id.tv_shift_state);
        mBtnToggleShift = findViewById(R.id.btn_toggle_shift);
        mBtnToggleShift.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                showInProgress();

                if (isOnShift()) {
                    // we are currently on shift, call end shift and handle the async response
                    // global ShiftStateListener will be also notified automatically with this call result
                    BringgSDKClient.getInstance().shiftActions().endShift(new EndShiftCallback() {
                        @Override
                        public void onEndShiftSuccess() {
                            updateView();
                        }

                        @Override
                        public void onEndShiftFailure(int error) {
                            updateView();
                            showError("End shift failed, errorCode=" + error);
                        }

                        @Override
                        public void onEndShiftRetryLater() {
                            updateView();
                        }
                    });
                } else {
                    // we are currently off shift, call start shift and handle the async response
                    // global ShiftStateListener will be also notified automatically with this call result
                    BringgSDKClient.getInstance().shiftActions().startShiftAndWaitForApproval(true, new StartShiftResultCallback() {
                        @Override
                        public void onShiftStarted() {
                            updateView();
                        }

                        @Override
                        public void onShiftStartFailed(int responseCode) {
                            updateView();
                            showError("Start shift failed, errorCode=" + responseCode);
                        }
                    });
                }
            }
        });
    }

    private void showError(@NonNull String message) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG);
    }

    private boolean isOnShift() {
        // get the current shift state from the SDK
        return BringgSDKClient.getInstance().shiftState().isOnShift();
    }

    /**
     * update our UI state according to the current shift state from the SDK
     */
    private void updateView() {
        updateView(isOnShift());
    }

    private void updateView(boolean isOnShift) {
        if (isOnShift) {
            mBtnToggleShift.setEnabled(true);
            mBtnToggleShift.setText(R.string.end_shift);
            mTvShiftState.setText(R.string.in_shift);
        } else {
            mBtnToggleShift.setEnabled(true);
            mBtnToggleShift.setText(R.string.start_shift);
            mTvShiftState.setText(R.string.not_in_shift);
        }
    }

    private void showInProgress() {
        mBtnToggleShift.setEnabled(false);
        mBtnToggleShift.setText(R.string.loading);
        mTvShiftState.setText(R.string.loading);
    }


    // ---------------------------------- ShiftStateListener implementation ---------------------------------- //
    // this implementation is very straightforward, we just show progress and update to the current state after changes.
    @Override
    public void onStartShiftRequestStarted() {
        showInProgress();
    }

    @Override
    public void onShiftStarted(long shiftId) {
        updateView(true);
    }

    @Override
    public void onShiftEnded() {
        updateView(false);
    }

    @Override
    public void onStartShiftFailed(int responseCode) {
        updateView();
    }

    @Override
    public void onEndShiftRequestSuccess() {
        updateView();
    }

    @Override
    public void onEndShiftAcknowledged() {
        updateView();
    }

    @Override
    public void onEndShiftRequestFailed(int errorCode) {
        updateView();
    }

    @Override
    public void onEndShiftRequestRetry() {
        updateView();
    }

    @Override
    public void onShiftEndedFromRemote(long shiftId, @NonNull String deviceId) {
        updateView();
    }
}
