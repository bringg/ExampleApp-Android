package com.bringg.exampleapp.login;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bringg.exampleapp.R;

public class LoginWithPhoneView extends LinearLayout {
    private EditText mEtPhone;
    private EditText mEtSms;
    private Button mBtnLogin;
    private ViewPhoneLoginListener mListener;
    private State mState;


    private enum State {
        PHONE, SMS
    }

    public LoginWithPhoneView(@NonNull Context context) {
        super(context);
        init();
    }

    public LoginWithPhoneView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoginWithPhoneView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginWithPhoneView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setListener(ViewPhoneLoginListener listener) {
        mListener = listener;
    }

    public void notifyRequestConfirmationSuccess() {
        setState(State.SMS);
    }

    protected void init() {
        mState = State.PHONE;
        LayoutInflater.from(getContext()).inflate(R.layout.view_login_with_phone, this, true);
        mEtPhone = findViewById(R.id.et_phone);
        mEtSms = findViewById(R.id.et_sms_password);
        mBtnLogin = findViewById(R.id.btn_phone_login);
        mBtnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                validate();
            }
        });
        setState(State.PHONE);
    }

    private void validate() {
        if (mState == State.PHONE) {
            if (isPhoneValid() && mListener != null)
                mListener.onPhoneNumberInsert(mEtPhone.getText().toString());
        } else if (mState == State.SMS) {
            if (isSmsValid() && mListener != null)
                mListener.onSmsPasswordInsert(mEtPhone.getText().toString(), mEtSms.getText().toString());
        }
    }

    private void setState(State state) {
        if (state == mState)
            return;
        mState = state;
        switch (mState) {
            case PHONE:
                mEtPhone.setVisibility(VISIBLE);
                mEtSms.setVisibility(GONE);
                mBtnLogin.setText(R.string.validate);
                break;
            case SMS:
                mEtPhone.setVisibility(GONE);
                mEtSms.setVisibility(VISIBLE);
                mBtnLogin.setText(R.string.login);
                break;
        }
    }

    boolean onBackPress() {
        if (mState == State.SMS) {
            setState(State.PHONE);
            return true;
        }
        return false;
    }

    private boolean isPhoneValid() {
        String phone = mEtPhone.getText().toString();
        if (TextUtils.isEmpty(phone) || phone.length() != 10) {
            toast(R.string.phone_number_not_valid);
            return false;
        }
        return true;
    }

    private boolean isSmsValid() {

        String sms = mEtSms.getText().toString();
        if (TextUtils.isEmpty(sms)) {
            toast(R.string.sms_empty_message);
            return false;
        }
        return true;
    }

    void toast(int stringId) {
        Toast.makeText(getContext(), stringId, Toast.LENGTH_SHORT).show();
    }

    interface ViewPhoneLoginListener {
        void onPhoneNumberInsert(String phone);

        void onSmsPasswordInsert(String phone, String sms);
    }
}
