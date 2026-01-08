package com.example.edubridge;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.utils.AnalyticsManager; // ✅ Module 5.3 import

public class CourseDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 1. Get Views
        TextView tvTitle = findViewById(R.id.tv_course_title);
        TextView tvDesc = findViewById(R.id.tv_course_desc);
        ImageView imgIcon = findViewById(R.id.img_course_icon);
        TextView tvChapter1 = findViewById(R.id.tv_chapter_1);
        TextView tvChapter2 = findViewById(R.id.tv_chapter_2);

        // 2. Get Data
        String title = getIntent().getStringExtra("EXTRA_TITLE");
        String desc = getIntent().getStringExtra("EXTRA_DESC");
        int iconResId = getIntent().getIntExtra(
                "EXTRA_ICON",
                R.drawable.ic_subject_math
        );

        // 3. Update Basic UI
        if (title != null) tvTitle.setText(title);
        if (desc != null) tvDesc.setText(desc);
        imgIcon.setImageResource(iconResId);

        // 4. DYNAMIC SYLLABUS LOGIC
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
                // Default (Math)
                tvChapter1.setText("Introduction to Numbers");
                tvChapter2.setText("Linear Equations");
            }
        }

        // Start Button
        findViewById(R.id.btn_start_course).setOnClickListener(v -> {


            AnalyticsManager.recordChapter(this);


            Toast.makeText(
                    this,
                    "Progress recorded for " + (title != null ? title : "course"),
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}
