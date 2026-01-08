package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity representing a notification stored locally.
 * Used for displaying notification history in the app.
 */
@Entity(tableName = "notifications")
public class Notification {

    @PrimaryKey
    @NonNull
    public String id;

    public String title;
    public String body;

    /**
     * Notification type: course_update, reminder, achievement, announcement
     */
    public String type;

    /**
     * Deeplink target screen: dashboard, badges, leaderboard, community, etc.
     */
    public String screen;

    public boolean isRead;

    /**
     * Timestamp in milliseconds when the notification was received.
     */
    public long timestamp;
}
