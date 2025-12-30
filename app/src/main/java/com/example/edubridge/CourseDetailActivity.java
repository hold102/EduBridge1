package com.example.edubridge;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RatingBar;

import androidx.appcompat.app.AppCompatActivity;

public class CourseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 1. Get Views (Basic info)
        TextView tvTitle = findViewById(R.id.tv_course_title);
        TextView tvDesc = findViewById(R.id.tv_course_desc);
        ImageView imgIcon = findViewById(R.id.img_course_icon);

        // 2. Get Views (M3.2 details)
        TextView tvInstructor = findViewById(R.id.tv_instructor);
        TextView tvDuration = findViewById(R.id.tv_duration);
        TextView tvRating = findViewById(R.id.tv_rating);
        TextView tvPrerequisites = findViewById(R.id.tv_prerequisites);
        TextView tvChapter1 = findViewById(R.id.tv_chapter_1);
        TextView tvChapter2 = findViewById(R.id.tv_chapter_2);
        RatingBar ratingBarUser = findViewById(R.id.ratingBar_user);

        // 3. Get Data from Intent (from M3.1)
        String title = getIntent().getStringExtra("EXTRA_TITLE");
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        int iconResId = getIntent().getIntExtra(
                "EXTRA_ICON",
                R.drawable.ic_subject_math
        );

        // 4. Update Basic UI
        if (title != null) tvTitle.setText(title);
        if (desc != null) tvDesc.setText(desc);
        imgIcon.setImageResource(iconResId);

        // 5. Static preview info (M3.2 compliant)
        tvInstructor.setText("Instructor: Dr. Smith");
        tvDuration.setText("Duration: 8 weeks");
        tvRating.setText("⭐ 4.6 (1,200 learners)");
        tvPrerequisites.setText("• Basic understanding of the subject");

        // 6. Dynamic syllabus & lesson structure (by course type)
        if (title != null) {
            if (title.contains("Geography")) {
                tvChapter1.setText("Map Reading & Coordinates");
                tvChapter2.setText("Plate Tectonics");
            } else if (title.contains("Science")) {
                tvChapter1.setText("Newton's Laws of Motion");
                tvChapter2.setText("The Periodic Table");
            } else if (title.contains("Art")) {
                tvChapter1.setText("Color Theory Basics");
                tvChapter2.setText("Perspective Drawing");
            } else if (title.contains("Computer")) {
                tvChapter1.setText("Variables & Data Types");
                tvChapter2.setText("Loops & Conditionals");
            } else {
                // Default: Mathematics
                tvChapter1.setText("Introduction to Numbers");
                tvChapter2.setText("Linear Equations");
            }
        }

        // 7. User rating interaction (no persistence required for M3.2)
        ratingBarUser.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                Toast.makeText(
                        this,
                        "Thanks for rating " + rating + " stars!",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // 8. Resume / Preview button
        findViewById(R.id.btn_start_course).setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Previewing " + (title != null ? title : "course"),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}
