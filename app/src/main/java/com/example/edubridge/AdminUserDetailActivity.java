package com.example.edubridge;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Admin User Detail Activity for M6.2 User Management.
 * 
 * Features:
 * - A6.2.3: View detailed user profile (XP, level, badges, progress)
 * - A6.2.4: Suspend, activate, or delete user accounts
 * - A6.2.5: Log all administrative actions
 */
public class AdminUserDetailActivity extends AppCompatActivity {

    private String userId, userName, userEmail;
    private boolean isSuspended = false;

    private TextView tvName, tvEmail, tvStatus, tvUid;
    private TextView tvXp, tvLevel, tvBadges, tvStreak;
    private MaterialButton btnToggleStatus, btnDelete;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail);

        db = FirebaseFirestore.getInstance();

        // Get user data from intent
        userId = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        userEmail = getIntent().getStringExtra("user_email");

        if (userId == null) {
            Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Find views
        tvName = findViewById(R.id.tv_name);
        tvEmail = findViewById(R.id.tv_email);
        tvStatus = findViewById(R.id.tv_status);
        tvUid = findViewById(R.id.tv_uid);
        tvXp = findViewById(R.id.tv_xp);
        tvLevel = findViewById(R.id.tv_level);
        tvBadges = findViewById(R.id.tv_badges);
        tvStreak = findViewById(R.id.tv_streak);
        btnToggleStatus = findViewById(R.id.btn_toggle_status);
        btnDelete = findViewById(R.id.btn_delete);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Set basic info
        tvName.setText(userName != null ? userName : "Unknown");
        tvEmail.setText(userEmail != null ? userEmail : "");
        tvUid.setText("UID: " + userId);

        // Load full user data
        loadUserData();

        // Setup action buttons
        btnToggleStatus.setOnClickListener(v -> toggleUserStatus());
        btnDelete.setOnClickListener(v -> confirmDeleteUser());
    }

    private void loadUserData() {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // XP
                        Long points = doc.getLong("totalPoints");
                        long xp = points != null ? points : 0;
                        tvXp.setText(String.valueOf(xp));

                        // Level
                        int level = LevelManager.getLevelForXp(xp);
                        tvLevel.setText(String.valueOf(level));

                        // Badges
                        List<String> badges = (List<String>) doc.get("badges");
                        int badgeCount = badges != null ? badges.size() : 0;
                        tvBadges.setText(String.valueOf(badgeCount));

                        // Streak
                        Long streak = doc.getLong("streakCount");
                        long streakCount = streak != null ? streak : 0;
                        tvStreak.setText(streakCount + " Day Streak");

                        // Status
                        Boolean suspended = doc.getBoolean("isSuspended");
                        isSuspended = suspended != null && suspended;
                        updateStatusUI();
                    }
                });
    }

    private void updateStatusUI() {
        if (isSuspended) {
            tvStatus.setText("Suspended");
            tvStatus.setBackgroundColor(0xFFD32F2F);
            btnToggleStatus.setText("Activate Account");
            btnToggleStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4CAF50));
        } else {
            tvStatus.setText("Active");
            tvStatus.setBackgroundColor(0xFF4CAF50);
            btnToggleStatus.setText("Suspend Account");
            btnToggleStatus.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFFFF9800));
        }
    }

    /**
     * Toggle user suspension status (A6.2.4).
     */
    private void toggleUserStatus() {
        String action = isSuspended ? "activate" : "suspend";
        String actionDisplay = isSuspended ? "Activate" : "Suspend";

        new AlertDialog.Builder(this)
                .setTitle(actionDisplay + " User?")
                .setMessage("Are you sure you want to " + action + " this user account?")
                .setPositiveButton(actionDisplay, (d, w) -> {
                    boolean newStatus = !isSuspended;

                    db.collection("users").document(userId)
                            .update("isSuspended", newStatus)
                            .addOnSuccessListener(unused -> {
                                isSuspended = newStatus;
                                updateStatusUI();
                                logAdminAction(action + "_user");
                                Toast.makeText(this, "User " + (newStatus ? "suspended" : "activated"),
                                        Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Confirm and delete user account (A6.2.4).
     */
    private void confirmDeleteUser() {
        new AlertDialog.Builder(this)
                .setTitle("Delete User?")
                .setMessage("This will permanently delete the user account. This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteUser())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteUser() {
        // Delete from Firestore (Note: Firebase Auth deletion requires admin SDK)
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(unused -> {
                    logAdminAction("delete_user");
                    Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Log administrative action (A6.2.5).
     */
    private void logAdminAction(String action) {
        FirebaseUser admin = FirebaseAuth.getInstance().getCurrentUser();
        if (admin == null)
            return;

        Map<String, Object> log = new HashMap<>();
        log.put("adminId", admin.getUid());
        log.put("adminEmail", admin.getEmail());
        log.put("action", action);
        log.put("targetUserId", userId);
        log.put("targetEmail", userEmail);
        log.put("timestamp", Timestamp.now());

        db.collection("admin_logs").add(log)
                .addOnSuccessListener(ref -> {
                    android.util.Log.d("AdminLog", "Action logged: " + action);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminLog", "Failed to log action", e);
                });
    }
}
