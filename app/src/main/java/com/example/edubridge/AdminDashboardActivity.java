package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Admin Dashboard Activity for M6.1 Admin Authentication.
 * 
 * Features:
 * - A6.1.2: Role-based menu visibility
 * - A6.1.3: Restrict access based on role
 * - A6.1.4: Session timeout with auto-logout
 */
public class AdminDashboardActivity extends AppCompatActivity {

    private String adminRole;
    private TextView tvRole, tvSessionTimer, tvWelcomeMessage;
    private MaterialButton btnLogout;

    // Menu cards
    private MaterialCardView cardUsers, cardContent, cardCommunity, cardAnalytics, cardSettings;

    // Session timer
    private Handler timerHandler;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        // Get role from intent
        adminRole = getIntent().getStringExtra("admin_role");
        if (adminRole == null) {
            adminRole = AdminManager.ROLE_MODERATOR; // Default to lowest permission
        }

        // Find views
        tvRole = findViewById(R.id.tv_role);
        tvSessionTimer = findViewById(R.id.tv_session_timer);
        tvWelcomeMessage = findViewById(R.id.tv_welcome_message);
        btnLogout = findViewById(R.id.btn_logout);

        cardUsers = findViewById(R.id.card_users);
        cardContent = findViewById(R.id.card_content);
        cardCommunity = findViewById(R.id.card_community);
        cardAnalytics = findViewById(R.id.card_analytics);
        cardSettings = findViewById(R.id.card_settings);

        // Setup UI based on role
        setupRoleBasedUI();

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Logout button
        btnLogout.setOnClickListener(v -> logout());

        // Menu card clicks
        setupMenuClicks();

        // Start session timeout (A6.1.4)
        startSessionTimeout();

        // Update timer display
        startTimerDisplay();
    }

    /**
     * Setup UI based on admin role (A6.1.2, A6.1.3).
     */
    private void setupRoleBasedUI() {
        tvRole.setText(AdminManager.getRoleDisplayName(adminRole));

        // Set welcome message based on role
        switch (adminRole) {
            case AdminManager.ROLE_SUPER_ADMIN:
                tvWelcomeMessage.setText("You have full access to manage the EduBridge platform.");
                break;
            case AdminManager.ROLE_CONTENT_ADMIN:
                tvWelcomeMessage.setText("You can manage courses and learning content.");
                break;
            case AdminManager.ROLE_MODERATOR:
                tvWelcomeMessage.setText("You can moderate users and community content.");
                break;
        }

        // Show/hide menu items based on role
        cardUsers.setVisibility(AdminManager.hasPermission(adminRole, "manage_users")
                ? View.VISIBLE
                : View.GONE);
        cardContent.setVisibility(AdminManager.hasPermission(adminRole, "manage_content")
                ? View.VISIBLE
                : View.GONE);
        cardCommunity.setVisibility(AdminManager.hasPermission(adminRole, "manage_community")
                ? View.VISIBLE
                : View.GONE);

        // Analytics and Settings - Super Admin only
        boolean isSuperAdmin = adminRole.equals(AdminManager.ROLE_SUPER_ADMIN);
        cardAnalytics.setVisibility(isSuperAdmin ? View.VISIBLE : View.GONE);
        cardSettings.setVisibility(isSuperAdmin ? View.VISIBLE : View.GONE);
    }

    private void setupMenuClicks() {
        cardUsers.setOnClickListener(v -> {
            // Navigate to User Management (M6.2)
            Intent intent = new Intent(this, AdminUserListActivity.class);
            startActivity(intent);
        });

        cardContent.setOnClickListener(v -> {
            // Navigate to Content Management (M6.3)
            Intent intent = new Intent(this, AdminCourseListActivity.class);
            startActivity(intent);
        });

        cardCommunity.setOnClickListener(v -> {
            Toast.makeText(this, "Community Moderation - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        cardAnalytics.setOnClickListener(v -> {
            Toast.makeText(this, "Analytics - Coming Soon", Toast.LENGTH_SHORT).show();
        });

        cardSettings.setOnClickListener(v -> {
            Toast.makeText(this, "System Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        });
    }

    // ===== Session Timeout (A6.1.4) =====

    private void startSessionTimeout() {
        AdminManager.startSessionTimeout(this, () -> {
            // Session expired - show dialog then logout
            runOnUiThread(() -> {
                new AlertDialog.Builder(AdminDashboardActivity.this)
                        .setTitle("Session Expired")
                        .setMessage("Your admin session has expired due to inactivity.")
                        .setCancelable(false)
                        .setPositiveButton("OK", (d, w) -> logout())
                        .show();
            });
        });
    }

    private void startTimerDisplay() {
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                int minutes = AdminManager.getRemainingSessionMinutes();
                tvSessionTimer.setText(minutes + " min");

                // Warning at 2 minutes
                if (minutes <= 2) {
                    tvSessionTimer.setTextColor(0xFFD32F2F); // Red
                } else {
                    tvSessionTimer.setTextColor(0xFFAAAAAA); // Gray
                }

                timerHandler.postDelayed(this, 10000); // Update every 10 seconds
            }
        };
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        // Reset session timeout on any interaction
        AdminManager.updateActivity();
    }

    private void logout() {
        AdminManager.stopSessionTimeout();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

        // Go back to admin login
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AdminManager.stopSessionTimeout();
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
