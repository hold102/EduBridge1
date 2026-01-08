package com.example.edubridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    // Post header
    private TextView tvUserName, tvTime, tvContent;
    private ImageView imgAvatar;

    // Owner actions
    private View layoutOwnerActions;
    private MaterialButton btnEdit, btnDelete;

    // Replies
    private TextView tvReplyCount;
    private RecyclerView recyclerReplies;
    private TextInputEditText etReply;
    private View btnSend;

    private final List<Reply> replyList = new ArrayList<>();
    private ReplyAdapter replyAdapter;
    private ListenerRegistration replyListener;

    // ✅ 方案A：当前准备回复谁
    private String replyToName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Back
        ImageView btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        // Post views
        imgAvatar = findViewById(R.id.img_avatar);
        tvUserName = findViewById(R.id.tv_user_name);
        tvTime = findViewById(R.id.tv_time);
        tvContent = findViewById(R.id.tv_content);

        layoutOwnerActions = findViewById(R.id.layout_owner_actions);
        btnEdit = findViewById(R.id.btn_edit);
        btnDelete = findViewById(R.id.btn_delete);

        // Replies views
        tvReplyCount = findViewById(R.id.tv_reply_count);
        recyclerReplies = findViewById(R.id.recycler_replies);
        etReply = findViewById(R.id.et_reply);
        btnSend = findViewById(R.id.btn_send);

        // Extras
        postId = getIntent().getStringExtra(EXTRA_POST_ID);
        authorId = getIntent().getStringExtra(EXTRA_AUTHOR_ID);

        String name = getIntent().getStringExtra(EXTRA_NAME);
        String time = getIntent().getStringExtra(EXTRA_TIME);
        String content = getIntent().getStringExtra(EXTRA_CONTENT);
        int avatar = getIntent().getIntExtra(EXTRA_AVATAR, R.drawable.img_hero_student);

        if (tvUserName != null) tvUserName.setText(safe(name));
        if (tvTime != null) tvTime.setText(safe(time));
        if (tvContent != null) tvContent.setText(safe(content));
        if (imgAvatar != null) imgAvatar.setImageResource(avatar);

        // ✅ 判断是否本人（帖子作者）
        String currentUid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
        boolean isOwner = currentUid != null && authorId != null && authorId.equals(currentUid);

        if (layoutOwnerActions != null) {
            layoutOwnerActions.setVisibility(isOwner ? View.VISIBLE : View.GONE);
        }

        if (isOwner) {
            if (btnEdit != null) btnEdit.setOnClickListener(v -> showEditDialog());
            if (btnDelete != null) btnDelete.setOnClickListener(v -> showDeleteConfirm());
        }

        // Replies
        setupReplies(currentUid);
        setupSendReply(currentUid);

        // ✅ 长按输入框：取消 @回复
        if (etReply != null) {
            etReply.setOnLongClickListener(v -> {
                replyToName = null;
                etReply.setHint("Write a reply...");
                Toast.makeText(this, "Reply target cleared", Toast.LENGTH_SHORT).show();
                return true;
            });
        }
    }

    // -------------------------
    // Post edit/delete
    // -------------------------
    private void showEditDialog() {
        if (TextUtils.isEmpty(postId)) {
            Toast.makeText(this, "Missing postId", Toast.LENGTH_SHORT).show();
            return;
        }

        TextInputEditText input = new TextInputEditText(this);
        input.setHint("Edit your post...");
        input.setText(tvContent == null ? "" : tvContent.getText());

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
        if (btnEdit != null) btnEdit.setEnabled(false);
        if (btnDelete != null) btnDelete.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("content", newContent);

        db.collection("posts")
                .document(postId)
                .update(updates)
                .addOnSuccessListener(v -> {
                    if (tvContent != null) tvContent.setText(newContent);
                    Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                    if (btnEdit != null) btnEdit.setEnabled(true);
                    if (btnDelete != null) btnDelete.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (btnEdit != null) btnEdit.setEnabled(true);
                    if (btnDelete != null) btnDelete.setEnabled(true);
                });
    }

    private void showDeleteConfirm() {
        if (TextUtils.isEmpty(postId)) {
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
        if (btnEdit != null) btnEdit.setEnabled(false);
        if (btnDelete != null) btnDelete.setEnabled(false);

        db.collection("posts")
                .document(postId)
                .delete()
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    if (btnEdit != null) btnEdit.setEnabled(true);
                    if (btnDelete != null) btnDelete.setEnabled(true);
                });
    }

    // -------------------------
    // Replies: listen + send + click-to-reply
    // -------------------------
    private void setupReplies(String currentUid) {
        if (TextUtils.isEmpty(postId) || recyclerReplies == null) return;

        replyAdapter = new ReplyAdapter(
                this,
                replyList,
                currentUid,
                postId,
                reply -> {
                    // ✅ 点击某条回复 → 设置 replyToName，并更新输入框 hint
                    replyToName = safe(reply.getUserName());
                    if (etReply != null) {
                        etReply.setHint("Reply to @" + replyToName + " (long press to cancel)");
                        etReply.requestFocus();
                    }
                    Toast.makeText(this, "Replying to @" + replyToName, Toast.LENGTH_SHORT).show();
                }
        );

        recyclerReplies.setLayoutManager(new LinearLayoutManager(this));
        recyclerReplies.setAdapter(replyAdapter);

        replyListener = db.collection("posts")
                .document(postId)
                .collection("replies")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    replyList.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Reply r = d.toObject(Reply.class);
                        if (r != null) {
                            r.setId(d.getId());
                            replyList.add(r);
                        }
                    }
                    replyAdapter.notifyDataSetChanged();

                    if (tvReplyCount != null) {
                        tvReplyCount.setText("Replies (" + replyList.size() + ")");
                    }
                });
    }

    private void setupSendReply(String currentUid) {
        if (btnSend == null) return;

        btnSend.setOnClickListener(v -> {
            if (TextUtils.isEmpty(postId)) {
                Toast.makeText(this, "Missing postId", Toast.LENGTH_SHORT).show();
                return;
            }
            if (currentUid == null) {
                Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show();
                return;
            }

            String text = (etReply == null || etReply.getText() == null) ? "" : etReply.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Reply cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = guessUserName();

            Map<String, Object> data = new HashMap<>();
            data.put("content", text);
            data.put("authorId", currentUid);
            data.put("userName", userName);
            data.put("replyToName", replyToName); // ✅ 方案A
            data.put("createdAt", FieldValue.serverTimestamp());

            db.collection("posts")
                    .document(postId)
                    .collection("replies")
                    .add(data)
                    .addOnSuccessListener(x -> {
                        if (etReply != null) etReply.setText("");
                        Toast.makeText(this, "Replied ✅", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(err ->
                            Toast.makeText(this, "Reply failed: " + err.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    private String guessUserName() {
        if (auth.getCurrentUser() == null) return "User";
        if (!TextUtils.isEmpty(auth.getCurrentUser().getDisplayName())) return auth.getCurrentUser().getDisplayName();

        String email = auth.getCurrentUser().getEmail();
        if (!TextUtils.isEmpty(email)) {
            int at = email.indexOf("@");
            return (at > 0) ? email.substring(0, at) : email;
        }
        return "User";
    }

    private String safe(String s) { return s == null ? "" : s; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (replyListener != null) {
            replyListener.remove();
            replyListener = null;
        }
    }
}