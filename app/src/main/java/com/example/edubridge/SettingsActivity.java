package com.example.edubridge;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.utils.LanguageHelper;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Bahasa Melayu
        View btnMalay = findViewById(R.id.btn_language_ms);
        if (btnMalay != null) {
            btnMalay.setOnClickListener(v -> {
                LanguageHelper.setLanguage(this, "ms");
                Toast.makeText(this, "Bahasa ditukar", Toast.LENGTH_SHORT).show();
                recreate();
            });
        }

        // English
        View btnEnglish = findViewById(R.id.btn_language_en);
        if (btnEnglish != null) {
            btnEnglish.setOnClickListener(v -> {
                LanguageHelper.setLanguage(this, "en");
                Toast.makeText(this, "Language changed", Toast.LENGTH_SHORT).show();
                recreate();
            });
        }
    }
}
