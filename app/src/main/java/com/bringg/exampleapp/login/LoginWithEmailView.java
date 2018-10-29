package com.bringg.exampleapp.login;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bringg.exampleapp.R;

public class LoginWithEmailView extends LinearLayout {
    private EditText mEtEmail;
    private EditText mEtPassword;
    private View mBtnLogin;
    private ViewEmailLoginListener mListener;

    public LoginWithEmailView(@NonNull Context context) {
        super(context);
        init();
    }

    public LoginWithEmailView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoginWithEmailView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginWithEmailView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setListener(ViewEmailLoginListener listener) {
        mListener = listener;
    }

    protected void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_login_with_mail, this, true);
        mEtEmail = findViewById(R.id.et_email);
        mEtPassword = findViewById(R.id.et_mail_password);
        mBtnLogin = findViewById(R.id.btn_email_login);
        mBtnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
    }

    private void validate() {
        if (isEmailValid() && isPasswordValid() && mListener != null)
            mListener.loginWithMail(mEtEmail.getText().toString(), mEtPassword.getText().toString());

    }

    private boolean isEmailValid() {
        if (TextUtils.isEmpty(mEtEmail.getText())) {
            showNotification(R.string.mail_empty_message);
            return false;
        }
        return true;
    }

    private boolean isPasswordValid() {
        if (TextUtils.isEmpty(mEtPassword.getText())) {
            showNotification(R.string.password_empty_message);
            return false;
        }
        return true;
    }

    interface ViewEmailLoginListener {
        void loginWithMail(String mail, String password);
    }

    void showNotification(@StringRes int stringId) {
        Snackbar.make(this, stringId, Toast.LENGTH_SHORT).show();
    }
}
