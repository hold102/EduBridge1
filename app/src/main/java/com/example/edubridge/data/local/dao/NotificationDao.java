package com.example.edubridge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.edubridge.data.local.entity.Notification;

import java.util.List;

/**
 * Data Access Object for Notification entity.
 */
@Dao
public interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Notification notification);

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    LiveData<List<Notification>> getAllNotifications();

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    void markAsRead(String notificationId);

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    LiveData<Integer> getUnreadCount();

    @Query("DELETE FROM notifications")
    void deleteAll();
}
