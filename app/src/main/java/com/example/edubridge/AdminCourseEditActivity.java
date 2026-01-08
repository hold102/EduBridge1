package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin Course Edit Activity for M6.3 Content Management.
 * 
 * Features:
 * - A6.3.1: Create, edit, delete courses
 * - A6.3.2: Manage learning materials
 * - A6.3.3: Structure content into modules and lessons
 * - A6.3.4: Publish or unpublish courses
 * - A6.3.5: Preview content before publication
 */
public class AdminCourseEditActivity extends AppCompatActivity {

    private boolean isNewCourse = true;
    private String courseId;

    // Course detail fields - synced with Course entity
    private TextInputEditText etTitle, etDescription, etCategory;
    private TextInputEditText etDifficulty, etDuration, etTotalLessons;
    private TextInputEditText etObjectives, etPrerequisites;
    private TextInputEditText etInstructorName, etInstructorBio;
    private SwitchMaterial switchPublish;
    private LinearLayout layoutModules;
    private TextView tvNoModules, tvHeaderTitle;
    private MaterialButton btnSave, btnDelete, btnPreview, btnAddModule;

    private FirebaseFirestore db;

    // Store modules in memory
    private List<Map<String, Object>> modules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_course_edit);

        db = FirebaseFirestore.getInstance();

        // Get intent data
        isNewCourse = getIntent().getBooleanExtra("is_new", true);
        courseId = getIntent().getStringExtra("course_id");

        // Find views
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etCategory = findViewById(R.id.et_category);
        etDifficulty = findViewById(R.id.et_difficulty);
        etDuration = findViewById(R.id.et_duration);
        etTotalLessons = findViewById(R.id.et_total_lessons);
        etObjectives = findViewById(R.id.et_objectives);
        etPrerequisites = findViewById(R.id.et_prerequisites);
        etInstructorName = findViewById(R.id.et_instructor_name);
        etInstructorBio = findViewById(R.id.et_instructor_bio);
        switchPublish = findViewById(R.id.switch_publish);
        layoutModules = findViewById(R.id.layout_modules);
        tvNoModules = findViewById(R.id.tv_no_modules);
        tvHeaderTitle = findViewById(R.id.tv_header_title);
        btnSave = findViewById(R.id.btn_save);
        btnDelete = findViewById(R.id.btn_delete);
        btnPreview = findViewById(R.id.btn_preview);
        btnAddModule = findViewById(R.id.btn_add_module);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Configure UI based on new/edit mode
        if (isNewCourse) {
            tvHeaderTitle.setText("Create Course");
            btnDelete.setVisibility(View.GONE);
            btnPreview.setVisibility(View.GONE);
        } else {
            tvHeaderTitle.setText("Edit Course");
            btnDelete.setVisibility(View.VISIBLE);
            btnPreview.setVisibility(View.VISIBLE);
            loadCourse();
        }

        // Button listeners
        btnSave.setOnClickListener(v -> saveCourse());
        btnDelete.setOnClickListener(v -> confirmDeleteCourse());
        btnPreview.setOnClickListener(v -> previewCourse());
        btnAddModule.setOnClickListener(v -> showAddModuleDialog());
    }

    private void loadCourse() {
        if (courseId == null)
            return;

        // Try to load from local DB first
        new Thread(() -> {
            com.example.edubridge.data.local.entity.Course localCourse = com.example.edubridge.data.local.AppDatabase
                    .getInstance(this).courseDao().getCourseByIdSync(courseId);

            if (localCourse != null) {
                runOnUiThread(() -> populateCourseFields(localCourse));
            } else {
                // If not local, try Firestore
                runOnUiThread(() -> loadFromFirestore());
            }
        }).start();
    }

    private void loadFromFirestore() {
        db.collection("courses").document(courseId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etTitle.setText(doc.getString("title"));
                        etDescription.setText(doc.getString("description"));
                        etCategory.setText(doc.getString("category"));
                        etDifficulty.setText(doc.getString("difficulty"));
                        etDuration.setText(doc.getString("duration"));

                        Long totalLessons = doc.getLong("totalLessons");
                        if (totalLessons != null) {
                            etTotalLessons.setText(String.valueOf(totalLessons));
                        }

                        etObjectives.setText(doc.getString("objectives"));
                        etPrerequisites.setText(doc.getString("prerequisites"));
                        etInstructorName.setText(doc.getString("instructorName"));
                        etInstructorBio.setText(doc.getString("instructorBio"));

                        Boolean published = doc.getBoolean("isPublished");
                        switchPublish.setChecked(published != null && published);

                        // Load modules
                        List<Map<String, Object>> loadedModules = (List<Map<String, Object>>) doc.get("modules");
                        if (loadedModules != null) {
                            modules = new ArrayList<>(loadedModules);
                            refreshModulesUI();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading course: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateCourseFields(com.example.edubridge.data.local.entity.Course course) {
        etTitle.setText(course.title);
        etDescription.setText(course.description);
        etCategory.setText(course.category);
        etDifficulty.setText(course.difficulty);
        etDuration.setText(course.duration);
        etTotalLessons.setText(String.valueOf(course.totalLessons));
        etObjectives.setText(course.objectives);
        etPrerequisites.setText(course.prerequisites);
        etInstructorName.setText(course.instructorName);
        etInstructorBio.setText(course.instructorBio);
        switchPublish.setChecked(course.isPublished);

        // Note: Modules are not currently stored in local Course entity as a structured
        // list suitable for editing
        // Attempt to fetch fresh structure from Firestore for modules if possible,
        // otherwise start empty
        db.collection("courses").document(courseId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        List<Map<String, Object>> loadedModules = (List<Map<String, Object>>) doc.get("modules");
                        if (loadedModules != null) {
                            modules = new ArrayList<>(loadedModules);
                            refreshModulesUI();
                        }
                    }
                });
    }

    private void saveCourse() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
        String difficulty = etDifficulty.getText() != null ? etDifficulty.getText().toString().trim() : "";
        String duration = etDuration.getText() != null ? etDuration.getText().toString().trim() : "";
        String totalLessonsStr = etTotalLessons.getText() != null ? etTotalLessons.getText().toString().trim() : "0";
        String objectives = etObjectives.getText() != null ? etObjectives.getText().toString().trim() : "";
        String prerequisites = etPrerequisites.getText() != null ? etPrerequisites.getText().toString().trim() : "";
        String instructorName = etInstructorName.getText() != null ? etInstructorName.getText().toString().trim() : "";
        String instructorBio = etInstructorBio.getText() != null ? etInstructorBio.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Please enter a course title", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalLessons = 0;
        try {
            totalLessons = Integer.parseInt(totalLessonsStr);
        } catch (NumberFormatException e) {
            // Ignore, use 0
        }

        Map<String, Object> course = new HashMap<>();
        course.put("title", title);
        course.put("description", description);
        course.put("category", category);
        course.put("difficulty", difficulty);
        course.put("duration", duration);
        course.put("totalLessons", totalLessons);
        course.put("objectives", objectives);
        course.put("prerequisites", prerequisites);
        course.put("instructorName", instructorName);
        course.put("instructorBio", instructorBio);
        course.put("isPublished", switchPublish.isChecked());
        course.put("modules", modules);
        course.put("updatedAt", Timestamp.now());

        if (isNewCourse) {
            course.put("createdAt", Timestamp.now());

            db.collection("courses").add(course)
                    .addOnSuccessListener(ref -> {
                        courseId = ref.getId();
                        isNewCourse = false;
                        Toast.makeText(this, "Course created!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Use set() instead of update() to handle case where course exists locally but
            // not in Firestore
            // (e.g., promoted sample courses)
            db.collection("courses").document(courseId).set(course)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Course saved!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void confirmDeleteCourse() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Course?")
                .setMessage("This will permanently delete this course. This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteCourse())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCourse() {
        if (courseId == null)
            return;

        db.collection("courses").document(courseId).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Course deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Preview course (A6.3.5).
     */
    private void previewCourse() {
        if (courseId == null) {
            Toast.makeText(this, "Save the course first to preview", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("courseId", courseId);
        intent.putExtra("preview_mode", true);
        startActivity(intent);
    }

    // ===== Module Management (A6.3.3) =====

    private void showAddModuleDialog() {
        EditText input = new EditText(this);
        input.setHint("Module title (e.g., 'Introduction')");
        input.setPadding(48, 32, 48, 32);

        new AlertDialog.Builder(this)
                .setTitle("Add Module")
                .setView(input)
                .setPositiveButton("Add", (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        addModule(title);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addModule(String title) {
        Map<String, Object> module = new HashMap<>();
        module.put("id", "module_" + UUID.randomUUID().toString().substring(0, 8));
        module.put("title", title);
        module.put("lessons", new ArrayList<Map<String, Object>>());

        modules.add(module);
        refreshModulesUI();
    }

    private void refreshModulesUI() {
        layoutModules.removeAllViews();

        if (modules.isEmpty()) {
            tvNoModules.setVisibility(View.VISIBLE);
            layoutModules.addView(tvNoModules);
            return;
        }

        tvNoModules.setVisibility(View.GONE);

        for (int i = 0; i < modules.size(); i++) {
            Map<String, Object> module = modules.get(i);
            View moduleView = createModuleView(module, i);
            layoutModules.addView(moduleView);
        }
    }

    private View createModuleView(Map<String, Object> module, int index) {
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 24);

        // Module header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setBackgroundColor(0xFFF5F5F5);
        header.setPadding(16, 12, 16, 12);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("📁 " + (index + 1) + ". " + module.get("title"));
        tvTitle.setTextSize(14);
        tvTitle.setTextColor(0xFF212121);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvTitle.setLayoutParams(titleParams);

        TextView btnAddLesson = new TextView(this);
        btnAddLesson.setText("+ Lesson");
        btnAddLesson.setTextSize(12);
        btnAddLesson.setTextColor(0xFF2196F3);
        btnAddLesson.setPadding(16, 8, 16, 8);
        btnAddLesson.setOnClickListener(v -> showAddLessonDialog(index));

        TextView btnRemove = new TextView(this);
        btnRemove.setText("✕");
        btnRemove.setTextSize(14);
        btnRemove.setTextColor(0xFFD32F2F);
        btnRemove.setPadding(16, 8, 8, 8);
        btnRemove.setOnClickListener(v -> {
            modules.remove(index);
            refreshModulesUI();
        });

        header.addView(tvTitle);
        header.addView(btnAddLesson);
        header.addView(btnRemove);
        container.addView(header);

        // Lessons
        List<Map<String, Object>> lessons = (List<Map<String, Object>>) module.get("lessons");
        if (lessons != null && !lessons.isEmpty()) {
            for (int j = 0; j < lessons.size(); j++) {
                Map<String, Object> lesson = lessons.get(j);
                View lessonView = createLessonView(lesson, index, j);
                container.addView(lessonView);
            }
        }

        return container;
    }

    private View createLessonView(Map<String, Object> lesson, int moduleIndex, int lessonIndex) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(32, 8, 16, 8);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);

        String type = (String) lesson.get("type");
        String icon = "📄";
        if ("video".equals(type))
            icon = "🎬";
        else if ("quiz".equals(type))
            icon = "📝";

        TextView tvLesson = new TextView(this);
        tvLesson.setText(icon + " " + lesson.get("title"));
        tvLesson.setTextSize(13);
        tvLesson.setTextColor(0xFF424242);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        tvLesson.setLayoutParams(params);

        TextView btnRemove = new TextView(this);
        btnRemove.setText("✕");
        btnRemove.setTextSize(12);
        btnRemove.setTextColor(0xFFD32F2F);
        btnRemove.setPadding(16, 4, 4, 4);
        btnRemove.setOnClickListener(v -> {
            List<Map<String, Object>> lessons = (List<Map<String, Object>>) modules.get(moduleIndex).get("lessons");
            if (lessons != null) {
                lessons.remove(lessonIndex);
                refreshModulesUI();
            }
        });

        row.addView(tvLesson);
        row.addView(btnRemove);
        return row;
    }

    private void showAddLessonDialog(int moduleIndex) {
        View dialogView = LayoutInflater.from(this).inflate(
                android.R.layout.simple_list_item_2, null);

        EditText input = new EditText(this);
        input.setHint("Lesson title");
        input.setPadding(48, 32, 48, 32);

        String[] types = { "Document", "Video", "Quiz" };
        final int[] selectedType = { 0 };

        new AlertDialog.Builder(this)
                .setTitle("Add Lesson")
                .setView(input)
                .setSingleChoiceItems(types, 0, (d, which) -> selectedType[0] = which)
                .setPositiveButton("Add", (d, w) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        String type = selectedType[0] == 1 ? "video" : selectedType[0] == 2 ? "quiz" : "document";
                        addLesson(moduleIndex, title, type);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addLesson(int moduleIndex, String title, String type) {
        Map<String, Object> lesson = new HashMap<>();
        lesson.put("id", "lesson_" + UUID.randomUUID().toString().substring(0, 8));
        lesson.put("title", title);
        lesson.put("type", type);
        lesson.put("contentUrl", "");

        List<Map<String, Object>> lessons = (List<Map<String, Object>>) modules.get(moduleIndex).get("lessons");
        if (lessons == null) {
            lessons = new ArrayList<>();
            modules.get(moduleIndex).put("lessons", lessons);
        }
        lessons.add(lesson);

        refreshModulesUI();
    }
}
