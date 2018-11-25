package com.bringg.exampleapp.activity;

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

import com.bringg.exampleapp.R;
import com.bringg.exampleapp.views.dialogs.ListItemDialog;
import com.bringg.exampleapp.views.login.LoginWithEmailView;
import com.bringg.exampleapp.views.login.LoginWithPhoneView;

import java.util.Map;

import driver_sdk.BringgSDKClient;
import driver_sdk.account.LoginCallback;
import driver_sdk.account.LoginError;
import driver_sdk.account.RequestConfirmationCallback;

public class LoginActivity extends BaseActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private static final int REQ_CODE_QR_SCANNER = 22;
    private static final int REQUEST_CODE_CAMERA = 101;

    private RadioGroup mRgLoginType;
    private LoginWithPhoneView mViewPhoneLogin;
    private TypeLogin mTypeLogin;
    private ApiRequestCallbackImpl mApiRequestCallback;
    private String mEmail;
    private String mPhone;
    private String mPassword;
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
        mViewPhoneLogin = findViewById(R.id.v_login_with_phone);

        ViewLoginListenerImpl loginListener = new ViewLoginListenerImpl();
        mViewPhoneLogin.setListener(loginListener);
        LoginWithEmailView viewMailLogin = findViewById(R.id.v_login_with_mail);
        viewMailLogin.setListener(loginListener);
        initRadioGroup();
    }

    private void checkEmailLoginRadioBtn() {
        ((Checkable) findViewById(R.id.rb_email)).setChecked(true);
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
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
        if (requestCode == REQUEST_CODE_CAMERA) {
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

    @Override
    public void onBackPressed() {
        if (mViewPhoneLogin.getVisibility() == View.VISIBLE) {
            if (mViewPhoneLogin.onBackPress())
                return;
        }
        super.onBackPressed();
    }

    // ------------------------------- login actions implementation ------------------------------- //
    // all login actions will return to the callback implementation


    /**
     * Use the SDK to login using phone number and sms verification code
     *
     * @param phone               phone number
     * @param smsVerificationCode verification code from sms
     * @param merchantId          optional:
     *                            there are two scenarios:
     *                            1. The user is registered on Bringg server with a single merchant - login flow will continue and callback will be called with the login response.
     *                            2. The user is listed with more than one merchant on Bringg server -
     *                            login flow will call the LoginCallback.onLoginMultipleResults with the list of possible merchants,
     *                            implantation should call the login again with a single selected merchantID
     */
    private void loginWithPhone(@NonNull String phone, @NonNull String smsVerificationCode, @Nullable String merchantId) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithPhone(phone, smsVerificationCode, merchantId, mApiRequestCallback);
    }

    /**
     * Use the SDK to login using user email and password
     *
     * @param email      user email
     * @param password   user password
     * @param merchantId optional:
     *                   there are two scenarios:
     *                   1. The user is registered on Bringg server with a single merchant - login flow will continue and callback will be called with the login response.
     *                   2. The user is listed with more than one merchant on Bringg server -
     *                   login flow will call the LoginCallback.onLoginMultipleResults with the list of possible merchants,
     *                   implantation should call the login again with a single selected merchantID
     */
    private void loginWithEmail(@NonNull String email, @NonNull String password, @Nullable String merchantId) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithEmail(email, password, merchantId, mApiRequestCallback);
    }

    /**
     * Use the SDK to login using QR code scan
     *
     * @param scanText the scan result
     */
    private void loginWithQR(@NonNull String scanText) {
        showLoadingProgress();
        BringgSDKClient.getInstance().loginActions().loginWithQRCode(scanText, mApiRequestCallback);
    }

    /**
     * Send a confirmation sms request to Bringg server, if Bringg recognize the user a confirmation code will be sent by sms.
     * implementation should then call BringgSDKClient.getInstance().loginActions().loginWithPhone() providing the verification code
     *
     * @param phone user phone number
     */
    private void requestConfirmation(String phone) {
        showLoadingProgress();

        // send sms confirmation code request to Bringg
        BringgSDKClient.getInstance().loginActions().requestConfirmation(phone, new RequestConfirmationCallback() {

            @Override
            public void onRequestConfirmationSuccess() {
                hideLoadingProgress();
                mViewPhoneLogin.notifyRequestConfirmationSuccess();
            }

            @Override
            public void onRequestConfirmationError() {
                hideLoadingProgress();
                showErrorMessage("Confirmation request error");
            }

            @Override
            public void onRequestConfirmationCanceled() {
                hideLoadingProgress();
                showErrorMessage("Confirmation request canceled");
            }
        });
    }
    // --------------------------------------------------------------------------------------------- //

    private class ViewLoginListenerImpl implements LoginWithPhoneView.ViewPhoneLoginListener, LoginWithEmailView.ViewEmailLoginListener {

        @Override
        public void onPhoneNumberInsert(String phone) {
            requestConfirmation(phone);
        }

        @Override
        public void onSmsPasswordInsert(String phone, String sms) {
            // we keep the credentials locally - we might need to select a merchant and re-login with a specific merchant later.
            mPhone = phone;
            mPassword = sms;
            loginWithPhone(phone, sms, null);
        }

        @Override
        public void loginWithMail(String mail, String password) {
            // we keep the credentials locally - we might need to select a merchant and re-login with a specific merchant later.
            mEmail = mail;
            mPassword = password;
            loginWithEmail(mail, password, null);
        }
    }

    private class ApiRequestCallbackImpl implements LoginCallback {

        @Override
        public void onLoginFailed(@NonNull LoginError loginError) {
            hideLoadingProgress();
            showErrorMessage("Login failed, error=" + loginError);
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
                                    loginWithEmail(mEmail, mPassword, String.valueOf(merchantNamesAndIds.get(value)));
                                    break;
                                case PHONE:
                                    loginWithPhone(mPhone, mPassword, String.valueOf(merchantNamesAndIds.get(value)));
                                    break;
                            }
                        }
                    })
                    .build().show();
        }
    }

    private void showErrorMessage(@NonNull String message) {
        Snackbar.make(mVsLoginViewContainer, message, Snackbar.LENGTH_LONG).show();
    }
}
