package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Unified Course Entity for Room Database and Firestore.
 * 
 * Supports:
 * - M2.1 Progress Dashboard (local progress tracking)
 * - M2.2/M2.3 Recommendations
 * - M3.1 Course Browsing (search, filters)
 * - M3.2 Course Details (objectives, instructor, prerequisites)
 */
@Entity(tableName = "courses")
public class Course {

    @PrimaryKey
    @NonNull
    public String courseId;

    // Basic Info
    public String title;
    public String description;
    public String category;
    public String difficulty;
    public String duration;
    public String thumbnailUrl;

    // Progress Fields (M2.1)
    public int progress; // Lessons completed
    public int totalLessons; // Total lessons
    public String status; // "Not Started", "In Progress", "Completed"

    // Browsing Fields (M3.1)
    public boolean isPublished;
    public long createdAt;

    // M3.2 Course Details
    public String objectives;
    public String learningOutcomes;
    public String instructorName;
    public String instructorBio;
    public String estimatedEffort;
    public String prerequisites;

    // Syllabus stored as comma-separated string for Room compatibility
    public String syllabusData;

    // Required empty constructor for Room
    public Course() {
        this.courseId = "";
    }

    // Constructor for creating new courses
    @Ignore
    public Course(@NonNull String courseId, String title, String description,
            String category, String difficulty, String duration,
            int totalLessons, boolean isPublished) {
        this.courseId = courseId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.duration = duration;
        this.totalLessons = totalLessons;
        this.isPublished = isPublished;
        this.progress = 0;
        this.status = "Not Started";
        this.createdAt = System.currentTimeMillis();
    }

    // ===== Helper Methods =====

    /**
     * Get syllabus as list (converts from stored string).
     */
    @Ignore
    public List<String> getSyllabusList() {
        if (syllabusData == null || syllabusData.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> list = new ArrayList<>();
        String[] items = syllabusData.split("\\|\\|");
        for (String item : items) {
            if (!item.isEmpty()) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * Set syllabus from list (converts to string for storage).
     */
    @Ignore
    public void setSyllabusList(List<String> syllabus) {
        if (syllabus == null || syllabus.isEmpty()) {
            this.syllabusData = "";
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < syllabus.size(); i++) {
            sb.append(syllabus.get(i));
            if (i < syllabus.size() - 1) {
                sb.append("||");
            }
        }
        this.syllabusData = sb.toString();
    }

    /**
     * Calculate progress percentage.
     */
    @Ignore
    public int getProgressPercent() {
        if (totalLessons <= 0)
            return 0;
        return (progress * 100) / totalLessons;
    }

    /**
     * Check if course matches search query.
     */
    @Ignore
    public boolean matchesSearch(String query) {
        if (query == null || query.isEmpty())
            return true;
        String lowerQuery = query.toLowerCase();
        return (title != null && title.toLowerCase().contains(lowerQuery)) ||
                (description != null && description.toLowerCase().contains(lowerQuery)) ||
                (category != null && category.toLowerCase().contains(lowerQuery));
    }

    /**
     * Check if course matches category filter.
     */
    @Ignore
    public boolean matchesCategory(String categoryFilter) {
        if (categoryFilter == null || categoryFilter.isEmpty() || categoryFilter.equals("All")) {
            return true;
        }
        return category != null && category.equalsIgnoreCase(categoryFilter);
    }

    /**
     * Check if course matches difficulty filter.
     */
    @Ignore
    public boolean matchesDifficulty(String difficultyFilter) {
        if (difficultyFilter == null || difficultyFilter.isEmpty() || difficultyFilter.equals("All")) {
            return true;
        }
        return difficulty != null && difficulty.equalsIgnoreCase(difficultyFilter);
    }
}
