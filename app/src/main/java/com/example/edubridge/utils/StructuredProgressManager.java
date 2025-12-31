package com.example.edubridge.utils;

import android.content.Context;

/**
 * Manages structured content progress for M3.4.
 *
 * Structure:
 * Course -> Module (implicit) -> Lesson -> Activity
 *
 * Progress is stored locally using SharedPreferences
 * to support offline usage.
 */
public class StructuredProgressManager {

    private static final String PREF_NAME = "structured_content_progress";

    /**
     * Marks a specific lesson as completed.
     */
    public static void markLessonCompleted(Context context,
                                           String courseTitle,
                                           int lessonIndex) {

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(buildLessonKey(courseTitle, lessonIndex), true)
                .apply();
    }

    /**
     * Checks whether a specific lesson is completed.
     */
    public static boolean isLessonCompleted(Context context,
                                            String courseTitle,
                                            int lessonIndex) {

        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(buildLessonKey(courseTitle, lessonIndex), false);
    }

    /**
     * Returns the number of completed lessons for a course.
     */
    public static int getCompletedLessons(Context context,
                                          String courseTitle,
                                          int totalLessons) {

        int completed = 0;
        for (int i = 1; i <= totalLessons; i++) {
            if (isLessonCompleted(context, courseTitle, i)) {
                completed++;
            }
        }
        return completed;
    }

    /**
     * Builds a unique key for each lesson.
     */
    private static String buildLessonKey(String courseTitle,
                                         int lessonIndex) {
        return courseTitle + "_lesson_" + lessonIndex;
    }
}
