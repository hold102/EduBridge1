package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * M3.4 Module List Activity
 * Shows course modules with expandable lessons
 */
public class ModuleListActivity extends AppCompatActivity
        implements ModuleAdapter.OnLessonClickListener {

    private RecyclerView rvModules;
    private ModuleAdapter adapter;
    private TextView tvCourseTitle, tvProgress;
    private LinearProgressIndicator progressBar;

    private String courseId, courseTitle, courseCategory;
    private List<CourseModule> modules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_list);

        initViews();
        loadCourseData();
        loadModules();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvCourseTitle = findViewById(R.id.tv_course_title);
        tvProgress = findViewById(R.id.tv_progress);
        progressBar = findViewById(R.id.progress_bar);
        rvModules = findViewById(R.id.rv_modules);

        rvModules.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ModuleAdapter(this, modules, this);
        rvModules.setAdapter(adapter);
    }

    private void loadCourseData() {
        courseId = getIntent().getStringExtra("EXTRA_COURSE_ID");
        courseTitle = getIntent().getStringExtra("EXTRA_TITLE");
        courseCategory = getIntent().getStringExtra("EXTRA_CATEGORY");

        if (courseTitle != null) {
            tvCourseTitle.setText(courseTitle);
        }
    }

    private void loadModules() {
        // Generate sample modules based on category
        modules = generateSampleModules(courseCategory);

        // M3.4.2: Apply sequential progression
        applySequentialProgression();

        adapter.updateModules(modules);
        updateProgress();
    }

    /**
     * M3.4.2: Enforce sequential progression
     */
    private void applySequentialProgression() {
        boolean previousModuleCompleted = true;

        for (int i = 0; i < modules.size(); i++) {
            CourseModule module = modules.get(i);

            // First module is always unlocked
            if (i == 0) {
                module.setLocked(false);
            } else {
                module.setLocked(!previousModuleCompleted);
            }

            // Apply to lessons within module
            boolean previousLessonCompleted = true;
            for (int j = 0; j < module.getLessons().size(); j++) {
                Lesson lesson = module.getLessons().get(j);
                if (j == 0) {
                    lesson.setLocked(module.isLocked());
                } else {
                    lesson.setLocked(!previousLessonCompleted || module.isLocked());
                }
                previousLessonCompleted = lesson.isCompleted();
            }

            previousModuleCompleted = module.isCompleted();
        }
    }

    private void updateProgress() {
        int totalLessons = 0;
        int completedLessons = 0;

        for (CourseModule module : modules) {
            for (Lesson lesson : module.getLessons()) {
                totalLessons++;
                if (lesson.isCompleted())
                    completedLessons++;
            }
        }

        int percent = totalLessons > 0 ? (completedLessons * 100 / totalLessons) : 0;
        tvProgress.setText(percent + "% complete");
        progressBar.setProgress(percent);
    }

    @Override
    public void onLessonClick(CourseModule module, Lesson lesson) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("EXTRA_COURSE_ID", courseId);
        intent.putExtra("EXTRA_COURSE_TITLE", courseTitle);
        intent.putExtra("EXTRA_MODULE_ID", module.getModuleId());
        intent.putExtra("EXTRA_MODULE_TITLE", module.getTitle());
        intent.putExtra("EXTRA_LESSON_ID", lesson.getLessonId());
        intent.putExtra("EXTRA_LESSON_TITLE", lesson.getTitle());
        intent.putExtra("EXTRA_LESSON_TYPE", lesson.getContentType());
        intent.putExtra("EXTRA_LESSON_CONTENT", lesson.getTextContent());
        intent.putExtra("EXTRA_LESSON_URL", lesson.getContentUrl());
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Lesson completed, refresh
            String lessonId = data.getStringExtra("COMPLETED_LESSON_ID");
            markLessonCompleted(lessonId);
        }
    }

    private void markLessonCompleted(String lessonId) {
        for (CourseModule module : modules) {
            for (Lesson lesson : module.getLessons()) {
                if (lesson.getLessonId().equals(lessonId)) {
                    lesson.setCompleted(true);
                    lesson.setCompletedAt(System.currentTimeMillis());
                    break;
                }
            }
        }

        applySequentialProgression();
        adapter.notifyDataSetChanged();
        updateProgress();

        // Update enrollment progress
        updateEnrollmentProgress();
    }

    private void updateEnrollmentProgress() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null || courseId == null)
            return;

        int totalLessons = 0;
        int completedLessons = 0;
        for (CourseModule module : modules) {
            for (Lesson lesson : module.getLessons()) {
                totalLessons++;
                if (lesson.isCompleted())
                    completedLessons++;
            }
        }

        EnrollmentManager.updateProgress(userId, courseId, completedLessons, totalLessons, null);
    }

    // ===== Sample Data Generation =====

    private List<CourseModule> generateSampleModules(String category) {
        List<CourseModule> result = new ArrayList<>();

        if (category != null && category.contains("Math")) {
            result.add(createModule("m1", "Fundamentals", 0, Arrays.asList(
                    createLesson("l1", "Introduction to Numbers", Lesson.TYPE_TEXT, 5),
                    createLesson("l2", "Basic Operations Video", Lesson.TYPE_VIDEO, 10),
                    createLesson("l3", "Practice Quiz", Lesson.TYPE_QUIZ, 5))));
            result.add(createModule("m2", "Algebra Basics", 1, Arrays.asList(
                    createLesson("l4", "Variables Explained", Lesson.TYPE_TEXT, 8),
                    createLesson("l5", "Solving Equations", Lesson.TYPE_VIDEO, 15),
                    createLesson("l6", "Worksheet Download", Lesson.TYPE_DOWNLOAD, 0),
                    createLesson("l7", "Module Quiz", Lesson.TYPE_QUIZ, 10))));
            result.add(createModule("m3", "Advanced Topics", 2, Arrays.asList(
                    createLesson("l8", "Quadratic Functions", Lesson.TYPE_TEXT, 12),
                    createLesson("l9", "Graphing Tutorial", Lesson.TYPE_VIDEO, 20),
                    createLesson("l10", "Final Assessment", Lesson.TYPE_QUIZ, 15))));
        } else if (category != null && category.contains("Coding")) {
            result.add(createModule("m1", "Getting Started", 0, Arrays.asList(
                    createLesson("l1", "What is Programming?", Lesson.TYPE_TEXT, 5),
                    createLesson("l2", "Setting Up Your IDE", Lesson.TYPE_VIDEO, 10),
                    createLesson("l3", "Hello World Quiz", Lesson.TYPE_QUIZ, 3))));
            result.add(createModule("m2", "Core Concepts", 1, Arrays.asList(
                    createLesson("l4", "Variables and Types", Lesson.TYPE_TEXT, 10),
                    createLesson("l5", "Control Flow", Lesson.TYPE_TEXT, 12),
                    createLesson("l6", "Loops Tutorial", Lesson.TYPE_VIDEO, 15),
                    createLesson("l7", "Code Samples", Lesson.TYPE_DOWNLOAD, 0))));
        } else {
            // Default modules
            result.add(createModule("m1", "Introduction", 0, Arrays.asList(
                    createLesson("l1", "Welcome", Lesson.TYPE_TEXT, 3),
                    createLesson("l2", "Overview Video", Lesson.TYPE_VIDEO, 8),
                    createLesson("l3", "Pre-Assessment", Lesson.TYPE_QUIZ, 5))));
            result.add(createModule("m2", "Core Content", 1, Arrays.asList(
                    createLesson("l4", "Main Concepts", Lesson.TYPE_TEXT, 15),
                    createLesson("l5", "Deep Dive Video", Lesson.TYPE_VIDEO, 20),
                    createLesson("l6", "Resources", Lesson.TYPE_DOWNLOAD, 0))));
            result.add(createModule("m3", "Assessment", 2, Arrays.asList(
                    createLesson("l7", "Review", Lesson.TYPE_TEXT, 10),
                    createLesson("l8", "Final Quiz", Lesson.TYPE_QUIZ, 15))));
        }

        return result;
    }

    private CourseModule createModule(String id, String title, int order, List<Lesson> lessons) {
        CourseModule module = new CourseModule(id, courseId, title, "", order);
        module.setLessons(lessons);
        return module;
    }

    private Lesson createLesson(String id, String title, String type, int duration) {
        Lesson lesson = new Lesson(id, "", title, type, 0);
        lesson.setDurationMinutes(duration);

        // Add sample content
        if (type.equals(Lesson.TYPE_TEXT)) {
            lesson.setTextContent("This is sample content for: " + title +
                    "\n\nLorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                    "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.");
        } else if (type.equals(Lesson.TYPE_VIDEO)) {
            lesson.setContentUrl("https://example.com/video/" + id);
        } else if (type.equals(Lesson.TYPE_DOWNLOAD)) {
            lesson.setContentUrl("https://example.com/download/" + id + ".pdf");
        } else if (type.equals(Lesson.TYPE_QUIZ)) {
            lesson.setQuizQuestions(Arrays.asList(
                    new Lesson.QuizQuestion("Sample question?",
                            Arrays.asList("Option A", "Option B", "Option C", "Option D"), 0)));
        }

        return lesson;
    }
}
