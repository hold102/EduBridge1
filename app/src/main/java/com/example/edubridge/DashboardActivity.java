package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // 1. Setup Navigation Cards (6 Items)
        setupCard(R.id.card_content_library, ContentLibraryActivity.class);
        setupCard(R.id.card_community, CommunityActivity.class);
        setupCard(R.id.card_learning_buddy, LearningBuddyActivity.class);
        setupCard(R.id.card_study_planner, StudyPlannerActivity.class);
        setupCard(R.id.card_profile, ProfileActivity.class);
        setupCard(R.id.card_settings, SettingsActivity.class);

        // 2. Setup Header - Streak (Fire Icon)
        View streakLayout = findViewById(R.id.layout_streak);
        if (streakLayout != null) {
            streakLayout.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, DailyCheckInActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupCard(int cardId, Class<?> destinationActivity) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                Intent intent = new Intent(DashboardActivity.this, destinationActivity);
                startActivity(intent);
            });
        }
    }
}