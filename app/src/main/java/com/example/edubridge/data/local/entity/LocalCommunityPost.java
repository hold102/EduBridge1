package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore; // ✅ 新增
import androidx.room.PrimaryKey;

/**
 * Room entity for locally cached community posts.
 */
@Entity(tableName = "community_posts")
public class LocalCommunityPost {

    @PrimaryKey
    @NonNull
    public String id;

    public String authorId;
    public String userName;
    public String content;
    public long createdAt;
    public int avatarRes;

    public LocalCommunityPost() {
        this.id = "";
    }

    @Ignore
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