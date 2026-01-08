package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for locally cached community posts.
 * Enables offline viewing of posts.
 */
@Entity(tableName = "community_posts")
public class LocalCommunityPost {

    @PrimaryKey
    @NonNull
    public String id; // Firestore document ID

    public String authorId;
    public String userName;
    public String content;
    public long createdAt; // Milliseconds since epoch
    public int avatarRes;

    public LocalCommunityPost() {
        this.id = "";
    }

    public LocalCommunityPost(@NonNull String id, String authorId, String userName,
            String content, long createdAt, int avatarRes) {
        this.id = id;
        this.authorId = authorId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
        this.avatarRes = avatarRes;
    }
}
