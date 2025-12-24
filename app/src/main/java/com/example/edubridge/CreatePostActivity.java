package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreatePostActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        // Close
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Camera Action (Jump to Homework/Camera Page)
        findViewById(R.id.btn_open_camera).setOnClickListener(v -> {
            Intent intent = new Intent(CreatePostActivity.this, HomeworkHelperActivity.class);
            startActivity(intent);
        });

        // Post Action
        findViewById(R.id.btn_post_action).setOnClickListener(v -> {
            EditText etContent = findViewById(R.id.et_content);
            String content = etContent.getText().toString().trim();

            if (!content.isEmpty()) {
                Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}