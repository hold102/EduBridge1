package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ResetPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // This page is optional for Firebase reset-link flow.
        // For standard flow: user sets new password via email link in browser.

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Toast.makeText(
                    this,
                    "Please reset your password via the email link, then login again.",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
