package com.example.edubridge;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                    Toast.makeText(SettingsActivity.this, "Accessibility Mode: Text-to-Speech ON", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(SettingsActivity.this, "Accessibility Mode: Text-to-Speech OFF", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // 4. Learning Goal Logic (M2.2)
        com.google.android.material.textfield.TextInputEditText etLearningGoal = findViewById(R.id.et_learning_goal);
        android.widget.Button btnSaveGoal = findViewById(R.id.btn_save_goal);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                    .getInstance();

            // Load current goal
            db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String currentGoal = snapshot.getString("learningGoal");
                    if (currentGoal != null && etLearningGoal != null) {
                        etLearningGoal.setText(currentGoal);
                    }
                }
            });

            // Save goal
            if (btnSaveGoal != null) {
                btnSaveGoal.setOnClickListener(v -> {
                    String newGoal = (etLearningGoal != null && etLearningGoal.getText() != null)
                            ? etLearningGoal.getText().toString().trim()
                            : "";

                    if (newGoal.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, "Please enter a valid goal.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    java.util.Map<String, Object> update = new java.util.HashMap<>();
                    update.put("learningGoal", newGoal);

                    db.collection("users").document(uid)
                            .set(update, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Toast.makeText(SettingsActivity.this,
                                    "Preferences Saved! Check your Dashboard.", Toast.LENGTH_LONG).show())
                            .addOnFailureListener(e -> Toast.makeText(SettingsActivity.this,
                                    "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
            }

            // 5. Notification Preferences (M2.3)
            SwitchMaterial switchCourseUpdates = findViewById(R.id.switch_course_updates);
            SwitchMaterial switchReminders = findViewById(R.id.switch_reminders);
            SwitchMaterial switchAchievements = findViewById(R.id.switch_achievements);
            SwitchMaterial switchAnnouncements = findViewById(R.id.switch_announcements);

            // Load preferences
            db.collection("users").document(uid).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    Boolean prefCourse = snapshot.getBoolean("prefCourseUpdates");
                    Boolean prefReminder = snapshot.getBoolean("prefReminders");
                    Boolean prefAchieve = snapshot.getBoolean("prefAchievements");
                    Boolean prefAnnounce = snapshot.getBoolean("prefAnnouncements");

                    if (switchCourseUpdates != null)
                        switchCourseUpdates.setChecked(prefCourse == null || prefCourse);
                    if (switchReminders != null)
                        switchReminders.setChecked(prefReminder == null || prefReminder);
                    if (switchAchievements != null)
                        switchAchievements.setChecked(prefAchieve == null || prefAchieve);
                    if (switchAnnouncements != null)
                        switchAnnouncements.setChecked(prefAnnounce == null || prefAnnounce);
                }
            });

            // Save on toggle change
            android.widget.CompoundButton.OnCheckedChangeListener prefListener = (buttonView, isChecked) -> {
                java.util.Map<String, Object> notifPrefs = new java.util.HashMap<>();
                if (switchCourseUpdates != null)
                    notifPrefs.put("prefCourseUpdates", switchCourseUpdates.isChecked());
                if (switchReminders != null)
                    notifPrefs.put("prefReminders", switchReminders.isChecked());
                if (switchAchievements != null)
                    notifPrefs.put("prefAchievements", switchAchievements.isChecked());
                if (switchAnnouncements != null)
                    notifPrefs.put("prefAnnouncements", switchAnnouncements.isChecked());

                db.collection("users").document(uid)
                        .set(notifPrefs, com.google.firebase.firestore.SetOptions.merge());
            };

            if (switchCourseUpdates != null)
                switchCourseUpdates.setOnCheckedChangeListener(prefListener);
            if (switchReminders != null)
                switchReminders.setOnCheckedChangeListener(prefListener);
            if (switchAchievements != null)
                switchAchievements.setOnCheckedChangeListener(prefListener);
            if (switchAnnouncements != null)
                switchAnnouncements.setOnCheckedChangeListener(prefListener);

            // 6. Admin Panel Entry (M6.1) - Show only to admins
            com.google.android.material.card.MaterialCardView cardAdminPanel = findViewById(R.id.card_admin_panel);
            if (cardAdminPanel != null) {
                AdminManager.checkIsAdmin(uid, (isAdmin, role) -> {
                    if (isAdmin) {
                        cardAdminPanel.setVisibility(android.view.View.VISIBLE);
                        cardAdminPanel.setOnClickListener(v -> {
                            Intent adminIntent = new Intent(SettingsActivity.this, AdminLoginActivity.class);
                            startActivity(adminIntent);
                        });
                    }
                });
            }
        }

        // 5. Log Out (REAL Firebase sign out)
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
