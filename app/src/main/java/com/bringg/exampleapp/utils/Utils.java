package com.bringg.exampleapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {
    private static final String ISO_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static String isoToStringDate(String iso) {
        if (TextUtils.isEmpty(iso))
            return "";

        DateFormat df1 = new SimpleDateFormat(ISO_TIME_FORMAT, Locale.getDefault());
        try {
            SimpleDateFormat simpleDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            return simpleDate.format(df1.parse(iso));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }
}
