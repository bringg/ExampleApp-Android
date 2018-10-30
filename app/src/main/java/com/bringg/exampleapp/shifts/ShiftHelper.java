package com.bringg.exampleapp.shifts;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.Utils;

import java.util.concurrent.CopyOnWriteArrayList;

import driver_sdk.BringgSDKClient;
import driver_sdk.shift.Shift;

import static com.bringg.exampleapp.shifts.ShiftHelper.ShiftState.SHIFT_OFF;
import static com.bringg.exampleapp.shifts.ShiftHelper.ShiftState.SHIFT_ON;

public class ShiftHelper {


    private final Context mContext;

    public enum ShiftState {
        SHIFT_ON,
        SHIFT_OFF
    }

    private final CopyOnWriteArrayList<ShiftStateHelperListener> mListeners = new CopyOnWriteArrayList<>();
    private ShiftState mState;
    private ShiftManager mShiftManager;

    public ShiftHelper(Context context, @NonNull ShiftManager shiftManager) {
        mContext = context;
        mShiftManager = shiftManager;
        mState = BringgSDKClient.getInstance().shiftState().isOnShift() ? SHIFT_ON : SHIFT_OFF;
        mShiftManager.setOnShiftUpdateListener(new OnShiftUpdateListenerImpl());
    }

    public void getShiftStatusFromRemote() {
        mShiftManager.getShiftStatusFromRemote();
    }

    public void toggleShift() {
        if (!Utils.isNetworkAvailable(mContext)) {
            notifyError(mContext.getString(R.string.no_internet_connection));
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

    private void notifyError(String message) {
        for (ShiftStateHelperListener listener : mListeners) {
            listener.onError(message);
        }
    }

    public ShiftState getState() {
        return mState;
    }

    public void register(ShiftStateHelperListener listener) {
        mListeners.add(listener);
    }

    public void unregister(ShiftStateHelperListener listener) {
        mListeners.remove(listener);

    }

    private void updateState(@NonNull Shift shift) {

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
        notifyStateChanged(mState, oldState);

    }

    private void notifyStateChanged(ShiftState state, ShiftState oldState) {
        for (ShiftStateHelperListener listener : mListeners) {
            listener.onStateChanged(this, state, oldState);
        }
    }


    public interface ShiftStateHelperListener {
        void onStateChanged(ShiftHelper shiftHelper, ShiftState state, ShiftState oldState);

        void onError(String string);
    }

    private class OnShiftUpdateListenerImpl implements ShiftManager.OnShiftUpdateListener {
        @Override
        public void onShiftUpdate(@NonNull Shift shift) {
            updateState(shift);
        }
    }
}
