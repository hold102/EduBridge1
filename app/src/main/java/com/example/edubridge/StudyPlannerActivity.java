package com.example.edubridge;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.dao.PlannerTaskDao;
import com.example.edubridge.data.local.entity.PlannerTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Study Planner Activity - Task Management
 * 
 * Features:
 * - Add new tasks with due time
 * - Mark tasks as complete
 * - Delete tasks
 * - Automatic overdue detection (tasks past 11:59 PM)
 */
public class StudyPlannerActivity extends AppCompatActivity implements PlannerTaskAdapter.TaskClickListener {

    private RecyclerView rvTodayTasks;
    private RecyclerView rvOverdueTasks;
    private TextView tvTodayHeader;
    private TextView tvOverdueHeader;
    private View layoutEmptyState;

    private PlannerTaskAdapter todayAdapter;
    private PlannerTaskAdapter overdueAdapter;
    private List<PlannerTask> todayTasks = new ArrayList<>();
    private List<PlannerTask> overdueTasks = new ArrayList<>();

    private PlannerTaskDao taskDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_planner);

        // Initialize database
        taskDao = AppDatabase.getInstance(this).plannerTaskDao();

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Find views
        rvTodayTasks = findViewById(R.id.rv_today_tasks);
        rvOverdueTasks = findViewById(R.id.rv_overdue_tasks);
        tvTodayHeader = findViewById(R.id.tv_today_header);
        tvOverdueHeader = findViewById(R.id.tv_overdue_header);
        layoutEmptyState = findViewById(R.id.layout_empty_state);

        // Setup RecyclerViews
        rvTodayTasks.setLayoutManager(new LinearLayoutManager(this));
        todayAdapter = new PlannerTaskAdapter(todayTasks, this);
        rvTodayTasks.setAdapter(todayAdapter);

        rvOverdueTasks.setLayoutManager(new LinearLayoutManager(this));
        overdueAdapter = new PlannerTaskAdapter(overdueTasks, this);
        rvOverdueTasks.setAdapter(overdueAdapter);

        // Load tasks
        loadTasks();

        // Add task button
        findViewById(R.id.btn_add_task).setOnClickListener(v -> showAddTaskDialog());
    }

    /**
     * Get start of today (midnight).
     */
    private long getTodayStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Load tasks from database.
     */
    private void loadTasks() {
        long todayStart = getTodayStart();

        // Today's tasks
        taskDao.getTodayTasks(todayStart).observe(this, tasks -> {
            todayTasks.clear();
            if (tasks != null) {
                todayTasks.addAll(tasks);
            }
            todayAdapter.notifyDataSetChanged();
            updateEmptyState();
        });

        // Overdue tasks
        taskDao.getOverdueTasks(todayStart).observe(this, tasks -> {
            overdueTasks.clear();
            if (tasks != null) {
                overdueTasks.addAll(tasks);
            }
            overdueAdapter.notifyDataSetChanged();

            // Show/hide overdue section
            boolean hasOverdue = !overdueTasks.isEmpty();
            tvOverdueHeader.setVisibility(hasOverdue ? View.VISIBLE : View.GONE);
            rvOverdueTasks.setVisibility(hasOverdue ? View.VISIBLE : View.GONE);

            updateEmptyState();
        });
    }

    /**
     * Update empty state visibility.
     */
    private void updateEmptyState() {
        boolean isEmpty = todayTasks.isEmpty() && overdueTasks.isEmpty();
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    /**
     * Show dialog to add new task.
     */
    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        // Create input field
        final EditText input = new EditText(this);
        input.setHint("Task title");
        input.setPadding(48, 32, 48, 32);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = input.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
                return;
            }
            showTimePickerForTask(title);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    /**
     * Show time picker for task due time.
     */
    private void showTimePickerForTask(String taskTitle) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            // Set due time to today at selected time
            Calendar dueDate = Calendar.getInstance();
            dueDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            dueDate.set(Calendar.MINUTE, minuteOfHour);
            dueDate.set(Calendar.SECOND, 59);
            dueDate.set(Calendar.MILLISECOND, 999);

            // If time is in the past, set to end of today (11:59:59 PM)
            if (dueDate.getTimeInMillis() < System.currentTimeMillis()) {
                dueDate.set(Calendar.HOUR_OF_DAY, 23);
                dueDate.set(Calendar.MINUTE, 59);
                dueDate.set(Calendar.SECOND, 59);
            }

            createTask(taskTitle, dueDate.getTimeInMillis());
        }, hour, minute, false);

        timePicker.setTitle("Set Due Time");
        timePicker.show();
    }

    /**
     * Create and save new task.
     */
    private void createTask(String title, long dueDate) {
        PlannerTask task = new PlannerTask(
                UUID.randomUUID().toString(),
                title,
                dueDate,
                false,
                System.currentTimeMillis());

        new Thread(() -> {
            taskDao.insert(task);
            runOnUiThread(() -> {
                Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    @Override
    public void onTaskChecked(PlannerTask task, boolean isChecked) {
        task.isCompleted = isChecked;
        new Thread(() -> taskDao.update(task)).start();
    }

    @Override
    public void onTaskDelete(PlannerTask task) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Delete \"" + task.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new Thread(() -> {
                        taskDao.delete(task);
                        runOnUiThread(() -> Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show());
                    }).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}