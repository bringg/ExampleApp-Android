package com.bringg.exampleapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Utils {
    static final String ISO_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static String msToString(long ms) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return df.format(ms);
    }

    public static String isoToStringDate(String iso) {
        if (TextUtils.isEmpty(iso))
            return "";
        DateFormat df1 = new SimpleDateFormat(ISO_TIME_FORMAT);

        try {

            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            String strDt = simpleDate.format(df1.parse(iso));
            return strDt;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean isNeedAskRuntimePermission() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
