package com.example.edubridge;

import com.google.firebase.Timestamp;

public class Reply {

    private String id;            // docId
    private String authorId;      // uid
    private String userName;      // display name
    private String content;       // reply text
    private String replyToName;   // 方案A：回复谁（@name）
    private Timestamp createdAt;  // time

    public Reply() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getReplyToName() { return replyToName; }
    public void setReplyToName(String replyToName) { this.replyToName = replyToName; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
