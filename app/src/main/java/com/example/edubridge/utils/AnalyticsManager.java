package com.example.edubridge.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Module 5.3 – Performance Analytics
 */
public class AnalyticsManager {

    private static final String PREF = "analytics_pref";
    private static final String KEY_PROGRESS = "completed_chapters";

    public static void recordChapter(Context context) {
        SharedPreferences sp =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        int current = sp.getInt(KEY_PROGRESS, 0);
        sp.edit().putInt(KEY_PROGRESS, current + 1).apply();
    }

    public static int getProgress(Context context) {
        SharedPreferences sp =
                context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return sp.getInt(KEY_PROGRESS, 0);
    }
}
