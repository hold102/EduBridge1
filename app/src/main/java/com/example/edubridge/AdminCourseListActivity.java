package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.entity.Course;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin Course List Activity for M6.3 Content Management.
 * 
 * Features:
 * - A6.3.1: View list of courses
 * - A6.3.4: Filter by publish status
 */
public class AdminCourseListActivity extends AppCompatActivity implements AdminCourseAdapter.OnCourseClickListener {

    private RecyclerView recyclerCourses;
    private AdminCourseAdapter adapter;
    private TextView tvCourseCount;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private Chip chipAll, chipPublished, chipDrafts;
    private FloatingActionButton fabAddCourse;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_course_list);

        db = FirebaseFirestore.getInstance();

        // Find views
        recyclerCourses = findViewById(R.id.recycler_courses);
        tvCourseCount = findViewById(R.id.tv_course_count);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);

        chipAll = findViewById(R.id.chip_all);
        chipPublished = findViewById(R.id.chip_published);
        chipDrafts = findViewById(R.id.chip_drafts);
        fabAddCourse = findViewById(R.id.fab_add_course);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminCourseAdapter(this);
        recyclerCourses.setAdapter(adapter);

        // Filter chip listeners
        chipAll.setOnClickListener(v -> {
            adapter.setFilter("all");
            updateCourseCount();
        });
        chipPublished.setOnClickListener(v -> {
            adapter.setFilter("published");
            updateCourseCount();
        });
        chipDrafts.setOnClickListener(v -> {
            adapter.setFilter("drafts");
            updateCourseCount();
        });

        // FAB - Create new course
        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminCourseEditActivity.class);
            intent.putExtra("is_new", true);
            startActivity(intent);
        });

        // Load courses
        loadCourses();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
    }

    private void loadCourses() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        // First, load from local Room database
        List<AdminCourseAdapter.CourseItem> allCourses = new ArrayList<>();
        Set<String> courseIds = new HashSet<>();

        try {
            AppDatabase localDb = AppDatabase.getInstance(this);
            List<Course> localCourses = localDb.courseDao().getAllCoursesSync();
            if (localCourses != null) {
                for (Course c : localCourses) {
                    if (c.courseId != null && !courseIds.contains(c.courseId)) {
                        courseIds.add(c.courseId);
                        allCourses.add(new AdminCourseAdapter.CourseItem(
                                c.courseId, c.title, c.category, c.description, c.isPublished, c.totalLessons));
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("AdminCourseList", "Failed to load local courses", e);
        }

        // Then, load from Firestore and merge
        db.collection("courses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();

                        // Skip if already loaded from local database
                        if (courseIds.contains(id))
                            continue;

                        String title = doc.getString("title");
                        String category = doc.getString("category");
                        String description = doc.getString("description");

                        Boolean published = doc.getBoolean("isPublished");
                        boolean isPublished = published != null && published;

                        // Count modules or use totalLessons
                        List<Map<String, Object>> modules = (List<Map<String, Object>>) doc.get("modules");
                        int moduleCount = modules != null ? modules.size() : 0;
                        if (moduleCount == 0) {
                            Long totalLessons = doc.getLong("totalLessons");
                            moduleCount = totalLessons != null ? totalLessons.intValue() : 0;
                        }

                        courseIds.add(id);
                        allCourses.add(new AdminCourseAdapter.CourseItem(
                                id, title, category, description, isPublished, moduleCount));
                    }

                    adapter.setCourses(allCourses);
                    updateCourseCount();

                    if (allCourses.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    // Still show local courses even if Firestore fails
                    if (!allCourses.isEmpty()) {
                        adapter.setCourses(allCourses);
                        updateCourseCount();
                    } else {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                    android.util.Log.e("AdminCourseList", "Failed to load Firestore courses", e);
                });
    }

    private void updateCourseCount() {
        int count = adapter.getFilteredCount();
        tvCourseCount.setText(count + " course" + (count != 1 ? "s" : ""));
    }

    @Override
    public void onCourseClick(AdminCourseAdapter.CourseItem course) {
        // Preview course (A6.3.5)
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("courseId", course.id);
        intent.putExtra("preview_mode", true);
        startActivity(intent);
    }

    @Override
    public void onEditClick(AdminCourseAdapter.CourseItem course) {
        // Edit course (A6.3.1)
        Intent intent = new Intent(this, AdminCourseEditActivity.class);
        intent.putExtra("course_id", course.id);
        intent.putExtra("is_new", false);
        startActivity(intent);
    }
}
