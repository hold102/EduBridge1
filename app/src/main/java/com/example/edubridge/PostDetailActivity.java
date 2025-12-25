package com.example.edubridge;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    public static final String EXTRA_POST_ID = "extra_post_id";
    public static final String EXTRA_AUTHOR_ID = "extra_author_id";

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_TIME = "extra_time";
    public static final String EXTRA_CONTENT = "extra_content";
    public static final String EXTRA_AVATAR = "extra_avatar";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String postId;
    private String authorId;

    private TextView tvUserName, tvTime, tvContent;
    private ImageView imgAvatar;

    private View layoutOwnerActions;
    private MaterialButton btnEdit, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Back
        ImageView btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        // Views
        imgAvatar = findViewById(R.id.img_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvTime = findViewById(R.id.tv_time);
        tvContent = findViewById(R.id.tv_content);

        layoutOwnerActions = findViewById(R.id.layout_owner_actions);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        // Extras
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        authorId = getIntent().getStringExtra(EXTRA_AUTHOR_ID);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String time = getIntent().getStringExtra(EXTRA_TIME);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        int avatar = getIntent().getIntExtra(EXTRA_AVATAR, R.drawable.img_hero_student);

        tvUserName.setText(name == null ? "" : name);
        tvTime.setText(time == null ? "" : time);
        tvContent.setText(content == null ? "" : content);
        imgAvatar.setImageResource(avatar);

        // ✅ 判断是否本人
        String currentUid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
        boolean isOwner = currentUid != null && authorId != null && authorId.equals(currentUid);

        layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        if (isOwner) {
            btnEdit.setOnClickListener(v -> showEditDialog());
            btnDelete.setOnClickListener(v -> showDeleteConfirm());
        }
    }

    private void showEditDialog() {
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(this, "Missing postId", Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputEditText input = new TextInputEditText(this);
        input.setHint("Edit your post...");
        input.setText(tvContent.getText());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Post")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newContent = (input.getText() == null) ? "" : input.getText().toString().trim();
                    if (newContent.isEmpty()) {
                        Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    updatePostContent(newContent);
                })
                .show();
    }

    private void updatePostContent(String newContent) {
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("content", newContent);

        db.collection("posts")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    tvContent.setText(newContent);
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    btnEdit.setEnabled(true);
                    btnDelete.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEdit.setEnabled(true);
                    btnDelete.setEnabled(true);
                });
    }

    private void showDeleteConfirm() {
        if (postId == null || postId.trim().isEmpty()) {
            Toast.makeText(this, "Missing postId", Toast.LENGTH_SHORT).show();
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Delete", (dialog, which) -> deletePost())
                .show();
    }

    private void deletePost() {
        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        db.collection("posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    btnEdit.setEnabled(true);
                    btnDelete.setEnabled(true);
                });
    }
}