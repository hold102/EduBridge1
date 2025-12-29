package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.entity.Notification;

/**
 * Activity to display the user's notification history.
 */
public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Back button
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Views
        rvNotifications = findViewById(R.id.rv_notifications);
        layoutEmpty = findViewById(R.id.layout_empty);

        // Setup RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this::onNotificationClick);
        rvNotifications.setAdapter(adapter);

        // Observe notifications from Room
        AppDatabase.getInstance(this).notificationDao().getAllNotifications()
                .observe(this, notifications -> {
                    if (notifications == null || notifications.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvNotifications.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvNotifications.setVisibility(View.VISIBLE);
                        adapter.setNotifications(notifications);
                    }
                });
    }

    private void onNotificationClick(Notification notification) {
        // Mark as read
        new Thread(() -> {
            AppDatabase.getInstance(this).notificationDao().markAsRead(notification.id);
        }).start();

        // Navigate to target screen
        Intent intent = null;
        if (notification.screen != null) {
            switch (notification.screen) {
                case "badges":
                    intent = new Intent(this, BadgesActivity.class);
                    break;
                case "leaderboard":
                    intent = new Intent(this, LeaderboardActivity.class);
                    break;
                case "community":
                    intent = new Intent(this, CommunityActivity.class);
                    break;
                case "dashboard":
                default:
                    intent = new Intent(this, DashboardActivity.class);
                    break;
            }
        }

        if (intent != null) {
            startActivity(intent);
        }
    }
}
