package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// file: data/local/entity/Course.java
@Entity(tableName = "courses")
public class Course {
    @PrimaryKey
    @NonNull
    public String courseId;

    public String title;
    public String description;
    public String category; // New Field for M2.3 Recommendation Logic

    // New Fields for M2.1 Progress Dashboard
    /**
     * Number of lessons completed (or points earned) for progress tracking.
     */
    public int progress;

    /**
     * Total number of lessons available in the course.
     */
    public int totalLessons;

    /**
     * Status of the course (e.g., "In Progress", "Completed").
     */
    public String status;

    // M2.2 Recommendation Fields
    public String difficulty; // e.g. "Beginner", "Advanced"
    public String duration; // e.g. "2 Weeks", "5 Hours"
}
