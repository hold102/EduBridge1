package com.example.edubridge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // 1. Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 2. Change Password
        findViewById(R.id.btn_change_password).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // 3. Accessibility Mode (TTS) Logic
        SwitchMaterial switchTTS = findViewById(R.id.switch_tts);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isTTSActive = prefs.getBoolean("TTS_ENABLED", false);
        switchTTS.setChecked(isTTSActive);

        switchTTS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("TTS_ENABLED", isChecked);
                editor.apply();

                if (isChecked) {
                    Toast.makeText(SettingsActivity.this, "Accessibility Mode: Text-to-Speech ON", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Accessibility Mode: Text-to-Speech OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 4. Log Out (REAL Firebase sign out)
        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(SettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Go back to MainActivity (launcher entry), clear back stack
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
