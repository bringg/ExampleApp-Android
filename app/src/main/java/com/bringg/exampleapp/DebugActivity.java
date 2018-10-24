package com.bringg.exampleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bringg.exampleapp.shifts.ShiftHelperActivity;
import com.bringg.exampleapp.utils.Utils;


abstract public class DebugActivity extends ShiftHelperActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!askPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_WRITE_EXTERNAL_STORAGE)) {
            //  LoggerDebug.get().start();
        }


    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {

            }
        }
    }

    private boolean askPermission(String manifestPermission, int requestCode) {

        if (Utils.isNeedAskRuntimePermission() && ContextCompat.checkSelfPermission(this, manifestPermission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{manifestPermission}, requestCode);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}
