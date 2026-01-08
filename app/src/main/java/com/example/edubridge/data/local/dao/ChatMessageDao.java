package com.example.edubridge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.edubridge.data.local.entity.ChatMessage;

import java.util.List;

/**
 * Data Access Object for ChatMessage entities.
 */
@Dao
public interface ChatMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatMessage message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getAllMessages();

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessagesSync();

    @Query("DELETE FROM chat_messages")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM chat_messages")
    int getMessageCount();
}
