package com.example.edubridge;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class HomeworkHelperActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework_helper);

        // Close
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Shutter Action
        findViewById(R.id.btn_shutter).setOnClickListener(v -> {
            Toast.makeText(this, "Photo captured! Analyzing...", Toast.LENGTH_SHORT).show();
            // In real app, analyze image and return to chat
            finish();
        });
    }
}