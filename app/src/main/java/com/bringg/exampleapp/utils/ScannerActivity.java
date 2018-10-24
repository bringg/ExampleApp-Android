package com.bringg.exampleapp.utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bringg.exampleapp.R;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static final String TAG = ScannerActivity.class.getSimpleName();
    public static final String EXTRA_SCAN_RESULT = "extra_scan_result";
    private ZXingScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

       mScannerView.setAspectTolerance(0.5f);// Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }


    @Override
    public void handleResult(Result result) {

        // Do something with the result here
        Log.v(TAG, result.getText()); // Prints scan results
        Log.v(TAG, result.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Intent resultIntnet=new Intent();
        resultIntnet.putExtra(EXTRA_SCAN_RESULT,result.getText());
        // If you would like to resume scanning, call this method below:
       // mScannerView.resumeCameraPreview(this);
        setResult(RESULT_OK,resultIntnet);
        onBackPressed();

    }
}