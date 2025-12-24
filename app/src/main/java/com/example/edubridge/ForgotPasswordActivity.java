package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        EditText etEmail = findViewById(R.id.et_email);

        // Send Button
        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                // Logic: Navigate to VerifyEmailActivity, but mark it for RESET PASSWORD
                Intent intent = new Intent(ForgotPasswordActivity.this, VerifyEmailActivity.class);
                intent.putExtra("PURPOSE", "RESET_PASSWORD"); // Critical Flag
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });

        // Back Button
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());
    }
}