package com.bringg.exampleapp.login;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import com.bringg.exampleapp.BaseActivity;
import com.bringg.exampleapp.BringgApp;
import com.bringg.exampleapp.R;
import com.bringg.exampleapp.utils.ScannerActivity;
import com.bringg.exampleapp.views.dialogs.ListItemDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Map;

import driver_sdk.account.LoginCallback;
import driver_sdk.account.RequestConfirmationCallback;
import driver_sdk.models.User;

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
        findViews();
        initViews();
        mApiRequestCallback = new ApiRequestCallbackImpl();


    }

    @Override
    protected void onRequestCameraResult(boolean allow) {
        if (allow)
            startQrCodeActivity();
    }

    private void findViews() {
        mRgLoginType = (RadioGroup) findViewById(R.id.rg_login_type);
        mViewMailLogin = (LoginWithEmailView) findViewById(R.id.v_login_with_mail);
        mViewPhoneLogin = (LoginWithPhoneView) findViewById(R.id.v_login_with_phone);
    }

    private void initViews() {
        mTypeLogin = TypeLogin.EMAIL;
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
        ViewLoginListenerImpl loginListener = new ViewLoginListenerImpl();
        mViewPhoneLogin.setListener(loginListener);
        mViewMailLogin.setListener(loginListener);
    }

    private void setTypeLogin(TypeLogin typeLogin) {
        if (typeLogin == mTypeLogin)
            return;
        mTypeLogin = typeLogin;
        switch (mTypeLogin) {
            case EMAIL:
                mViewPhoneLogin.setVisibility(View.GONE);
                mViewMailLogin.setVisibility(View.VISIBLE);
                break;
            case PHONE:
                mViewPhoneLogin.setVisibility(View.VISIBLE);
                mViewMailLogin.setVisibility(View.GONE);
                break;
            case QR_CODE:
                if (!askCameraPermission())
                    startQrCodeActivity();
                break;
        }
    }

    private void startQrCodeActivity() {

        Intent intent = new Intent(this, ScannerActivity.class);
        startActivityForResult(intent, REQ_CODE_QR_SCANNER);
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

    private void loginWithPhone(String merchantId) {
        showLoadingProgress();
        mBringgProvider.getClient().loginActions().loginWithCredentials(null, mPhone, mPassword, merchantId, Locale.getDefault().getCountry(), mApiRequestCallback);

    }

    private void loginWithMail(String merchantId) {
        showLoadingProgress();

        mBringgProvider.getClient().loginActions().loginWithCredentials(mEmail, null, mPassword, merchantId, Locale.getDefault().getCountry(), mApiRequestCallback);

    }

    private void loginWithQR(String scanTex) {
        try {
            Log.d(TAG, scanTex);
            JSONObject jScan = new JSONObject(scanTex);
            String token = jScan.getString("token");
            String secret = jScan.getString("secret");
            String region = jScan.getString("region");
            mBringgProvider.getClient().loginActions().loginWithQRCode(region, token, secret, mApiRequestCallback);
        } catch (JSONException e) {
            e.printStackTrace();
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
            //TODO need to move to loginAction()
            mBringgProvider.getClient().credentialsActions().requestConfirmation(phone, Locale.getDefault().getCountry(), mApiRequestCallback);
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
            if (getApplication() instanceof BringgApp) {
                ((BringgApp) getApplication()).notifyLoginSuccess();
            }
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
