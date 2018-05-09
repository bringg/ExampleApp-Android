package com.bringg.exampleapp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.shifts.ShiftHelper;

/**
 * TODO: document your custom view class.
 */
public class ShiftControlView extends FrameLayout {



    private Button mBtnToggleShift;
    private TextView mTvShiftState;
    private ShiftHelper mShiftHelper;
    ShiftHelper.ShiftStateHelperListener mShiftStateHelperListener=new ShiftStateHelperListenerImpl();


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
        mShiftHelper.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mShiftHelper.stop();

    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_shift_control, this, true);

        mShiftHelper =new ShiftHelper(getContext(),mShiftStateHelperListener);
        mTvShiftState = (TextView) findViewById(R.id.tv_shift_state);
        mBtnToggleShift = (Button) findViewById(R.id.btn_toggle_shift);
        mBtnToggleShift.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mShiftHelper.toggleShift();
            }
        });
    }


    private void updateView() {
        switch (mShiftHelper.getState()) {
            case LOADING:
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


    private class ShiftStateHelperListenerImpl implements ShiftHelper.ShiftStateHelperListener
    {
        @Override
        public void onStateChange(ShiftHelper shiftHelper, ShiftHelper.ShiftState state, ShiftHelper.ShiftState oldState) {
            updateView();
        }
    }


}
