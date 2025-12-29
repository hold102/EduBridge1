// file: data/local/AppDatabase.java
package com.example.edubridge.data.local; // Assumes your package name

import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.edubridge.data.local.dao.ChatMessageDao;
import com.example.edubridge.data.local.dao.CommunityPostDao;
import com.example.edubridge.data.local.dao.CourseDao;
import com.example.edubridge.data.local.dao.NotificationDao;
import com.example.edubridge.data.local.dao.PlannerTaskDao;
import com.example.edubridge.data.local.entity.ChatMessage;
import com.example.edubridge.data.local.entity.Course;
import com.example.edubridge.data.local.entity.LocalCommunityPost;
import com.example.edubridge.data.local.entity.Notification;
import com.example.edubridge.data.local.entity.PlannerTask;

/**
 * The main database class for the application.
 * Utilizes the Singleton pattern to ensure a single database instance
 * lifecycle.
 * <p>
 * Version 8: Added LocalCommunityPost entity for offline community posts.
 * </p>
 */
@Database(entities = { Course.class, Notification.class, ChatMessage.class,
        PlannerTask.class, LocalCommunityPost.class }, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CourseDao courseDao();

    public abstract NotificationDao notificationDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract PlannerTaskDao plannerTaskDao();

    public abstract CommunityPostDao communityPostDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "edubridge_db")
                            .fallbackToDestructiveMigration() // For dev environment simplicity
                            .allowMainThreadQueries() // Simple MVP approach, discouraged in prod
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
