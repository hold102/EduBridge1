package com.example.edubridge;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();

        EditText etEmail = findViewById(R.id.et_email);

        // Send Button -> Send password reset link
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent double-click spam (optional but good)
            v.setEnabled(false);

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        v.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(
                                    this,
                                    "Reset link sent! Please check your email.",
                                    Toast.LENGTH_LONG
                            ).show();
                            finish(); // Back to Login
                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Unknown error";
                            Toast.makeText(this, "Failed: " + msg, Toast.LENGTH_LONG).show();
                        }
                    });
        });

        // Back Button
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
    }
}
