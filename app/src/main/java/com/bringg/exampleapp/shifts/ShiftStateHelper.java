package com.bringg.exampleapp.shifts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShiftStateHelper {

    ShiftStateHelper()
    void start() {

    }

    void stop()
    {

    }

    public interface ShiftStateHelperListener {

    }
    private class BroadcastReceiverShiftChangeImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateState();
        }
    }
}
