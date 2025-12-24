package com.example.edubridge;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PostDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Send Comment
        findViewById(R.id.btn_send_comment).setOnClickListener(v -> {
            Toast.makeText(this, "Comment posted!", Toast.LENGTH_SHORT).show();
            // Logic to add comment to view would go here
        });
    }
}