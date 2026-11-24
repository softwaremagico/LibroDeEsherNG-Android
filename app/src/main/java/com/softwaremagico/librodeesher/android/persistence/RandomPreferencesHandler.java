package com.softwaremagico.librodeesher.android.persistence;

import android.content.Context;
import android.content.SharedPreferences;

/** Stores the random-generation preferences that used to live in the legacy RandomWindow. */
public final class RandomPreferencesHandler {

    private static final String PREFERENCES = "LibroDeEsherRandomPreferences";
    private static final String SPECIALIZATION = "specializationLevel";

    private static volatile RandomPreferencesHandler instance;

    private final SharedPreferences preferences;

    private RandomPreferencesHandler(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public static RandomPreferencesHandler getInstance(Context context) {
        if (instance == null) {
            synchronized (RandomPreferencesHandler.class) {
                if (instance == null) {
                    instance = new RandomPreferencesHandler(context);
                }
            }
        }
        return instance;
    }

    /** Specialization level in the [-3..3] range expected by the rules engine (0 = random). */
    public int getSpecializationLevel() {
        return Math.max(-3, Math.min(3, preferences.getInt(SPECIALIZATION, 0)));
    }

    public void setSpecializationLevel(int specializationLevel) {
        preferences.edit()
                .putInt(SPECIALIZATION, Math.max(-3, Math.min(3, specializationLevel)))
                .apply();
    }
}
