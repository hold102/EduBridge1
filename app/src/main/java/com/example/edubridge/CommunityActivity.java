package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class CommunityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // FIXED: Create Post (FAB at bottom)
        View btnCreate = findViewById(R.id.btn_create_post);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        // Setup Posts
        setupPost(R.id.post_1, "Alice M.", "Just finished the Python course!", "2h ago", R.drawable.img_hero_student);
        setupPost(R.id.post_2, "Bob D.", "Study group for Geography anyone?", "5h ago", R.drawable.ic_ai_buddy_avatar);
        setupPost(R.id.post_3, "Charlie", "Check out my Art sketch!", "1d ago", R.drawable.img_hero_student);
    }

    private void setupPost(int includeId, String name, String content, String time, int avatarResId) {
        View postView = findViewById(includeId);
        if (postView != null) {
            TextView tvName = postView.findViewById(R.id.tv_user_name);
            TextView tvContent = postView.findViewById(R.id.tv_content);
            TextView tvTime = postView.findViewById(R.id.tv_time);
            ImageView imgAvatar = postView.findViewById(R.id.img_avatar);

            if (tvName != null) tvName.setText(name);
            if (tvContent != null) tvContent.setText(content);
            if (tvTime != null) tvTime.setText(time);
            if (imgAvatar != null) imgAvatar.setImageResource(avatarResId);

            // FIXED: Navigate to Detail Activity
            postView.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityActivity.this, PostDetailActivity.class);
                startActivity(intent);
            });
        }
    }
}