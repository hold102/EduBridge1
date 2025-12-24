package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        mAuth = FirebaseAuth.getInstance();

        // ✅ VERIFY button
        findViewById(R.id.btn_verify).setOnClickListener(v -> {
            Toast.makeText(this, "VERIFY clicked", Toast.LENGTH_SHORT).show();

            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "No logged-in user. Please log in again.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }

            user.reload().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    String msg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                    Toast.makeText(this, "Reload failed: " + msg, Toast.LENGTH_LONG).show();
                    return;
                }

                FirebaseUser refreshedUser = mAuth.getCurrentUser();
                if (refreshedUser != null && refreshedUser.isEmailVerified()) {
                    Toast.makeText(this, "Email verified ✅", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VerifyEmailActivity.this, DashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Not verified yet. Please click the email link.", Toast.LENGTH_LONG).show();
                }
            });
        });

        // ✅ RESEND email
        findViewById(R.id.tv_resend).setOnClickListener(v -> {
            Toast.makeText(this, "Resend clicked", Toast.LENGTH_SHORT).show();

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

        // ✅ LOG OUT (avoid being stuck)
        findViewById(R.id.tv_logout).setOnClickListener(v -> {
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(VerifyEmailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}


