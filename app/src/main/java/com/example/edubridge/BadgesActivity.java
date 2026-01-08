package com.example.edubridge;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Badges Activity for M4.3 Digital Badges.
 * 
 * Features:
 * - M4.3.2: Display earned badges in user profile
 * - M4.3.3: Categorize badges by type
 * - M4.3.4: View locked badges with unlock conditions
 */
public class BadgesActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private BadgeAdapter adapter;

    private final List<Badge> badges = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    // Header fields (M2.4)
    private TextView tvBadgesXp;
    private TextView tvBadgesCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        // Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null)
            btnBack.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_badges);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BadgeAdapter(badges);
        recycler.setAdapter(adapter);

        // Header (M2.4)
        tvBadgesXp = findViewById(R.id.tv_badges_xp);
        tvBadgesCount = findViewById(R.id.tv_badges_count);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        buildBadgeList();
        listenUser(user.getUid());
    }

    /**
     * Build the complete badge list from BadgeDefinitions (M4.3.3).
     * Badges are grouped by category for display.
     */
    private void buildBadgeList() {
        badges.clear();

        // Get all badges from central definitions (M4.3.3)
        // Already sorted by category in BadgeDefinitions
        badges.addAll(BadgeDefinitions.getAllBadges());

        adapter.notifyDataSetChanged();
    }

    private void listenUser(String uid) {
        if (userListener != null)
            userListener.remove();

        userListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null)
                        return;
                    if (snapshot == null || !snapshot.exists())
                        return;

                    // Prevent duplicate processing from local writes
                    if (snapshot.getMetadata() != null && snapshot.getMetadata().hasPendingWrites()) {
                        updateBadgeUI(snapshot);
                        return;
                    }

                    updateBadgeUI(snapshot);

                    // Check and award any missing badges based on points (M4.3.1)
                    Long points = snapshot.getLong("totalPoints");
                    if (points != null) {
                        BadgeManager.checkAndAwardPointBadges(uid, points);
                    }
                });
    }

    /**
     * Update badge UI based on user data (M4.3.2).
     */
    private void updateBadgeUI(DocumentSnapshot snapshot) {
        long points = 0L;
        Long p = snapshot.getLong("totalPoints");
        if (p != null)
            points = p;

        // Get user's earned badges
        Set<String> unlockedSet = new HashSet<>();
        List<String> unlocked = (List<String>) snapshot.get("badges");
        if (unlocked != null)
            unlockedSet.addAll(unlocked);

        // Update each badge's unlocked state
        for (Badge b : badges) {
            boolean unlockedByRecord = unlockedSet.contains(b.getId());
            boolean unlockedByPoints = (b.getConditionType() == Badge.ConditionType.POINTS)
                    && (points >= b.getConditionValue());
            b.setUnlocked(unlockedByRecord || unlockedByPoints);
        }
        adapter.notifyDataSetChanged();

        // Update header (M2.4)
        if (tvBadgesXp != null) {
            tvBadgesXp.setText(points + " XP");
        }
        if (tvBadgesCount != null) {
            int earnedCount = 0;
            for (Badge b : badges) {
                if (b.isUnlocked())
                    earnedCount++;
            }
            tvBadgesCount.setText(earnedCount + "/" + badges.size() + " Badges Earned");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }
    }
}