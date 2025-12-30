package com.example.edubridge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Admin Manager for M6.1 Admin Authentication & Authorization.
 * 
 * Features:
 * - A6.1.1: Admin authentication validation
 * - A6.1.2: Role-based access control (RBAC)
 * - A6.1.3: Role permission checking
 * - A6.1.4: Session timeout management
 */
public class AdminManager {

    private static final String TAG = "AdminManager";

    // Admin roles (A6.1.2)
    public static final String ROLE_SUPER_ADMIN = "super_admin";
    public static final String ROLE_CONTENT_ADMIN = "content_admin";
    public static final String ROLE_MODERATOR = "moderator";

    // Session timeout (A6.1.4) - 15 minutes in milliseconds
    public static final long SESSION_TIMEOUT_MS = 15 * 60 * 1000;
    private static final long TIMEOUT_CHECK_INTERVAL_MS = 60 * 1000; // Check every minute

    private static long lastActivityTime = System.currentTimeMillis();
    private static Handler timeoutHandler;
    private static Runnable timeoutRunnable;
    private static Activity currentActivity;

    /**
     * Check if user is an admin.
     * Callback returns true if user has isAdmin=true in Firestore.
     */
    public static void checkIsAdmin(String uid, AdminCheckCallback callback) {
        if (uid == null || uid.isEmpty()) {
            callback.onResult(false, null);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean isAdmin = doc.getBoolean("isAdmin");
                        String role = doc.getString("adminRole");
                        callback.onResult(isAdmin != null && isAdmin, role);
                    } else {
                        callback.onResult(false, null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to check admin status", e);
                    callback.onResult(false, null);
                });
    }

    /**
     * Check if role has permission for an action.
     * 
     * Permissions:
     * - super_admin: All permissions
     * - content_admin: manage_content, view_analytics
     * - moderator: manage_users, manage_community
     */
    public static boolean hasPermission(String role, String permission) {
        if (role == null)
            return false;

        switch (role) {
            case ROLE_SUPER_ADMIN:
                return true; // Super admin has all permissions

            case ROLE_CONTENT_ADMIN:
                return permission.equals("manage_content") ||
                        permission.equals("view_analytics") ||
                        permission.equals("view_dashboard");

            case ROLE_MODERATOR:
                return permission.equals("manage_users") ||
                        permission.equals("manage_community") ||
                        permission.equals("view_dashboard");

            default:
                return false;
        }
    }

    /**
     * Get display name for role.
     */
    public static String getRoleDisplayName(String role) {
        if (role == null)
            return "Unknown";

        switch (role) {
            case ROLE_SUPER_ADMIN:
                return "Super Admin";
            case ROLE_CONTENT_ADMIN:
                return "Content Admin";
            case ROLE_MODERATOR:
                return "Moderator";
            default:
                return "Unknown";
        }
    }

    // ===== Session Timeout Management (A6.1.4) =====

    /**
     * Start session timeout tracking.
     * Call this when entering admin dashboard.
     */
    public static void startSessionTimeout(Activity activity, SessionTimeoutCallback callback) {
        currentActivity = activity;
        lastActivityTime = System.currentTimeMillis();

        if (timeoutHandler != null) {
            stopSessionTimeout();
        }

        timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = System.currentTimeMillis() - lastActivityTime;

                if (elapsed >= SESSION_TIMEOUT_MS) {
                    // Session expired - logout
                    Log.d(TAG, "Session timeout - logging out admin");
                    callback.onTimeout();
                } else {
                    // Check again
                    timeoutHandler.postDelayed(this, TIMEOUT_CHECK_INTERVAL_MS);
                }
            }
        };

        timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_CHECK_INTERVAL_MS);
    }

    /**
     * Stop session timeout tracking.
     * Call this when leaving admin dashboard.
     */
    public static void stopSessionTimeout() {
        if (timeoutHandler != null && timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        timeoutHandler = null;
        timeoutRunnable = null;
        currentActivity = null;
    }

    /**
     * Update last activity time.
     * Call this on user interaction to reset timeout.
     */
    public static void updateActivity() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Get remaining session time in minutes.
     */
    public static int getRemainingSessionMinutes() {
        long elapsed = System.currentTimeMillis() - lastActivityTime;
        long remaining = SESSION_TIMEOUT_MS - elapsed;
        return (int) Math.max(0, remaining / 60000);
    }

    // ===== Callbacks =====

    public interface AdminCheckCallback {
        void onResult(boolean isAdmin, String role);
    }

    public interface SessionTimeoutCallback {
        void onTimeout();
    }
}
