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
                                    Toast.makeText(SignUpActivity.this, "Sign up failed: user is null", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // Set display name (optional but useful for Community/Leaderboard)
                                UserProfileChangeRequest profileUpdates =
                                        new UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(profileTask -> {
                                            // Send verification email
                                            user.sendEmailVerification()
                                                    .addOnCompleteListener(verifyTask -> {
                                                        if (verifyTask.isSuccessful()) {
                                                            Toast.makeText(SignUpActivity.this,
                                                                    "Account created. Verification email sent.",
                                                                    Toast.LENGTH_LONG).show();
                                                        } else {
                                                            String msg = (verifyTask.getException() != null)
                                                                    ? verifyTask.getException().getMessage()
                                                                    : "Unknown error";
                                                            Toast.makeText(SignUpActivity.this,
                                                                    "Account created, but failed to send verification email: " + msg,
                                                                    Toast.LENGTH_LONG).show();
                                                        }

                                                        // Go to Verify Email screen
                                                        Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                                                        startActivity(intent);
                                                        finish();
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
}



