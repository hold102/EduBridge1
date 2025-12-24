package com.example.edubridge;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DailyCheckInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_check_in);

        // Close Button
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        // Claim Reward Logic
        findViewById(R.id.btn_claim).setOnClickListener(v -> {
            Toast.makeText(this, "You earned 50 Points!", Toast.LENGTH_LONG).show();
            // Here you would implement logic to update user points in database
            finish(); // Close after claiming
        });
    }
}