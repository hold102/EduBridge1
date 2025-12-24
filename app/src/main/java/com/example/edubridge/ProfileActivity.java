package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Edit Profile Button (The Pencil Icon)
        ImageView btnEdit = findViewById(R.id.btn_edit_profile);
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                // Navigate to Edit Profile
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
            });
        }

        // Dynamic data loading can happen here later
        TextView tvName = findViewById(R.id.tv_name);
        tvName.setText("EduBridge Student");
    }
}