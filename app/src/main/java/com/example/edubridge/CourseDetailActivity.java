package com.example.edubridge;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * M3.2 Course Details Activity
 * M3.3 Enrollment Management
 * 
 * Features:
 * - M3.2.1-5: Display course details
 * - M3.3.1: Enroll in courses
 * - M3.3.2: Prevent duplicate enrollment
 * - M3.3.3: Unenroll from courses
 * - M3.3.4: Record timestamps
 * - M3.3.5: Sync to dashboard
 */
public class CourseDetailActivity extends AppCompatActivity {

    private static final String TAG = "CourseDetail";

    // Views
    private ImageView imgThumbnail;
    private TextView tvTitle, tvDesc, tvCategory, tvDifficulty;
    private TextView tvDuration, tvLessons, tvEffort;
    private TextView tvInstructorName, tvInstructorBio;
    private TextView tvObjectives, tvPrerequisites;
    private TextView tvEnrollmentStatus, tvProgressPercent, tvEnrolledDate;
    private LinearProgressIndicator progressBar;
    private MaterialCardView cardEnrollment, cardNotEnrolled, cardPrerequisites;
    private MaterialButton btnContinue, btnEnroll, btnUnenroll;
    private RecyclerView rvSyllabus;

    private SyllabusAdapter syllabusAdapter;
    private FirebaseFirestore db;
    private String courseId, courseTitle, courseCategory;
    private String userId;
    private int totalLessons;
    private EnrollmentManager.EnrollmentStatus enrollmentStatus;
    private int userProgress = 0;
    private long enrolledAt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getUid();

        initViews();
        loadCourseData();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Course info views
        imgThumbnail = findViewById(R.id.img_thumbnail);
        tvTitle = findViewById(R.id.tv_course_title);
        tvDesc = findViewById(R.id.tv_course_desc);
        tvCategory = findViewById(R.id.tv_category);
        tvDifficulty = findViewById(R.id.tv_difficulty);
        tvDuration = findViewById(R.id.tv_duration);
        tvLessons = findViewById(R.id.tv_lessons);
        tvEffort = findViewById(R.id.tv_effort);

        // Instructor views
        tvInstructorName = findViewById(R.id.tv_instructor_name);
        tvInstructorBio = findViewById(R.id.tv_instructor_bio);

        // Objectives and prerequisites
        tvObjectives = findViewById(R.id.tv_objectives);
        tvPrerequisites = findViewById(R.id.tv_prerequisites);
        cardPrerequisites = findViewById(R.id.card_prerequisites);

        // Enrollment views
        cardEnrollment = findViewById(R.id.card_enrollment);
        cardNotEnrolled = findViewById(R.id.card_not_enrolled);
        tvEnrollmentStatus = findViewById(R.id.tv_enrollment_status);
        tvProgressPercent = findViewById(R.id.tv_progress_percent);
        tvEnrolledDate = findViewById(R.id.tv_enrolled_date);
        progressBar = findViewById(R.id.progress_bar);
        btnContinue = findViewById(R.id.btn_continue);
        btnEnroll = findViewById(R.id.btn_enroll);
        btnUnenroll = findViewById(R.id.btn_unenroll);

        // Syllabus
        rvSyllabus = findViewById(R.id.rv_syllabus);
        rvSyllabus.setLayoutManager(new LinearLayoutManager(this));

