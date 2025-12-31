package com.example.edubridge;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.utils.EnrollmentManager;
import com.example.edubridge.utils.StructuredProgressManager;
import com.google.android.material.button.MaterialButton;

import java.util.Map;

/**
 * CourseDetailActivity
 *
 * - M3.2: Course preview & details
 * - M3.3: Enrollment management + dashboard progress
 * - M3.4: Structured content (Module -> Lesson -> Activity),
 *         progressive unlock, offline persistence
 */
public class CourseDetailActivity extends AppCompatActivity {

    private static final int TOTAL_LESSONS = 2;

    private String courseTitle;
    private MaterialButton btnEnroll;

    private TextView tvChapter1;
    private TextView tvChapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // ===== Basic views =====
        TextView tvTitle = findViewById(R.id.tv_course_title);
        TextView tvDesc = findViewById(R.id.tv_course_desc);
        ImageView imgIcon = findViewById(R.id.img_course_icon);
        btnEnroll = findViewById(R.id.btn_start_course);

        // ===== Detail views =====
        TextView tvInstructor = findViewById(R.id.tv_instructor);
        TextView tvDuration = findViewById(R.id.tv_duration);
        TextView tvRating = findViewById(R.id.tv_rating);
        TextView tvPrerequisites = findViewById(R.id.tv_prerequisites);
        tvChapter1 = findViewById(R.id.tv_chapter_1);
        tvChapter2 = findViewById(R.id.tv_chapter_2);
        RatingBar ratingBarUser = findViewById(R.id.ratingBar_user);

        // ===== Intent data =====
        courseTitle = getIntent().getStringExtra("EXTRA_TITLE");
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        int iconResId = getIntent().getIntExtra(
                "EXTRA_ICON",
                R.drawable.ic_subject_math
        );

        tvTitle.setText(courseTitle);
        tvDesc.setText(desc);
        imgIcon.setImageResource(iconResId);

        // ===== Static preview info =====
        tvInstructor.setText("Instructor: Dr. Smith");
        tvDuration.setText("Duration: 8 weeks");
        tvRating.setText("⭐ 4.6 (1,200 learners)");
        tvPrerequisites.setText("• Basic understanding of the subject");

        // ===== Dynamic syllabus titles =====
        if (courseTitle.contains("Computer")) {
            tvChapter1.setText("Variables & Data Types");
            tvChapter2.setText("Loops & Conditionals");
        } else if (courseTitle.contains("Geography")) {
            tvChapter1.setText("Map Reading & Coordinates");
            tvChapter2.setText("Plate Tectonics");
        } else if (courseTitle.contains("Physics")) {
            tvChapter1.setText("Newton's Laws of Motion");
            tvChapter2.setText("The Periodic Table");
        } else if (courseTitle.contains("Art")) {
            tvChapter1.setText("Color Theory Basics");
            tvChapter2.setText("Perspective Drawing");
        } else {
            tvChapter1.setText("Introduction to Numbers");
            tvChapter2.setText("Linear Equations");
        }

        ratingBarUser.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(
                        this,
                        "Thanks for rating " + rating + " stars!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // ===== Lesson click handling =====
        tvChapter1.setOnClickListener(v -> handleLesson(1));

        tvChapter2.setOnClickListener(v -> {
            if (StructuredProgressManager.isLessonCompleted(this, courseTitle, 1)) {
                handleLesson(2);
            }
        });

        refreshEnrollButton();
        updateLessonUnlockState();

        // ===== Enroll / Resume button =====
        btnEnroll.setOnClickListener(v -> {
            if (!EnrollmentManager.isEnrolled(this, courseTitle)) {
                EnrollmentManager.enroll(this, courseTitle);
                Toast.makeText(this, "Successfully enrolled!", Toast.LENGTH_SHORT).show();
            } else {
                handleLesson(getNextIncompleteLesson());
            }
            refreshEnrollButton();
            updateLessonUnlockState();
        });

        // ===== Long press: Unenroll =====
        btnEnroll.setOnLongClickListener(v -> {
            EnrollmentManager.unenroll(this, courseTitle);
            clearCourseProgress();
            Toast.makeText(this, "Unenrolled from course", Toast.LENGTH_SHORT).show();
            refreshEnrollButton();
            updateLessonUnlockState();
            return true;
        });
    }

