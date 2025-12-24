package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.utils.CourseListActivity;

public class ContentLibraryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_library);

        setupCourseItem(R.id.item_math);
        setupCourseItem(R.id.item_science);
        setupCourseItem(R.id.item_coding);
        setupCourseItem(R.id.item_art);
        setupCourseItem(R.id.item_geo);
    }

    private void setupCourseItem(int includeId) {
        View itemView = findViewById(includeId);
        if (itemView != null) {
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(ContentLibraryActivity.this, CourseListActivity.class);
                startActivity(intent);
            });
        }
    }
}
