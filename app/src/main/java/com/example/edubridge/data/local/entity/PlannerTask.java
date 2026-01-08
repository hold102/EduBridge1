package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore; // ✅ 新增
import androidx.room.PrimaryKey;

/**
 * Room entity for Study Planner tasks.
 */
@Entity(tableName = "planner_tasks")
public class PlannerTask {

    @PrimaryKey
    @NonNull
    public String id;

    @NonNull
    public String userId;
    public String title;
    public long dueDate;
    public boolean isCompleted;
    public long createdAt;

    public PlannerTask() {
        this.id = "";
        this.userId = "";
    }

    // ✅ 加了 @Ignore
    @Ignore
    public PlannerTask(@NonNull String id, @NonNull String userId, String title, long dueDate, boolean isCompleted,
                       long createdAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.createdAt = createdAt;
    }

    public boolean isOverdue() {
        return !isCompleted && System.currentTimeMillis() > dueDate;
    }
}