package com.example.edubridge;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.dao.CommunityPostDao;
import com.example.edubridge.data.local.entity.LocalCommunityPost;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Community Activity - Discussion Forum
 * 
 * Features:
 * - View and create community posts
 * - Online: Fetches from Firestore, caches locally
 * - Offline: Loads cached posts from Room database
 */
public class CommunityActivity extends AppCompatActivity {

    private static final String TAG = "CommunityActivity";

    private RecyclerView recyclerPosts;
    private CommunityPostAdapter adapter;
    private final List<CommunityPost> postList = new ArrayList<>();
    private TextView tvOfflineIndicator;

    private FirebaseFirestore db;
    private ListenerRegistration postListener;
    private CommunityPostDao localPostDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);

        // Initialize database
        localPostDao = AppDatabase.getInstance(this).communityPostDao();

        // Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null)
            btnBack.setOnClickListener(v -> finish());

        // Create post button
        View btnCreate = findViewById(R.id.btn_create_post);
        if (btnCreate != null) {
            btnCreate.setClickable(true);
            btnCreate.setOnClickListener(v -> {
                if (!isOnline()) {
                    Toast.makeText(this, "Cannot create posts while offline", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    startActivity(new Intent(CommunityActivity.this, CreatePostActivity.class));
                } catch (Exception e) {
                    Toast.makeText(this, "Cannot open create post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "start CreatePostActivity failed", e);
                }
            });
        }

        recyclerPosts = findViewById(R.id.recycler_posts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new CommunityPostAdapter(this, postList);
        recyclerPosts.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Check connectivity and load posts accordingly
        if (isOnline()) {
            loadFromFirestore();
        } else {
            loadFromLocalCache();
            Toast.makeText(this, "Offline mode - showing cached posts", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check if device has network connectivity.
     */
    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        }
        return false;
    }

    /**
     * Load posts from Firestore and cache locally.
     */
    private void loadFromFirestore() {
        postListener = db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        error.printStackTrace();
                        // Fallback to local on error
                        if (postList.isEmpty()) {
                            loadFromLocalCache();
                        }
                        return;
                    }
                    if (value == null)
                        return;

                    postList.clear();
                    List<LocalCommunityPost> localPosts = new ArrayList<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        CommunityPost post = doc.toObject(CommunityPost.class);
                        if (post != null) {
                            post.setId(doc.getId());
                            postList.add(post);

                            // Convert to local entity for caching
                            LocalCommunityPost localPost = new LocalCommunityPost(
                                    doc.getId(),
                                    post.getAuthorId(),
                                    post.getUserName(),
                                    post.getContent(),
                                    post.getCreatedAt() != null ? post.getCreatedAt().toDate().getTime() : 0,
                                    post.getAvatarRes());
                            localPosts.add(localPost);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Cache posts to local database
                    new Thread(() -> {
                        localPostDao.deleteAll();
                        localPostDao.insertAll(localPosts);
                        Log.d(TAG, "Cached " + localPosts.size() + " posts locally");
                    }).start();
                });
    }

    /**
     * Load posts from local Room database (offline mode).
     */
    private void loadFromLocalCache() {
        new Thread(() -> {
            List<LocalCommunityPost> cachedPosts = localPostDao.getAllPostsSync();

            runOnUiThread(() -> {
                postList.clear();
                for (LocalCommunityPost local : cachedPosts) {
                    CommunityPost post = new CommunityPost();
                    post.setId(local.id);
                    post.setAuthorId(local.authorId);
                    post.setUserName(local.userName);
                    post.setContent(local.content);
                    post.setCreatedAt(new Timestamp(local.createdAt / 1000, 0));
                    post.setAvatarRes(local.avatarRes);
                    postList.add(post);
                }
                adapter.notifyDataSetChanged();

                if (cachedPosts.isEmpty()) {
                    Toast.makeText(this, "No cached posts available", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (postListener != null)
            postListener.remove();
    }
}