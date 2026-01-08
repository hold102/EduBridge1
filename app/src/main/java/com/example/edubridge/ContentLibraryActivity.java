package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.data.local.OfflineManager;

public class ContentLibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_library);

        OfflineManager.init(this);
        OfflineManager.seedIfEmpty();

        // Back Button
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Setup Courses with DATA PASSING
        setupCourseItem(
                R.id.item_math,
                "Mathematics",
                "Algebra, Geometry & Calculus",
                R.drawable.ic_subject_math
        );

        setupCourseItem(
                R.id.item_science,
                "Science",
                "Physics, Chemistry & Biology",
                R.drawable.ic_subject_science
        );

        setupCourseItem(
                R.id.item_coding,
                "Computer Science",
                "Python, Java & Web Development",
                R.drawable.ic_subject_coding
        );

        setupCourseItem(
                R.id.item_art,
                "Art & Design",
                "Sketching, Color Theory & History",
                R.drawable.ic_subject_art
        );

        setupCourseItem(
                R.id.item_geo,
                "Geography",
                "World Map, Cultures & Climate",
                R.drawable.ic_subject_geo
        );
    }

    private void setupCourseItem(int includeId, String title, String desc, int iconResId) {
        View itemView = findViewById(includeId);
        if (itemView != null) {
            TextView tvTitle = itemView.findViewById(R.id.tv_course_title);
            TextView tvDesc = itemView.findViewById(R.id.tv_course_desc);
            ImageView imgIcon = itemView.findViewById(R.id.img_course_icon);

            if (tvTitle != null) tvTitle.setText(title);
            if (tvDesc != null) tvDesc.setText(desc);
            if (imgIcon != null) imgIcon.setImageResource(iconResId);

            // Pass data to Detail Activity
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(
                        ContentLibraryActivity.this,
                        CourseDetailActivity.class
                );
                intent.putExtra("EXTRA_TITLE", title);
                intent.putExtra("EXTRA_DESC", desc);
                intent.putExtra("EXTRA_ICON", iconResId);
                startActivity(intent);
            });
        }
    }
}
