package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin User List Activity for M6.2 User Management.
 * 
 * Features:
 * - A6.2.1: View list of registered users
 * - A6.2.2: Search and filter by name, email, status
 */
public class AdminUserListActivity extends AppCompatActivity implements AdminUserAdapter.OnUserClickListener {

    private RecyclerView recyclerUsers;
    private AdminUserAdapter adapter;
    private EditText etSearch;
    private TextView tvUserCount;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private Chip chipAll, chipActive, chipSuspended;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_list);

        db = FirebaseFirestore.getInstance();

        // Find views
        recyclerUsers = findViewById(R.id.recycler_users);
        etSearch = findViewById(R.id.et_search);
        tvUserCount = findViewById(R.id.tv_user_count);
        progressBar = findViewById(R.id.progress_bar);
        layoutEmpty = findViewById(R.id.layout_empty);

        chipAll = findViewById(R.id.chip_all);
        chipActive = findViewById(R.id.chip_active);
        chipSuspended = findViewById(R.id.chip_suspended);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Setup RecyclerView
        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminUserAdapter(this);
        recyclerUsers.setAdapter(adapter);

        // Search listener
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
                updateUserCount();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter chip listeners
        chipAll.setOnClickListener(v -> {
            adapter.setStatusFilter("all");
            updateUserCount();
        });
        chipActive.setOnClickListener(v -> {
            adapter.setStatusFilter("active");
            updateUserCount();
        });
        chipSuspended.setOnClickListener(v -> {
            adapter.setStatusFilter("suspended");
            updateUserCount();
        });

        // Load users
        loadUsers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from detail view
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        db.collection("users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    List<AdminUserAdapter.UserItem> users = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String uid = doc.getId();
                        String name = doc.getString("displayName");
                        if (name == null)
                            name = doc.getString("name");
                        String email = doc.getString("email");

                        Long points = doc.getLong("totalPoints");
                        long totalPoints = points != null ? points : 0L;

                        Boolean suspended = doc.getBoolean("isSuspended");
                        boolean isSuspended = suspended != null && suspended;

                        List<String> badges = (List<String>) doc.get("badges");
                        int badgeCount = badges != null ? badges.size() : 0;

                        users.add(
                                new AdminUserAdapter.UserItem(uid, name, email, totalPoints, isSuspended, badgeCount));
                    }

                    adapter.setUsers(users);
                    updateUserCount();

                    if (users.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                });
    }

    private void updateUserCount() {
        int count = adapter.getFilteredCount();
        tvUserCount.setText(count + " user" + (count != 1 ? "s" : ""));
    }

    @Override
    public void onUserClick(AdminUserAdapter.UserItem user) {
        // Open user detail (A6.2.3)
        Intent intent = new Intent(this, AdminUserDetailActivity.class);
        intent.putExtra("user_id", user.uid);
        intent.putExtra("user_name", user.name);
        intent.putExtra("user_email", user.email);
        startActivity(intent);
    }
}
