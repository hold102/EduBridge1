package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.edubridge.utils.EnrollmentManager;
import com.google.android.material.card.MaterialCardView;

import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupCard(R.id.card_content_library, ContentLibraryActivity.class);
        setupCard(R.id.card_community, CommunityActivity.class);
        setupCard(R.id.card_learning_buddy, LearningBuddyActivity.class);
        setupCard(R.id.card_study_planner, StudyPlannerActivity.class);
        setupCard(R.id.card_profile, ProfileActivity.class);
        setupCard(R.id.card_settings, SettingsActivity.class);

        View streakLayout = findViewById(R.id.layout_streak);
        if (streakLayout != null) {
            streakLayout.setOnClickListener(v ->
                    startActivity(new Intent(this, DailyCheckInActivity.class)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshEnrolledCourses();
    }

    private void refreshEnrolledCourses() {
        View container = findViewById(R.id.layout_enrolled_courses);
        if (!(container instanceof android.widget.LinearLayout)) return;

        android.widget.LinearLayout layout = (android.widget.LinearLayout) container;
        layout.removeAllViews();

        Map<String, Integer> enrolled = EnrollmentManager.getAllEnrollments(this);

        for (String title : enrolled.keySet()) {
            TextView tv = new TextView(this);
            tv.setText("• " + title + " (" + enrolled.get(title) + "%)");
            tv.setTextSize(14f);
            layout.addView(tv);
        }
    }

    private void setupCard(int cardId, Class<?> destinationActivity) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v ->
                    startActivity(new Intent(this, destinationActivity)));
        }
    }
}
