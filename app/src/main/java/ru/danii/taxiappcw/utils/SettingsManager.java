package ru.danii.taxiappcw.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;

    private static final String PREF_NAME = "taxi_settings";
    private static final String KEY_THEME = "theme_mode";
    private static final String KEY_TRIP_ACTIVE = "is_trip_active";
    private final SharedPreferences prefs;

    public SettingsManager(Context context) {
        // Используем applicationContext, чтобы не было утечек памяти
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveThemeMode(int mode) {
        prefs.edit().putInt(KEY_THEME, mode).commit(); // commit() надежнее apply() для мгновенной смены темы
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, MODE_SYSTEM);
    }

    public void applyTheme(int mode) {
        if (mode == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public void saveLastTrip(String from, String to, String tariff) {
        prefs.edit()
                .putString("last_from", from)
                .putString("last_to", to)
                .putString("last_tariff", tariff)
                .apply();
    }

    public String[] getLastTrip() {
        return new String[]{
                prefs.getString("last_from", ""),
                prefs.getString("last_to", ""),
                prefs.getString("last_tariff", "Эконом")
        };
    }

    /**
     * Устанавливает статус активности текущей поездки.
     * @param isActive true, если поездка идет, false в остальных случаях.
     */
    public void setTripActive(boolean isActive) {
        prefs.edit().putBoolean(KEY_TRIP_ACTIVE, isActive).apply();
    }

    /**
     * Проверяет, осталась ли незавершенная поездка.
     * @return статус активности поездки.
     */
    public boolean isTripActive() {
        return prefs.getBoolean(KEY_TRIP_ACTIVE, false);
    }
}