package com.bringg.exampleapp.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bringg.exampleapp.BringgApp;
import com.bringg.exampleapp.R;
import com.bringg.exampleapp.ShiftManager;

import driver_sdk.shift.Shift;

/**
 * TODO: document your custom view class.
 */
public class ShiftControlView extends FrameLayout {

    private ShiftState mState;
    private BroadcastReceiver mReceiver = new BroadcastReceiverShiftChangeImpl();

    private enum ShiftState {
        SHIFT_ON,
        SHIFT_OFF,
        LOAD_ON,
        LOAD_OFF
    }

    private Button mBtnToggleShift;
    private TextView mTvShiftState;
    private ShiftManager mShiftManager;


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
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, ShiftManager.getIntentFilterShiftChanged());
        if (getContext().getApplicationContext() instanceof BringgApp) {
            mShiftManager = ((BringgApp) getContext().getApplicationContext()).getShiftManager();
            updateState();
        }
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shift_control, this, true);
        mTvShiftState = (TextView) findViewById(R.id.tv_shift_state);
        mBtnToggleShift = (Button) findViewById(R.id.btn_toggle_shift);
        mBtnToggleShift.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShiftManager.getShift().on) {
                    mShiftManager.updateShiftState(false);
                    setState(ShiftState.LOAD_OFF);
                } else {
                    mShiftManager.updateShiftState(true);
                    setState(ShiftState.LOAD_ON);
                }
            }
        });
    }

    private void setState(ShiftState state) {
        if (mState == state)
            return;
        mState = state;
        updateView();
    }

    private void updateView() {
        switch (mState) {
            case LOAD_ON:
            case LOAD_OFF:
                mBtnToggleShift.setText(R.string.loading);
                break;
            case SHIFT_OFF:
                mBtnToggleShift.setText(R.string.start_shift);
                mTvShiftState.setText(R.string.not_in_shift);
                break;
            case SHIFT_ON:
                mBtnToggleShift.setText(R.string.end_shift);
                mTvShiftState.setText(R.string.in_shift);
                break;
        }
    }


    private class BroadcastReceiverShiftChangeImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
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


}
