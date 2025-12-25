package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Create Post (FAB at bottom)
        View btnCreate = findViewById(R.id.btn_create_post);
        if (btnCreate != null) {
            btnCreate.setOnClickListener(v -> {
                Intent intent = new Intent(CommunityActivity.this, CreatePostActivity.class);
                startActivity(intent);
            });
        }

        // RecyclerView setup
        recyclerPosts = findViewById(R.id.recycler_posts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        // Dummy posts (for UI testing)
        List<CommunityPost> dummyPosts = new ArrayList<>();
        dummyPosts.add(new CommunityPost("Alice M.", "Just finished the Python course!", "2h ago", R.drawable.img_hero_student));
        dummyPosts.add(new CommunityPost("Bob D.", "Study group for Geography anyone?", "5h ago", R.drawable.ic_ai_buddy_avatar));
        dummyPosts.add(new CommunityPost("Charlie", "Check out my Art sketch!", "1d ago", R.drawable.img_hero_student));

        CommunityPostAdapter adapter = new CommunityPostAdapter(this, dummyPosts);
        recyclerPosts.setAdapter(adapter);
    }
}