package com.example.finalyearprojecta.notification;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Saves and loads reminders per user account.
 * Each Firebase user gets their own isolated key, so logging in with a
 * different account shows only that account's reminders.
 */
public class ReminderPreferenceHelper {

    private static final String PREF_NAME       = "reminder_prefs";
    private static final String KEY_PREFIX      = "reminders_";   // ✅ per-user prefix

    private final SharedPreferences prefs;
    private final Gson gson;
    private final String userKey;

    public ReminderPreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson  = new Gson();

        // ✅ Build a key unique to the logged-in user.
        // Falls back to "guest" if somehow called before login (shouldn't happen).
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (currentUser != null) ? currentUser.getUid() : "guest";
        userKey = KEY_PREFIX + uid;   // e.g. "reminders_abc123xyz"
    }

    /** Serialize the full list to JSON and store it under this user's key. */
    public void save(ArrayList<ReminderModel> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(userKey, json).apply();
    }

    /** Load and return this user's reminder list, or empty list if none saved yet. */
    public ArrayList<ReminderModel> load() {
        String json = prefs.getString(userKey, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<ReminderModel>>() {}.getType();
        ArrayList<ReminderModel> loaded = gson.fromJson(json, type);
        return loaded != null ? loaded : new ArrayList<>();
    }
}