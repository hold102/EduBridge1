package com.example.edubridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    private static final long POINTS_PER_POST = 5;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText etContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etContent = findViewById(R.id.et_content);

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_post_action).setOnClickListener(v -> publishPost());
    }

    private void publishPost() {
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            Toast.makeText(this, "Please write something...", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String uid = user.getUid();
        final String email = user.getEmail();

        // ✅ Build display name (English only)
        String userName;
        if (!TextUtils.isEmpty(user.getDisplayName())) {
            userName = user.getDisplayName();
        } else if (!TextUtils.isEmpty(email) && email.contains("@")) {
            userName = email.substring(0, email.indexOf("@"));
        } else if (!TextUtils.isEmpty(email)) {
            userName = email;
        } else {
            userName = "Student";
        }

        // 1) Post data
        Map<String, Object> post = new HashMap<>();
        post.put("authorId", uid);
        post.put("userName", userName);
        post.put("content", content);
        post.put("createdAt", Timestamp.now());
        post.put("avatarRes", 0);

        // Disable button to prevent double click
        findViewById(R.id.btn_post_action).setEnabled(false);

        // 2) Batch: add post + update points + store userName
        WriteBatch batch = db.batch();

        DocumentReference newPostRef = db.collection("posts").document();
        batch.set(newPostRef, post);

        DocumentReference userRef = db.collection("users").document(uid);

        // ✅ store userName for leaderboard
        PointsManager.applyUserName(batch, userRef, userName);

        // ✅ +5 per post (centralized)
        PointsManager.applyAwardPoints(batch, userRef, POINTS_PER_POST, "create_post");

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Posted! +" + POINTS_PER_POST + " points", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.btn_post_action).setEnabled(true);
                    Toast.makeText(this, "Post failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}