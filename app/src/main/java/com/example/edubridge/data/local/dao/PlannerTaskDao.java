package com.example.edubridge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.edubridge.data.local.entity.PlannerTask;

import java.util.List;

/**
 * Data Access Object for PlannerTask entities.
 * All queries filter by userId for user isolation.
 */
@Dao
public interface PlannerTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlannerTask task);

    @Update
    void update(PlannerTask task);

    @Delete
    void delete(PlannerTask task);

    /**
     * Get all tasks for a specific user.
     */
    @Query("SELECT * FROM planner_tasks WHERE userId = :userId ORDER BY dueDate ASC, createdAt ASC")
    LiveData<List<PlannerTask>> getAllTasks(String userId);

    /**
     * Get today's tasks for a specific user (not overdue, due today or future).
     */
    @Query("SELECT * FROM planner_tasks WHERE userId = :userId AND dueDate >= :todayStart ORDER BY dueDate ASC")
    LiveData<List<PlannerTask>> getTodayTasks(String userId, long todayStart);

    /**
     * Get overdue tasks for a specific user (due date has passed and not
     * completed).
     */
    @Query("SELECT * FROM planner_tasks WHERE userId = :userId AND dueDate < :todayStart AND isCompleted = 0 ORDER BY dueDate ASC")
    LiveData<List<PlannerTask>> getOverdueTasks(String userId, long todayStart);

    /**
     * Delete all tasks for a specific user.
     */
    @Query("DELETE FROM planner_tasks WHERE userId = :userId")
    void deleteAllForUser(String userId);

    /**
     * Get pending task count for a specific user.
     */
    @Query("SELECT COUNT(*) FROM planner_tasks WHERE userId = :userId AND isCompleted = 0")
    int getPendingTaskCount(String userId);

    // ===== Legacy methods (without user filter - for migration) =====

    @Query("SELECT * FROM planner_tasks ORDER BY dueDate ASC, createdAt ASC")
    List<PlannerTask> getAllTasksSync();

    @Query("DELETE FROM planner_tasks")
    void deleteAll();
}
