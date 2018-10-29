package com.bringg.exampleapp.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.RadioGroup;
import android.widget.ViewAnimator;

import com.bringg.exampleapp.BaseActivity;
import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.ScannerActivity;
import com.bringg.exampleapp.views.dialogs.ListItemDialog;

import java.util.Locale;
import java.util.Map;

import driver_sdk.BringgSDKClient;
import driver_sdk.account.LoginCallback;
import driver_sdk.account.RequestConfirmationCallback;

public class LoginActivity extends BaseActivity {

    private static final int REQ_CODE_QR_SCANNER = 22;
    public static final String TAG = LoginActivity.class.getSimpleName();
    private RadioGroup mRgLoginType;
    private LoginWithEmailView mViewMailLogin;
    private LoginWithPhoneView mViewPhoneLogin;
    private TypeLogin mTypeLogin;
    private ApiRequestCallbackImpl mApiRequestCallback;
    private String mPhone;
    private String mPassword;
    private String mEmail;
    private ViewAnimator mVsLoginViewContainer;

    enum TypeLogin {
        EMAIL,
        PHONE,
        QR_CODE

    }

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mApiRequestCallback = new ApiRequestCallbackImpl();

        mVsLoginViewContainer = findViewById(R.id.v_login_view_switcher);
        mVsLoginViewContainer.setVisibility(View.INVISIBLE);

        mRgLoginType = findViewById(R.id.rg_login_type);
        mViewMailLogin = findViewById(R.id.v_login_with_mail);
        mViewPhoneLogin = findViewById(R.id.v_login_with_phone);

        ViewLoginListenerImpl loginListener = new ViewLoginListenerImpl();
        mViewPhoneLogin.setListener(loginListener);
        mViewMailLogin.setListener(loginListener);
        initRadioGroup();
    }

    private void checkEmailLoginRadioBtn() {
        ((Checkable) findViewById(R.id.rb_email)).setChecked(true);
    }

    @Override
    protected void onRequestCameraResult(boolean allow) {
        if (allow)
            startScanActivityWithPermissionCheck();
    }

    private void initRadioGroup() {
        mRgLoginType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                TypeLogin typeLogin = null;
                switch (checkedId) {
                    case R.id.rb_email:
                        typeLogin = TypeLogin.EMAIL;
                        break;
                    case R.id.rb_phone:
                        typeLogin = TypeLogin.PHONE;
                        break;
                    case R.id.rb_qr_code:
                        typeLogin = TypeLogin.QR_CODE;
                        break;
                }
                setTypeLogin(typeLogin);
            }
        });
    }

    private void setTypeLogin(TypeLogin typeLogin) {
        if (typeLogin == mTypeLogin)
            return;
        mTypeLogin = typeLogin;
        switch (mTypeLogin) {
            case EMAIL:
                mVsLoginViewContainer.setVisibility(View.VISIBLE);
                mVsLoginViewContainer.setDisplayedChild(1);
                break;
            case PHONE:
                mVsLoginViewContainer.setVisibility(View.VISIBLE);
                mVsLoginViewContainer.setDisplayedChild(0);
                break;
            case QR_CODE:
                mVsLoginViewContainer.setVisibility(View.INVISIBLE);
                startScanActivityWithPermissionCheck();
                break;
        }
    }

    private void startScanActivityWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, BaseActivity.REQUEST_CODE_CAMERA);
        } else {
            startScanActivity();
        }
    }

    private void startScanActivity() {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, REQ_CODE_QR_SCANNER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BaseActivity.REQUEST_CODE_CAMERA) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startScanActivity();
            } else {
                Snackbar.make(mRgLoginType, "Can't use QR login without approving the camera permission", Snackbar.LENGTH_LONG).show();
                checkEmailLoginRadioBtn();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQ_CODE_QR_SCANNER) {
            if (data != null) {
                String scanTex = data.getStringExtra(ScannerActivity.EXTRA_SCAN_RESULT);
                loginWithQR(scanTex);
            } else Log.e(TAG, "onActivityResult data = null");
        }
    }

    private void loginWithPhone(@Nullable String merchantId) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithCredentials(null, mPhone, mPassword, merchantId, Locale.getDefault().getCountry(), mApiRequestCallback);

    }

    private void loginWithMail(String merchantId) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithCredentials(mEmail, null, mPassword, merchantId, Locale.getDefault().getCountry(), mApiRequestCallback);
    }

    private void loginWithQR(String scanText) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithQRCode(scanText, mApiRequestCallback);
    }

    @Override
    public void onBackPressed() {
        if (mViewPhoneLogin.getVisibility() == View.VISIBLE) {
            if (mViewPhoneLogin.onBackPress())
                return;
        }
        super.onBackPressed();

    }

    private class ViewLoginListenerImpl implements LoginWithPhoneView.ViewPhoneLoginListener, LoginWithEmailView.ViewEmailLoginListener {

        @Override
        public void onPhoneNumberInsert(String phone) {
            requestConfirmation(phone);
        }

        @Override
        public void onSmsPasswordInsert(String phone, String sms) {
            mPhone = phone;
            mPassword = sms;
            LoginActivity.this.loginWithPhone(null);
        }

        @Override
        public void loginWithMail(String mail, String password) {
            mEmail = mail;
            mPassword = password;
            LoginActivity.this.loginWithMail(null);
        }

        private void requestConfirmation(String phone) {
            showLoadingProgress();
            BringgSDKClient.getInstance().loginActions().requestConfirmation(phone, Locale.getDefault().getCountry(), mApiRequestCallback);
        }
    }

    private class ApiRequestCallbackImpl implements RequestConfirmationCallback, LoginCallback {

        @Override
        public void onRequestConfirmationSuccess() {
            hideLoadingProgress();
            mViewPhoneLogin.notifyRequestConfirmationSuccess();
        }

        @Override
        public void onRequestConfirmationError() {

        }

        @Override
        public void onRequestConfirmationCanceled() {

        }

        @Override
        public void onLoginFailed() {
            hideLoadingProgress();
            toast("Login failed");
        }

        @Override
        public void onLoginCanceled() {

        }

        @Override
        public void onLoginSuccess() {
            hideLoadingProgress();
            setResult(RESULT_OK);
            finish();
        }

        @Override
        public void onLoginMultipleResults(final Map<String, Long> merchantNamesAndIds) {
            hideLoadingProgress();
            new ListItemDialog.Builder(LoginActivity.this)
                    .setTitle(R.string.choose_company)
                    .setItems(merchantNamesAndIds.keySet())
                    .setOnSelectedItemListener(new ListItemDialog.SelectedItemListener() {
                        @Override
                        public void onSelectedItem(int index, String value) {
                            switch (mTypeLogin) {
                                case EMAIL:
                                    loginWithMail(String.valueOf(merchantNamesAndIds.get(value)));
                                    break;
                                case PHONE:
                                    loginWithPhone(String.valueOf(merchantNamesAndIds.get(value)));
                                    break;
                            }
                        }
                    })
                    .build().show();
        }
    }
}
