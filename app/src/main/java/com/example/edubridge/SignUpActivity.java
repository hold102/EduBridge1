package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // 导入 EditText
import android.widget.TextView;
import android.widget.Toast; // 导入 Toast
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Views
        EditText etName = findViewById(R.id.et_name);
        EditText etEmail = findViewById(R.id.et_email);
        EditText etPassword = findViewById(R.id.et_password);
        Button btnSignUp = findViewById(R.id.btn_signup);
        TextView tvLogin = findViewById(R.id.tv_login);

        // Sign Up Button Logic with Validation
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. Get Input Data
                String name = etName.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                // 2. Validate Inputs
                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    // Error: Show Toast if any field is empty
                    Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Success: Proceed to Verify Email
                    // In a real app, you would save data to database here
                    Intent intent = new Intent(SignUpActivity.this, VerifyEmailActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Back to Login Logic
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Simply close this activity to return to Login
            }
        });
    }
}