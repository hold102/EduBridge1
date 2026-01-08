package com.example.edubridge.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages structured content progress for M3.4.
 * Progress is stored locally to support offline usage.
 */
public class StructuredProgressManager {

    private static final String PREF_NAME = "structured_content_progress";

    /**
     * Check if a specific lesson is completed.
     */
    public static boolean isLessonCompleted(Context context,
                                            String courseTitle,
                                            int lessonIndex) {
        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        return prefs.getBoolean(courseTitle + "_lesson_" + lessonIndex, false);
    }

    /**
     * Mark a lesson as completed.
     */
    public static void markLessonCompleted(Context context,
                                           String courseTitle,
                                           int lessonIndex) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        prefs.edit()
                .putBoolean(courseTitle + "_lesson_" + lessonIndex, true)
                .apply();
    }

    /**
     * Returns how many lessons are completed in this course.
     */
    public static int getCompletedLessons(Context context,
                                          String courseTitle,
                                          int totalLessons) {

        int count = 0;
        for (int i = 1; i <= totalLessons; i++) {
            if (isLessonCompleted(context, courseTitle, i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Clears all structured progress for a course.
     * Used when user unenrolls.
     */
    public static void clearCourse(Context context,
                                   String courseTitle,
                                   int totalLessons) {

        SharedPreferences.Editor editor =
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                        .edit();

        for (int i = 1; i <= totalLessons; i++) {
            editor.remove(courseTitle + "_lesson_" + i);
        }

        editor.apply();
    }
}
