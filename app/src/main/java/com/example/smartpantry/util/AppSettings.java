package com.example.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves the user's settings using SharedPreferences (small key-value storage).
 * Keeping it in one class means every screen reads the settings the same way.
 */
public class AppSettings {

    private static final String PREFS_NAME = "smart_pantry_settings";

    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_EXPIRY_DAYS = "expiry_days";
    private static final String KEY_SHOW_ALMOST_THERE = "show_almost_there";
    private static final String KEY_DEFAULT_UNIT = "default_unit";

    // The choices shown in the "days before expiry" spinner
    public static final int[] EXPIRY_DAY_OPTIONS = {1, 2, 3, 5, 7};

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isExpiryAlertsOn(Context context) {
        return getPrefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsOn(Context context, boolean on) {
        getPrefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, on).apply();
    }

    public static int getExpiryDays(Context context) {
        return getPrefs(context).getInt(KEY_EXPIRY_DAYS, 3);
    }

    public static void setExpiryDays(Context context, int days) {
        getPrefs(context).edit().putInt(KEY_EXPIRY_DAYS, days).apply();
    }

    public static boolean isAlmostThereOn(Context context) {
        return getPrefs(context).getBoolean(KEY_SHOW_ALMOST_THERE, true);
    }

    public static void setAlmostThereOn(Context context, boolean on) {
        getPrefs(context).edit().putBoolean(KEY_SHOW_ALMOST_THERE, on).apply();
    }

    public static String getDefaultUnit(Context context) {
        return getPrefs(context).getString(KEY_DEFAULT_UNIT, "pcs");
    }

    public static void setDefaultUnit(Context context, String unit) {
        getPrefs(context).edit().putString(KEY_DEFAULT_UNIT, unit).apply();
    }
}
