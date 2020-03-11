package com.fuelio.fuelio;

import android.content.Context;
import android.content.SharedPreferences;


public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "fuelio";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        editor.apply();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.apply();
    }

    public void saveUser(String id, String name) {
        editor.putString("user_id", id);
        editor.putString("name", name);
        editor.putString("type", "user");
        editor.apply();
    }

    public void setPhone(String phone) {
        editor.putString("phone", phone);
        editor.apply();
    }

    public void setType(String pin) {
        editor.putString("type", pin);
        editor.apply();
    }

    public void setName(String name) {
        editor.putString("name", name);
        editor.apply();
    }

    public void setAbout(String about) {
        editor.putString("about", about);
        editor.apply();
    }


    public void setRegistrationtoken(String token) {
        editor.putString("token", token);
        editor.apply();
    }

    public void setOpenBubble(boolean open) {
        editor.putBoolean("open", open);
        editor.apply();
    }

    public void setDeleteReminders(boolean del) {
        editor.putBoolean("delete", del);
        editor.apply();
    }

    public void logout() {
        editor.clear().apply();
    }

    public boolean getDeleteReminders() {
        return pref.getBoolean("delete", false);
    }

    public boolean getOpenBubble() {
        return pref.getBoolean("open", true);
    }

    public String getRegistrationToken() {
        return pref.getString("token", "none");
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public String getName() {
        return pref.getString("name", "none");
    }

    public String getType() {
        return pref.getString("type", "none");
    }

    public String getId() {
        return pref.getString("user_id", "none");
    }

    public String getPhone() {
        return pref.getString("phone", "none");
    }

    public String getAbout() {
        return pref.getString("about", "About yourself");
    }


}
