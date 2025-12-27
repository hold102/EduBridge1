package com.example.edubridge;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BadgesActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private BadgeAdapter adapter;

    private final List<Badge> badges = new ArrayList<>();

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ListenerRegistration userListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        // Back
        View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_badges);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BadgeAdapter(badges);
        recycler.setAdapter(adapter);

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

    // ✅ 给每个 badge 一个稳定的 id（写入 Firestore 用）
    private void buildBadgeList() {
        badges.clear();
        badges.add(new Badge("first_post", "First Post", "Earn 5 points", 5));
        badges.add(new Badge("contributor", "Contributor", "Earn 20 points", 20));
        badges.add(new Badge("rising_star", "Rising Star", "Earn 50 points", 50));
        badges.add(new Badge("community_hero", "Community Hero", "Earn 100 points", 100));
        badges.add(new Badge("legend", "Legend", "Earn 200 points", 200));
        adapter.notifyDataSetChanged();
    }

    private void listenUser(String uid) {
        if (userListener != null) userListener.remove();

        userListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) return;
                    if (snapshot == null || !snapshot.exists()) return;

                    // ✅ 防止本地写入造成重复 Toast
                    if (snapshot.getMetadata() != null && snapshot.getMetadata().hasPendingWrites()) {
                        // 仍然更新 UI，但不触发 unlock/toast
                        updateBadgeUI(snapshot);
                        return;
                    }

                    updateBadgeUI(snapshot);
                    unlockMissingBadgesIfNeeded(uid, snapshot);
                });
    }

    private void updateBadgeUI(DocumentSnapshot snapshot) {
        long points = 0L;
        Long p = snapshot.getLong("totalPoints");
        if (p != null) points = p;

        Set<String> unlockedSet = new HashSet<>();
        List<String> unlocked = (List<String>) snapshot.get("badges");
        if (unlocked != null) unlockedSet.addAll(unlocked);

        for (Badge b : badges) {
            boolean unlockedByRecord = unlockedSet.contains(b.getId());
            boolean unlockedByPoints = points >= b.getRequiredPoints();
            b.setUnlocked(unlockedByRecord || unlockedByPoints);
        }
        adapter.notifyDataSetChanged();
    }

    private void unlockMissingBadgesIfNeeded(String uid, DocumentSnapshot snapshot) {
        long points = 0L;
        Long p = snapshot.getLong("totalPoints");
        if (p != null) points = p;

        Set<String> unlockedSet = new HashSet<>();
        List<String> unlocked = (List<String>) snapshot.get("badges");
        if (unlocked != null) unlockedSet.addAll(unlocked);

        // ✅ 如果 points 达到门槛但 badges 里还没有 -> 触发 toast + 写入 arrayUnion
        for (Badge b : badges) {
            boolean reached = points >= b.getRequiredPoints();
            boolean alreadyRecorded = unlockedSet.contains(b.getId());

            if (reached && !alreadyRecorded) {
                Toast.makeText(this, "New badge unlocked: " + b.getTitle(), Toast.LENGTH_LONG).show();

                db.collection("users")
                        .document(uid)
                        .update("badges", FieldValue.arrayUnion(b.getId()));
            }
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