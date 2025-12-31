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

/**
 * CourseDetailActivity
 *
 * M3.2: Course details & preview (no enrollment required)
 * M3.3: Enrollment / Unenrollment + dashboard progress
 * M3.4: Structured content (Module -> Lesson -> Activity),
 *       sequential unlock + offline persistence
 */
public class CourseDetailActivity extends AppCompatActivity {

    private static final int TOTAL_LESSONS = 2;

    private String courseTitle;

    private MaterialButton btnStart;
    private MaterialButton btnCancel;

    private TextView tvLesson1;
    private TextView tvLesson2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // ===== Views =====
        TextView tvTitle = findViewById(R.id.tv_course_title);
        TextView tvDesc = findViewById(R.id.tv_course_desc);
        ImageView imgIcon = findViewById(R.id.img_course_icon);

        btnStart = findViewById(R.id.btn_start_course);
        btnCancel = findViewById(R.id.btn_cancel_course);

        tvLesson1 = findViewById(R.id.tv_chapter_1);
        tvLesson2 = findViewById(R.id.tv_chapter_2);

        RatingBar ratingBar = findViewById(R.id.ratingBar_user);

        // ===== Intent Data =====
        courseTitle = getIntent().getStringExtra("EXTRA_TITLE");
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        int iconRes = getIntent().getIntExtra("EXTRA_ICON", R.drawable.ic_subject_math);

        tvTitle.setText(courseTitle);
        tvDesc.setText(desc);
        imgIcon.setImageResource(iconRes);

        // ===== Rating (preview only) =====
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(this, "Thanks for rating!", Toast.LENGTH_SHORT).show();
            }
        });

        // ===== Lesson Clicks =====
        tvLesson1.setOnClickListener(v -> openLesson(1));
        tvLesson2.setOnClickListener(v -> openLesson(2));

        // ===== Main Button =====
        btnStart.setOnClickListener(v -> {
            if (!EnrollmentManager.isEnrolled(this, courseTitle)) {
                EnrollmentManager.enroll(this, courseTitle);
                Toast.makeText(this, "Enrolled successfully", Toast.LENGTH_SHORT).show();
            } else {
                openLesson(getNextLesson());
            }
            refreshUI();
        });

        // ===== Cancel Button =====
        btnCancel.setOnClickListener(v -> {
            EnrollmentManager.unenroll(this, courseTitle);
            StructuredProgressManager.clearCourse(this, courseTitle, TOTAL_LESSONS);
            Toast.makeText(this, "Course cancelled", Toast.LENGTH_SHORT).show();
            refreshUI();
        });

        refreshUI();
    }

    /**
     * Open lesson with rule checks
     */
    private void openLesson(int lessonIndex) {

        if (!EnrollmentManager.isEnrolled(this, courseTitle)) {
            Toast.makeText(this, "Please enroll before learning", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lessonIndex == 2 &&
                !StructuredProgressManager.isLessonCompleted(this, courseTitle, 1)) {
            Toast.makeText(this, "Complete Lesson 1 first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (lessonIndex == 1) {
            completeLesson(1);
        } else {
            showQuizDialog();
        }
    }

    /**
     * Quiz activity (simple Yes / No)
     */
    private void showQuizDialog() {

        new AlertDialog.Builder(this)
                .setTitle("Quiz")
                .setMessage("Is this course related to your major?")
                .setPositiveButton("Yes", (d, w) -> completeLesson(2))
                .setNegativeButton("No", (d, w) ->
                        Toast.makeText(this, "Incorrect, try again", Toast.LENGTH_SHORT).show())
                .show();
    }

    /**
     * Complete lesson and update progress
     */
    private void completeLesson(int lessonIndex) {

        StructuredProgressManager.markLessonCompleted(this, courseTitle, lessonIndex);

        int completed =
                StructuredProgressManager.getCompletedLessons(this, courseTitle, TOTAL_LESSONS);

        int progress = (completed * 100) / TOTAL_LESSONS;

        EnrollmentManager.updateProgress(this, courseTitle, progress);

        Toast.makeText(this,
                "Lesson completed (" + progress + "%)",
                Toast.LENGTH_SHORT).show();

        refreshUI();
    }

    /**
     * UI refresh logic
     */
    private void refreshUI() {

        boolean enrolled = EnrollmentManager.isEnrolled(this, courseTitle);

        int completed =
                StructuredProgressManager.getCompletedLessons(this, courseTitle, TOTAL_LESSONS);

        // Lesson lock state
        tvLesson1.setAlpha(1f);
        tvLesson2.setAlpha(completed >= 1 ? 1f : 0.4f);

        // Buttons
        if (!enrolled) {
            btnStart.setText("Enroll");
            btnCancel.setVisibility(MaterialButton.GONE);
        } else {
            btnCancel.setVisibility(MaterialButton.VISIBLE);

            int progress = (completed * 100) / TOTAL_LESSONS;
            btnStart.setText(progress < 100
                    ? "Resume Learning (" + progress + "%)"
                    : "Completed");
        }
    }

    /**
     * Next incomplete lesson
     */
    private int getNextLesson() {
        for (int i = 1; i <= TOTAL_LESSONS; i++) {
            if (!StructuredProgressManager.isLessonCompleted(this, courseTitle, i)) {
                return i;
            }
        }
        return TOTAL_LESSONS;
    }
}
