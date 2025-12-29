package com.example.edubridge.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room entity for storing chat messages with the AI Buddy.
 */
@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey
    @NonNull
    public String id;

    /**
     * The message content.
     */
    public String content;

    /**
     * true = user message, false = AI response
     */
    public boolean isUser;

    /**
     * Timestamp when the message was created.
     */
    public long timestamp;

    public ChatMessage() {
        this.id = "";
    }

    public ChatMessage(@NonNull String id, String content, boolean isUser, long timestamp) {
        this.id = id;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = timestamp;
    }
}
