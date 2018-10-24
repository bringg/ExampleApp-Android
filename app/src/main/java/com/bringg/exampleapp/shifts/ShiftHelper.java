package com.bringg.exampleapp.shifts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.bringg.exampleapp.BringgApp;
import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.Utils;

import driver_sdk.connection.services.RequestQueueService;
import driver_sdk.shift.Shift;

public class ShiftHelper {


    private boolean mIsStart;

    public enum ShiftState {
        SHIFT_ON,
        SHIFT_OFF
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
        if (!Utils.isNetworkAvailable(mContext)) {
            if (mListener != null)
                mListener.onError(mContext.getString(R.string.no_internet_connection));
            return;
        }
        if (mShiftManager.getShift() == null)
            return;
        if (mShiftManager.getShift().on) {
            mShiftManager.updateShiftState(false);
            setState(ShiftState.SHIFT_OFF);
        } else {
            mShiftManager.updateShiftState(true);
            setState(ShiftState.SHIFT_ON);
        }
    }


    public ShiftState getState() {
        return mState;
    }

    public void register() {
        if (!mIsStart) {
            mIsStart = true;
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, ShiftManager.getIntentFilterShiftChanged());
            updateState();
        }
    }

    public void unregister() {
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
        updateServicesByState();
        if (mListener != null)
            mListener.onStateChanged(this, mState, oldState);
    }

    private void updateServicesByState() {
        if (mState == ShiftState.SHIFT_ON) {
            startShiftService();
        } else if (mState == ShiftState.SHIFT_OFF) {
            stopShiftService();
        }
    }


    private void startShiftService() {
        //mContext.startService(new Intent(mContext, ShiftService.class));
    }

    public void stopShiftService() {
      //  mContext.stopService(new Intent(mContext, ShiftService.class));

    }

    public interface ShiftStateHelperListener {
        void onStateChanged(ShiftHelper shiftHelper, ShiftState state, ShiftState oldState);

        void onError(String string);
    }

    private class BroadcastReceiverShiftChangeImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    }


}
