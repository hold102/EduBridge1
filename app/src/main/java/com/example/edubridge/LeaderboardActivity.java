package com.example.edubridge;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private LeaderboardAdapter adapter;
    private final List<Leader> leaders = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration listener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        // ✅ Back button in XML (btn_back)
        View back = findViewById(R.id.btn_back);
        if (back != null) back.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler_leaderboard);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LeaderboardAdapter(this, leaders);
        recycler.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        listener = db.collection("users")
                .orderBy("totalPoints", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (value == null) return;

                    leaders.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String uid = doc.getId();

                        String name = doc.getString("name");
                        if (name == null || name.trim().isEmpty()) name = doc.getString("userName");
                        if (name == null || name.trim().isEmpty()) name = "User";

                        Long points = doc.getLong("totalPoints");
                        if (points == null) points = 0L;

                        List<String> badges = (List<String>) doc.get("badges");
                        int badgeCount = (badges == null) ? 0 : badges.size();

                        leaders.add(new Leader(uid, name, points, badgeCount));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            listener.remove();
            listener = null;
        }
    }
}
