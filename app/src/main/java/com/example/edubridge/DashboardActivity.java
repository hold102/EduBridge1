package com.example.edubridge;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.edubridge.utils.SyncManager;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private static final int REQ_POST_NOTIF = 2001;
    private static final String SP_NAME = "edubridge_sp";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView tvUsername;
    private TextView tvPoints;
    private ListenerRegistration userDocListener;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
            return;
        }

        if (!currentUser.isEmailVerified()) {
            Toast.makeText(this, "Email not verified. Please verify first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(DashboardActivity.this, VerifyEmailActivity.class));
            finish();
            return;
        }

        currentUid = currentUser.getUid();
        setContentView(R.layout.activity_dashboard);

        if (!SyncManager.isOnline(this)) {
            Toast.makeText(this, "Offline mode: using cached data", Toast.LENGTH_SHORT).show();
        }

        tvUsername = findViewById(R.id.tv_username);
        tvPoints = findViewById(R.id.tv_points);

        setUsernameFromAuth(currentUser);
        listenUserPoints(currentUid);
        ensureNotificationPermissionThenSyncToken();

        setupCard(R.id.card_content_library, ContentLibraryActivity.class);
        setupCard(R.id.card_community, CommunityActivity.class);
        setupCard(R.id.card_learning_buddy, LearningBuddyActivity.class);
        setupCard(R.id.card_study_planner, StudyPlannerActivity.class);
        setupCard(R.id.card_profile, ProfileActivity.class);
        setupCard(R.id.card_settings, SettingsActivity.class);
        setupCard(R.id.card_badges, BadgesActivity.class);

        MaterialCardView pointsCard = findViewById(R.id.card_points);
        if (pointsCard != null) {
            pointsCard.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, LeaderboardActivity.class))
            );
        }

        View streakLayout = findViewById(R.id.layout_streak);
        if (streakLayout != null) {
            streakLayout.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, DailyCheckInActivity.class))
            );
        }
    }

    private void setUsernameFromAuth(FirebaseUser user) {
        String name;
        if (!TextUtils.isEmpty(user.getDisplayName())) {
            name = user.getDisplayName();
        } else if (!TextUtils.isEmpty(user.getEmail())) {
            String email = user.getEmail();
            int at = email.indexOf("@");
            name = (at > 0) ? email.substring(0, at) : email;
        } else {
            name = "Student";
        }
        if (tvUsername != null) tvUsername.setText(name);
    }

    private void listenUserPoints(String uid) {
        if (userDocListener != null) userDocListener.remove();
        userDocListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        Long points = snapshot.getLong("totalPoints");
                        if (tvPoints != null) tvPoints.setText("Points: " + (points != null ? points : 0));
                    }
                });
    }

    private void setupCard(int cardId, Class<?> destinationActivity) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, destinationActivity))
            );
        }
    }

    private void ensureNotificationPermissionThenSyncToken() {
        if (TextUtils.isEmpty(currentUid)) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                syncFcmTokenToFirestore(currentUid);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIF);
            }
        } else {
            syncFcmTokenToFirestore(currentUid);
        }
    }

    private void syncFcmTokenToFirestore(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (TextUtils.isEmpty(token)) return;
                    Log.d(TAG, "FCM Token = " + token);
                    getSharedPreferences(SP_NAME, MODE_PRIVATE).edit().putString(KEY_FCM_TOKEN, token).apply();
                    Map<String, Object> update = new HashMap<>();
                    update.put("fcmToken", token);
                    update.put("fcmUpdatedAt", FieldValue.serverTimestamp());
                    db.collection("users").document(uid).set(update, SetOptions.merge());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Get token failed: " + e.getMessage()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIF && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            syncFcmTokenToFirestore(currentUid);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDocListener != null) {
            userDocListener.remove();
            userDocListener = null;
        }
    }
}