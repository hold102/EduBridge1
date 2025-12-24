package com.example.edubridge;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class StudyPlannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_planner);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Add Task (Now at the bottom)
        findViewById(R.id.btn_add_task).setOnClickListener(v -> {
            Toast.makeText(this, "New Task Dialog will open here", Toast.LENGTH_SHORT).show();
        });
    }
}