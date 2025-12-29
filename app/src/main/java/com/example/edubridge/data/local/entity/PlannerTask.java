package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for Study Planner tasks.
 */
@Entity(tableName = "planner_tasks")
public class PlannerTask {

    @PrimaryKey
    @NonNull
    public String id;

    /**
     * Task title/description.
     */
    public String title;

    /**
     * Due date timestamp (milliseconds).
     * Set to end of day (11:59:59 PM) for the due date.
     */
    public long dueDate;

    /**
     * Whether the task is completed.
     */
    public boolean isCompleted;

    /**
     * Creation timestamp.
     */
    public long createdAt;

    public PlannerTask() {
        this.id = "";
    }

    public PlannerTask(@NonNull String id, String title, long dueDate, boolean isCompleted, long createdAt) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    /**
     * Check if task is overdue (due date has passed and not completed).
     */
    public boolean isOverdue() {
        return !isCompleted && System.currentTimeMillis() > dueDate;
    }
}
