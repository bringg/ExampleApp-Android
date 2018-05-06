package com.bringg.exampleapp.database;

import android.content.Context;
import android.content.SharedPreferences;

import driver_sdk.models.User;
import driver_sdk.storage.db.BringgSchema;

public class Pref {
    private static final String BRINGG_PREF_NAME = "BRINGG_PREF_NAME";
    private final SharedPreferences editor;

    private static Pref INSTANCE;

    public Pref(Context context) {

        editor = context.getSharedPreferences(BRINGG_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static Pref get(Context context) {
        if (INSTANCE == null) {
            synchronized (Pref.class) {
                if (INSTANCE == null)
                    INSTANCE = new Pref(context);
            }
        }
        return INSTANCE;
    }

    public void setUser(User user) {
        editor.edit().
                putLong(BringgSchema.COL_ID, user.getId()).
                putString(BringgSchema.COL_NAME, user.getName()).
                putString(BringgSchema.COL_STATUS, user.getStatus()).
                putString(BringgSchema.COL_SUB, user.getSubStatus()).
                putString(BringgSchema.COL_PHONE, user.getPhone()).
                putString(BringgSchema.COL_IMAGE, user.getImageUrl()).
                putBoolean(BringgSchema.COL_ADMIN, user.isAdmin()).
                putBoolean(BringgSchema.COL_BETA, user.isBeta()).
                putBoolean(BringgSchema.COL_DEBUG, user.isDebug()).
                putString(BringgSchema.COL_EMAIL, user.getEmail()).
                putString(BringgSchema.COL_AUTHENTICATION_TOKEN, user.getAuthenticationToken()).
                commit();

    }

    public User getUser() {
        User user = new User(
                editor.getLong(BringgSchema.COL_ID, 0),
                editor.getString(BringgSchema.COL_NAME, null),
                editor.getString(BringgSchema.COL_PHONE, null),
                editor.getString(BringgSchema.COL_STATUS, null),
                editor.getString(BringgSchema.COL_SUB, null),
                editor.getString(BringgSchema.COL_IMAGE, null),
                editor.getBoolean(BringgSchema.COL_ADMIN, false),
                editor.getBoolean(BringgSchema.COL_BETA, false),
                editor.getBoolean(BringgSchema.COL_DEBUG, false),
                editor.getString(BringgSchema.COL_EMAIL, null),
                editor.getString(BringgSchema.COL_AUTHENTICATION_TOKEN, null));
        return user;
    }

    public void clear() {
        editor.edit().clear().commit();
    }
}
