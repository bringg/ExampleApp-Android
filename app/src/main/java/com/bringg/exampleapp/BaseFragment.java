package com.bringg.exampleapp;

import android.content.Context;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {
    protected BringgProvider mBringgProvider;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(getActivity() instanceof BaseActivity)
            mBringgProvider=((BaseActivity)getActivity()).getBringProvider();
    }
}
