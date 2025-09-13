package com.example.zabello.domain.session;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS = "session_prefs";
    private static final String KEY_USER_ID = "user_id";

    private static SessionManager INSTANCE;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SessionManager(context);
        }
        return INSTANCE;
    }

    public void setUserId(long id) {
        prefs.edit().putLong(KEY_USER_ID, id).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0L);
    }

    public boolean isLoggedIn() {
        return getUserId() > 0L;
    }

    public void clear() {
        prefs.edit().remove(KEY_USER_ID).apply();
    }
}
