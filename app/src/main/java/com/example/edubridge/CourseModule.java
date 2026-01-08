package com.example.edubridge;

import java.util.ArrayList;
import java.util.List;

/**
 * M3.4.1 Module - Organizes lessons within a course
 */
public class CourseModule {

    private String moduleId;
    private String courseId;
    private String title;
    private String description;
    private int orderIndex; // For sequential ordering
    private boolean isLocked; // M3.4.2: Sequential progression
    private List<Lesson> lessons;

    public CourseModule() {
        this.lessons = new ArrayList<>();
    }

    public CourseModule(String moduleId, String courseId, String title,
            String description, int orderIndex) {
        this.moduleId = moduleId;
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
        this.isLocked = orderIndex > 0; // First module unlocked by default
        this.lessons = new ArrayList<>();
    }

    // Getters and Setters
    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public void setLessons(List<Lesson> lessons) {
        this.lessons = lessons;
    }

    /**
     * Calculate module completion percentage based on lessons.
     */
    public int getCompletionPercent() {
        if (lessons == null || lessons.isEmpty())
            return 0;
        int completed = 0;
        for (Lesson lesson : lessons) {
            if (lesson.isCompleted())
                completed++;
        }
        return (completed * 100) / lessons.size();
    }

    /**
     * Check if all lessons in module are completed.
     */
    public boolean isCompleted() {
        if (lessons == null || lessons.isEmpty())
            return false;
        for (Lesson lesson : lessons) {
            if (!lesson.isCompleted())
                return false;
        }
        return true;
    }
}
