package com.example.edubridge.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class EnrollmentManager {

    private static final String PREF_NAME = "course_enrollments";

    private static SharedPreferences getPrefs(Context ctx) {
        return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Check enrollment
    public static boolean isEnrolled(Context ctx, String courseTitle) {
        return getPrefs(ctx).contains(courseTitle);
    }

    // Enroll course (progress starts at 0)
    public static void enroll(Context ctx, String courseTitle) {
        getPrefs(ctx)
                .edit()
                .putInt(courseTitle, 0)
                .apply();
    }

    // Unenroll course
    public static void unenroll(Context ctx, String courseTitle) {
        getPrefs(ctx)
                .edit()
                .remove(courseTitle)
                .apply();
    }

    // Update progress (0–100)
    public static void updateProgress(Context ctx, String courseTitle, int progress) {
        if (isEnrolled(ctx, courseTitle)) {
            getPrefs(ctx)
                    .edit()
                    .putInt(courseTitle, progress)
                    .apply();
        }
    }

    // Get all enrolled courses
    public static Map<String, Integer> getAllEnrollments(Context ctx) {
        Map<String, ?> all = getPrefs(ctx).getAll();
        Map<String, Integer> result = new HashMap<>();

        for (String key : all.keySet()) {
            Object value = all.get(key);
            if (value instanceof Integer) {
                result.put(key, (Integer) value);
            }
        }
        return result;
    }
}
