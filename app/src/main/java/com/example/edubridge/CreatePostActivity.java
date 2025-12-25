package com.example.edubridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

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

        // Close
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Post
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

        String uid = user.getUid();
        String email = user.getEmail();

        // 名字兜底：邮箱前缀
        String userName = "User";
        if (!TextUtils.isEmpty(user.getDisplayName())) {
            userName = user.getDisplayName();
        } else if (!TextUtils.isEmpty(email) && email.contains("@")) {
            userName = email.substring(0, email.indexOf("@"));
        } else if (!TextUtils.isEmpty(email)) {
            userName = email;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("authorId", uid);            // ✅关键字段
        data.put("userName", userName);
        data.put("content", content);
        data.put("createdAt", Timestamp.now());
        data.put("avatarRes", 0);            // 默认头像

        findViewById(R.id.btn_post_action).setEnabled(false);

        db.collection("posts")
                .add(data)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Posted successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // 回到列表，snapshotListener 自动刷新
                })
                .addOnFailureListener(e -> {
                    findViewById(R.id.btn_post_action).setEnabled(true);
                    Toast.makeText(this, "Post failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}