    /**
     * Handles lesson activity based on lesson index.
     */
    private void handleLesson(int lessonIndex) {
        if (lessonIndex == 1) {
            // TEXT-based activity
            completeLesson(lessonIndex);
        } else if (lessonIndex == 2) {
            // QUIZ-based activity
            showQuizDialog(lessonIndex);
        }
    }

    /**
     * Course-specific quiz dialog.
     */
    private void showQuizDialog(int lessonIndex) {

        String question;
        String correctAnswer;

        if (courseTitle.contains("Computer")) {
            question = "Is Java an object-oriented language?";
            correctAnswer = "Yes";
        } else if (courseTitle.contains("Mathematics")) {
            question = "Is calculus a branch of mathematics?";
            correctAnswer = "Yes";
        } else if (courseTitle.contains("Geography")) {
            question = "Does geography study Earth's physical features?";
            correctAnswer = "Yes";
        } else if (courseTitle.contains("Physics")) {
            question = "Is force measured in Newtons?";
            correctAnswer = "Yes";
        } else {
            question = "Is art a creative discipline?";
            correctAnswer = "Yes";
        }

        new AlertDialog.Builder(this)
                .setTitle("Quiz")
                .setMessage(question)
                .setPositiveButton(correctAnswer,
                        (d, w) -> completeLesson(lessonIndex))
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Marks a lesson as completed and syncs progress to dashboard.
     */
    private void completeLesson(int lessonIndex) {

        StructuredProgressManager.markLessonCompleted(
                this, courseTitle, lessonIndex);

        int completed =
                StructuredProgressManager.getCompletedLessons(
                        this, courseTitle, TOTAL_LESSONS);

        int progress = (completed * 100) / TOTAL_LESSONS;

        EnrollmentManager.updateProgress(this, courseTitle, progress);

        Toast.makeText(
                this,
                "Lesson completed (" + progress + "%)",
                Toast.LENGTH_SHORT
        ).show();

        refreshEnrollButton();
        updateLessonUnlockState();
    }

    /**
     * Updates visual lock/unlock state of lessons.
     */
    private void updateLessonUnlockState() {
        tvChapter1.setAlpha(1f);

        if (StructuredProgressManager.isLessonCompleted(this, courseTitle, 1)) {
            tvChapter2.setAlpha(1f);
        } else {
            tvChapter2.setAlpha(0.4f);
        }
    }

    /**
     * Finds the next incomplete lesson.
     */
    private int getNextIncompleteLesson() {
        for (int i = 1; i <= TOTAL_LESSONS; i++) {
            if (!StructuredProgressManager.isLessonCompleted(this, courseTitle, i)) {
                return i;
            }
        }
        return TOTAL_LESSONS;
    }

    /**
     * Clears all structured progress for this course (used on unenroll).
     */
    private void clearCourseProgress() {
        for (int i = 1; i <= TOTAL_LESSONS; i++) {
            getSharedPreferences("structured_content_progress", MODE_PRIVATE)
                    .edit()
                    .remove(courseTitle + "_lesson_" + i)
                    .apply();
        }
    }

    /**
     * Updates main button text.
     */
    private void refreshEnrollButton() {
        if (EnrollmentManager.isEnrolled(this, courseTitle)) {

            int completed =
                    StructuredProgressManager.getCompletedLessons(
                            this, courseTitle, TOTAL_LESSONS);

            int progress = (completed * 100) / TOTAL_LESSONS;

            btnEnroll.setText(
                    progress < 100
                            ? "Resume Learning (" + progress + "%)"
                            : "Completed"
            );
        } else {
            btnEnroll.setText("Enroll");
        }
    }
}
