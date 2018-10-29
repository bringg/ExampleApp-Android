package com.bringg.exampleapp.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bringg.exampleapp.BringgApp;
import com.bringg.exampleapp.R;
import com.bringg.exampleapp.shifts.ShiftHelper;

/**
 * TODO: document your custom view class.
 */
public class ShiftControlView extends FrameLayout {


    private Button mBtnToggleShift;
    private TextView mTvShiftState;
    private ShiftHelper mShiftHelper;
    ShiftHelper.ShiftStateHelperListener mShiftStateHelperListener = new ShiftStateHelperListenerImpl();


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
        mShiftHelper.register(mShiftStateHelperListener);
        updateView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mShiftHelper.unregister(mShiftStateHelperListener);

    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shift_control, this, true);

        mShiftHelper = ((BringgApp) ((Activity) getContext()).getApplication()).getShiftHelper();
        mTvShiftState = (TextView) findViewById(R.id.tv_shift_state);
        mBtnToggleShift = (Button) findViewById(R.id.btn_toggle_shift);
        mBtnToggleShift.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnToggleShift.setEnabled(false);
                mShiftHelper.toggleShift();
            }
        });
    }


    private void updateView() {
        switch (mShiftHelper.getState()) {
            case SHIFT_OFF:
                mBtnToggleShift.setEnabled(true);
                mBtnToggleShift.setText(R.string.start_shift);
                mTvShiftState.setText(R.string.not_in_shift);
                break;
            case SHIFT_ON:
                mBtnToggleShift.setEnabled(true);
                mBtnToggleShift.setText(R.string.end_shift);
                mTvShiftState.setText(R.string.in_shift);
                break;
            case UNKNOWN:
                mBtnToggleShift.setEnabled(false);
                mBtnToggleShift.setText(R.string.loading);
                mTvShiftState.setText(R.string.loading);
                break;
        }
    }


    private class ShiftStateHelperListenerImpl implements ShiftHelper.ShiftStateHelperListener {
        @Override
        public void onStateChanged(ShiftHelper shiftHelper, ShiftHelper.ShiftState state, ShiftHelper.ShiftState oldState) {
            mBtnToggleShift.setEnabled(true);

            updateView();
        }

        @Override
        public void onError(String message) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }


}
