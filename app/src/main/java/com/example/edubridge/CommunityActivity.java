package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class CommunityActivity extends AppCompatActivity {

    private RecyclerView recyclerPosts;
    private CommunityPostAdapter adapter;
    private final List<CommunityPost> postList = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration postListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // ✅ + 按钮：跳转到 CreatePostActivity
        View btnCreate = findViewById(R.id.btn_create_post);
        if (btnCreate != null) {
            btnCreate.setClickable(true);
            btnCreate.setOnClickListener(v -> {
                Toast.makeText(this, "打开发布页…", Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(CommunityActivity.this, CreatePostActivity.class));
                } catch (Exception e) {
                    Toast.makeText(this, "无法打开发布页: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("CommunityActivity", "start CreatePostActivity failed", e);
                }
            });
        } else {
            Toast.makeText(this, "btn_create_post 没找到，请检查 activity_community.xml 的 id", Toast.LENGTH_LONG).show();
        }

        recyclerPosts = findViewById(R.id.recycler_posts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommunityPostAdapter(this, postList);
        recyclerPosts.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        postListener = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        return;
                    }
                    if (value == null) return;

                    postList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        CommunityPost post = doc.toObject(CommunityPost.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            postList.add(post);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postListener != null) postListener.remove();
    }
}