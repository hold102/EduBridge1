package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSignUp = findViewById(R.id.btn_signup);
        TextView tvLogin = findViewById(R.id.tv_login);

        // Sign Up -> Create Firebase account + Send verification email
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Firebase: create account
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user == null) {
                                    Toast.makeText(SignUpActivity.this, "Sign up failed: user is null",
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Set display name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            // STEP 1: Save user to Firestore
                                            saveUserToFirestore(user.getUid(), name, email, () -> {
                                                // STEP 2: Send verification email
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(verifyTask -> {
                                                            if (verifyTask.isSuccessful()) {
                                                                Toast.makeText(SignUpActivity.this,
                                                                        "Account created. Verification email sent.",
                                                                        Toast.LENGTH_LONG).show();
                                                            } else {
                                                                Toast.makeText(SignUpActivity.this,
                                                                        "Account created, but email failed.",
                                                                        Toast.LENGTH_SHORT).show();
                                                            }
                                                            // Go to Verify Email screen
                                                            startActivity(new Intent(SignUpActivity.this,
                                                                    VerifyEmailActivity.class));
                                                            finish();
                                                        });
                                            });
                                        });

                            } else {
                                String msg = (task.getException() != null)
                                        ? task.getException().getMessage()
                                        : "Unknown error";
                                Toast.makeText(SignUpActivity.this, "Sign up failed: " + msg, Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        // Back to Login
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * Save user document to Firestore.
     * Creates users/{uid} with profile data.
     */
    private void saveUserToFirestore(String uid, String name, String email, Runnable onComplete) {
        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();

        java.util.Map<String, Object> userData = new java.util.HashMap<>();
        userData.put("uid", uid);
        userData.put("email", email);
        userData.put("name", name);
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("enrolledCoursesCount", 0);
        userData.put("completedCoursesCount", 0);
        userData.put("totalPoints", 0);
        userData.put("level", 1);
        // Admin fields (default to non-admin)
        userData.put("isAdmin", false);
        userData.put("adminRole", null);
        userData.put("isSuspended", false);

        db.collection("users").document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("SignUpActivity", "✅ User saved to Firestore: " + uid);
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("SignUpActivity", "❌ Firestore save failed: " + e.getMessage());
                    Toast.makeText(SignUpActivity.this,
                            "Warning: Profile save failed - " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Continue anyway so user can still use the app
                    onComplete.run();
                });
    }
}
