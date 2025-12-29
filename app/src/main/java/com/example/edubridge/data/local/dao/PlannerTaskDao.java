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
 */
@Dao
public interface PlannerTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PlannerTask task);

    @Update
    void update(PlannerTask task);

    @Delete
    void delete(PlannerTask task);

    @Query("SELECT * FROM planner_tasks ORDER BY dueDate ASC, createdAt ASC")
    LiveData<List<PlannerTask>> getAllTasks();

    @Query("SELECT * FROM planner_tasks ORDER BY dueDate ASC, createdAt ASC")
    List<PlannerTask> getAllTasksSync();

    /**
     * Get today's tasks (not overdue, due today or future).
     */
    @Query("SELECT * FROM planner_tasks WHERE dueDate >= :todayStart ORDER BY dueDate ASC")
    LiveData<List<PlannerTask>> getTodayTasks(long todayStart);

    /**
     * Get overdue tasks (due date has passed and not completed).
     */
    @Query("SELECT * FROM planner_tasks WHERE dueDate < :todayStart AND isCompleted = 0 ORDER BY dueDate ASC")
    LiveData<List<PlannerTask>> getOverdueTasks(long todayStart);

    @Query("DELETE FROM planner_tasks")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM planner_tasks WHERE isCompleted = 0")
    int getPendingTaskCount();
}
