package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Admin Login Activity for M6.1 Admin Authentication.
 * 
 * Features:
 * - A6.1.1: Secure credential authentication
 * - Validates admin role before granting access
 */
public class AdminLoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvError, tvBack;
    private ProgressBar progressBar;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();

        // Find views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvError = findViewById(R.id.tv_error);
        tvBack = findViewById(R.id.tv_back);
        progressBar = findViewById(R.id.progress_bar);

        // Login button
        btnLogin.setOnClickListener(v -> attemptLogin());

        // Back button
        tvBack.setOnClickListener(v -> finish());
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validate input
        if (TextUtils.isEmpty(email)) {
            showError("Please enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError("Please enter password");
            return;
        }

        // Show loading
        setLoading(true);
        hideError();

        // Authenticate with Firebase
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user != null) {
                        // Check if user is admin
                        checkAdminAccess(user.getUid());
                    } else {
                        setLoading(false);
                        showError("Authentication failed");
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    showError("Invalid credentials: " + e.getMessage());
                });
    }

    private void checkAdminAccess(String uid) {
        AdminManager.checkIsAdmin(uid, (isAdmin, role) -> {
            setLoading(false);

            if (isAdmin && role != null) {
                // Grant access - go to admin dashboard
                Toast.makeText(this, "Welcome, " + AdminManager.getRoleDisplayName(role),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, AdminDashboardActivity.class);
                intent.putExtra("admin_role", role);
                startActivity(intent);
                finish();
            } else {
                // Deny access - not an admin
                showError("Access denied. You are not authorized as an admin.");
                // Sign out since they don't have admin access
                auth.signOut();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
}
