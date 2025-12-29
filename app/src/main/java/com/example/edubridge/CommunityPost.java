package com.example.edubridge;

import com.google.firebase.Timestamp;

public class CommunityPost {
    private String id;          // Firestore docId（本地用）
    private String authorId;    // ✅ 发布者 uid（Firestore 存）
    private String userName;
    private String content;
    private Timestamp createdAt;
    private int avatarRes;

    public CommunityPost() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getAvatarRes() { return avatarRes; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }
}