        // Button listeners
        btnContinue.setOnClickListener(v -> startLearning());
        btnEnroll.setOnClickListener(v -> enrollInCourse());
        btnUnenroll.setOnClickListener(v -> confirmUnenroll());
    }

    private void loadCourseData() {
        // Get data from intent
        courseId = getIntent().getStringExtra("EXTRA_COURSE_ID");
        courseTitle = getIntent().getStringExtra("EXTRA_TITLE");
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        courseCategory = getIntent().getStringExtra("EXTRA_CATEGORY");
        String difficulty = getIntent().getStringExtra("EXTRA_DIFFICULTY");
        String duration = getIntent().getStringExtra("EXTRA_DURATION");
        totalLessons = getIntent().getIntExtra("EXTRA_LESSONS", 0);

        // Set basic info
        if (courseTitle != null)
            tvTitle.setText(courseTitle);
        if (desc != null)
            tvDesc.setText(desc);
        if (courseCategory != null)
            tvCategory.setText(courseCategory);
        if (difficulty != null) {
            tvDifficulty.setText(difficulty);
            setDifficultyColor(difficulty);
        }
        if (duration != null)
            tvDuration.setText(duration);
        tvLessons.setText(String.valueOf(totalLessons));

        // Set thumbnail
        setThumbnail(courseCategory);

        // Load detailed info
        loadDetailedInfo(courseTitle, courseCategory);

        // Check enrollment using EnrollmentManager
        checkEnrollmentStatus();
    }

    private void loadDetailedInfo(String title, String category) {
        // Sample data based on category
        String objectives, instructor, instructorBio, effort, prerequisites;
        List<String> syllabus;

        if (category != null && category.contains("Math")) {
            objectives = "• Master algebraic expressions and equations\n• Solve real-world math problems\n• Build strong foundation for advanced math";
            instructor = "Dr. Sarah Chen";
            instructorBio = "PhD in Mathematics, 10+ years teaching experience";
            effort = "3-4 hrs/week";
            prerequisites = "Basic arithmetic knowledge";
            syllabus = Arrays.asList("Introduction to Variables", "Linear Equations",
                    "Quadratic Functions", "Graphing Techniques", "Word Problems");
        } else if (category != null && category.contains("Science")) {
            objectives = "• Understand fundamental scientific concepts\n• Apply scientific method\n• Develop critical thinking skills";
            instructor = "Prof. James Wilson";
            instructorBio = "PhD Physics, Former NASA researcher";
            effort = "4-5 hrs/week";
            prerequisites = "Basic math skills recommended";
            syllabus = Arrays.asList("Scientific Method", "Forces and Motion",
                    "Energy and Work", "Waves and Sound", "Light and Optics");
        } else if (category != null && category.contains("Coding")) {
            objectives = "• Write clean, efficient code\n• Understand programming fundamentals\n• Build real-world applications";
            instructor = "Alex Kumar";
            instructorBio = "Senior Software Engineer at Google";
            effort = "5-6 hrs/week";
            prerequisites = "No prior experience needed";
            syllabus = Arrays.asList("Getting Started", "Variables and Data Types",
                    "Control Flow", "Functions", "OOP", "Building Your First App");
        } else {
            objectives = "• Understand core concepts\n• Apply knowledge practically\n• Achieve learning goals";
            instructor = "EduBridge Team";
            instructorBio = "Expert instructors";
            effort = "2-3 hrs/week";
            prerequisites = null;
            syllabus = Arrays.asList("Introduction", "Core Concepts",
                    "Practical Applications", "Advanced Topics", "Final Project");
        }

        tvObjectives.setText(objectives);
        tvInstructorName.setText(instructor);
        tvInstructorBio.setText(instructorBio);
        tvEffort.setText(effort);

        if (prerequisites != null && !prerequisites.isEmpty()) {
            tvPrerequisites.setText(prerequisites);
            cardPrerequisites.setVisibility(View.VISIBLE);
            findViewById(R.id.tv_prereq_label).setVisibility(View.VISIBLE);
        } else {
            cardPrerequisites.setVisibility(View.GONE);
            findViewById(R.id.tv_prereq_label).setVisibility(View.GONE);
        }

        syllabusAdapter = new SyllabusAdapter(this, syllabus, userProgress);
        rvSyllabus.setAdapter(syllabusAdapter);
    }

    // ===== M3.3 Enrollment Management =====

    private void checkEnrollmentStatus() {
        EnrollmentManager.checkEnrollmentStatus(userId, courseId,
                (status, progress, enrolledAtTime) -> {
                    this.enrollmentStatus = status;
                    this.userProgress = progress;
                    this.enrolledAt = enrolledAtTime;

                    if (status == EnrollmentManager.EnrollmentStatus.NOT_ENROLLED) {
                        showNotEnrolled();
                    } else {
                        showEnrolled();
                    }
                });
    }

    private void showEnrolled() {
        cardEnrollment.setVisibility(View.VISIBLE);
        cardNotEnrolled.setVisibility(View.GONE);

        int percent = totalLessons > 0 ? (userProgress * 100 / totalLessons) : 0;
        tvProgressPercent.setText(percent + "%");
        progressBar.setProgress(percent);

        // M3.3.4: Show enrolled timestamp
        if (enrolledAt > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            tvEnrolledDate.setText("Enrolled on: " + sdf.format(new Date(enrolledAt)));
            tvEnrolledDate.setVisibility(View.VISIBLE);
        } else {
            tvEnrolledDate.setVisibility(View.GONE);
        }

        // Update status text and button
        if (enrollmentStatus == EnrollmentManager.EnrollmentStatus.COMPLETED) {
            tvEnrollmentStatus.setText("Completed!");
            btnContinue.setText("Review Course");
            btnUnenroll.setVisibility(View.GONE); // M3.3.3: Can't unenroll completed
        } else if (percent > 0) {
            tvEnrollmentStatus.setText("In Progress");
            btnContinue.setText("Continue Learning");
            btnUnenroll.setVisibility(View.VISIBLE);
        } else {
            tvEnrollmentStatus.setText("Enrolled");
            btnContinue.setText("Start Learning");
            btnUnenroll.setVisibility(View.VISIBLE);
        }

        if (syllabusAdapter != null) {
            syllabusAdapter.notifyDataSetChanged();
        }
    }

    private void showNotEnrolled() {
        cardEnrollment.setVisibility(View.GONE);
        cardNotEnrolled.setVisibility(View.VISIBLE);
    }

    // M3.3.1: Enroll in course
    private void enrollInCourse() {
        if (userId == null) {
            Toast.makeText(this, "Please log in to enroll", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        btnEnroll.setEnabled(false);
        btnEnroll.setText("Enrolling...");

        EnrollmentManager.enrollInCourse(userId, courseId, courseTitle,
                courseCategory, totalLessons,
                new EnrollmentManager.EnrollmentCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(CourseDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            enrollmentStatus = EnrollmentManager.EnrollmentStatus.ENROLLED;
                            userProgress = 0;
                            enrolledAt = System.currentTimeMillis();
                            showEnrolled();
                            btnEnroll.setEnabled(true);
                            btnEnroll.setText("Enroll Now");
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(CourseDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                            btnEnroll.setEnabled(true);
                            btnEnroll.setText("Enroll Now");
                        });
                    }
                });
    }

    // M3.3.3: Unenroll with confirmation
    private void confirmUnenroll() {
        new AlertDialog.Builder(this)
                .setTitle("Unenroll from Course")
                .setMessage(
                        "Are you sure you want to unenroll from \"" + courseTitle + "\"? Your progress will be lost.")
                .setPositiveButton("Unenroll", (dialog, which) -> unenrollFromCourse())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void unenrollFromCourse() {
        if (userId == null)
            return;

        btnUnenroll.setEnabled(false);

        EnrollmentManager.unenrollFromCourse(userId, courseId,
                new EnrollmentManager.EnrollmentCallback() {
                    @Override
                    public void onSuccess(String message) {
                        runOnUiThread(() -> {
                            Toast.makeText(CourseDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                            enrollmentStatus = EnrollmentManager.EnrollmentStatus.NOT_ENROLLED;
                            userProgress = 0;
                            showNotEnrolled();
                            btnUnenroll.setEnabled(true);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(CourseDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                            btnUnenroll.setEnabled(true);
                        });
                    }
                });
    }

    private void startLearning() {
        // M3.4: Launch structured content with modules and lessons
        Intent intent = new Intent(this, ModuleListActivity.class);
        intent.putExtra("EXTRA_COURSE_ID", courseId);
        intent.putExtra("EXTRA_TITLE", courseTitle);
        intent.putExtra("EXTRA_CATEGORY", courseCategory);
        startActivity(intent);
    }

    private void setDifficultyColor(String difficulty) {
        int color;
        switch (difficulty.toLowerCase()) {
            case "beginner":
                color = 0xFF4CAF50;
                break;
            case "intermediate":
                color = 0xFFFF9800;
                break;
            case "advanced":
                color = 0xFFF44336;
                break;
            default:
                color = 0xFF4CAF50;
        }
        tvDifficulty.getBackground().setTint(color);
    }

    private void setThumbnail(String category) {
        if (category == null) {
            imgThumbnail.setImageResource(R.drawable.ic_nav_library);
            return;
        }
        int resId;
        switch (category.toLowerCase()) {
            case "mathematics":
                resId = R.drawable.ic_subject_math;
                break;
            case "science":
                resId = R.drawable.ic_subject_science;
                break;
            case "coding":
                resId = R.drawable.ic_subject_coding;
                break;
            case "art":
                resId = R.drawable.ic_subject_art;
                break;
            case "geography":
                resId = R.drawable.ic_subject_geo;
                break;
            default:
                resId = R.drawable.ic_nav_library;
        }
        imgThumbnail.setImageResource(resId);
    }
}