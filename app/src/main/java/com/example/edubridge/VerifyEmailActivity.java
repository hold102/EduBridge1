package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Button btnContinue;
    private TextView tvResend;
    private TextView tvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();

        btnContinue = findViewById(R.id.btn_continue);
        tvResend = findViewById(R.id.tv_resend);
        tvLogout = findViewById(R.id.tv_logout);

        // Continue (always visible)
        btnContinue.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No logged-in user. Please log in again.", Toast.LENGTH_LONG).show();
                goToLogin();
                return;
            }

            // Refresh user state from server
            user.reload().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    String msg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(this, "Reload failed: " + msg, Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseUser refreshed = mAuth.getCurrentUser();
                if (refreshed != null && refreshed.isEmailVerified()) {
                    Toast.makeText(this, "Email verified.", Toast.LENGTH_SHORT).show();
                    goToDashboard();
                } else {
                    Toast.makeText(this, "You have not verified your email yet. Please check your inbox.", Toast.LENGTH_LONG).show();
                }
            });
        });

        // Resend verification email
        tvResend.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No logged-in user.", Toast.LENGTH_LONG).show();
                return;
            }

            user.sendEmailVerification().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Verification email resent.", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(this, "Resend failed: " + msg, Toast.LENGTH_LONG).show();
                }
            });
        });

        // Log out
        tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
            goToMain();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: auto-check when user returns from Gmail
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload();
        }
    }

    private void goToDashboard() {
        Intent intent = new Intent(VerifyEmailActivity.this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToMain() {
        Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}



