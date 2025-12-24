package com.example.edubridge;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Back Button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Pre-fill Data (Simulation)
        EditText etName = findViewById(R.id.et_name);
        EditText etBio = findViewById(R.id.et_bio);
        EditText etLocation = findViewById(R.id.et_location);

        etName.setText("EduBridge Student");
        etBio.setText("Level 5 Scholar");
        etLocation.setText("Kuala Lumpur");

        // Save Button
        findViewById(R.id.btn_save).setOnClickListener(v -> {
            Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
            finish(); // Go back to Profile
        });
    }
}