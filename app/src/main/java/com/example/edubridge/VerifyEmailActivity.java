package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class VerifyEmailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        EditText etCode = findViewById(R.id.et_code);

        // Verify Button Logic
        findViewById(R.id.btn_verify).setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();

            if (code.length() < 4) {
                Toast.makeText(this, "Invalid Code", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check why we are here (From Intent)
            String purpose = getIntent().getStringExtra("PURPOSE");

            if ("RESET_PASSWORD".equals(purpose)) {
                // Flow: Forgot Pass -> Verify -> RESET PASSWORD
                Intent intent = new Intent(VerifyEmailActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
                finish();
            } else {
                // Flow: Sign Up -> Verify -> DASHBOARD
                Toast.makeText(this, "Email Verified! Welcome.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(VerifyEmailActivity.this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Resend Logic
        findViewById(R.id.tv_resend).setOnClickListener(v -> {
            Toast.makeText(this, "Code resent", Toast.LENGTH_SHORT).show();
        });
    }
}