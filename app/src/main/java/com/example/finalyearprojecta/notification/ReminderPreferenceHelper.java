package com.example.finalyearprojecta.notification;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Saves and loads the reminder list to SharedPreferences using Gson.
 * Call save() every time the list changes (add or delete).
 * Call load() once when the activity opens.
 */
public class ReminderPreferenceHelper {

    private static final String PREF_NAME = "reminder_prefs";
    private static final String KEY_REMINDERS = "reminders_list";

    private final SharedPreferences prefs;
    private final Gson gson;

    public ReminderPreferenceHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /** Serialize the full list to JSON and store it. */
    public void save(ArrayList<ReminderModel> list) {
        String json = gson.toJson(list);
        prefs.edit().putString(KEY_REMINDERS, json).apply();
    }

    /** Deserialize and return the saved list, or an empty list if nothing saved yet. */
    public ArrayList<ReminderModel> load() {
        String json = prefs.getString(KEY_REMINDERS, null);
        if (json == null) return new ArrayList<>();

        Type type = new TypeToken<ArrayList<ReminderModel>>() {}.getType();
        ArrayList<ReminderModel> loaded = gson.fromJson(json, type);
        return loaded != null ? loaded : new ArrayList<>();
    }
}