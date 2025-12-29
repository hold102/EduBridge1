package com.example.edubridge.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Module 5.1 – Offline Access
 */
public class OfflineManager {

    /* -------- ENTITY -------- */
    @Entity(tableName = "courses")
    public static class Course {

        @PrimaryKey(autoGenerate = true)
        public int id;

        public String title;
        public String description;
        public int iconRes;
    }

    /* -------- DAO -------- */
    @Dao
    interface CourseDao {
        @Query("SELECT * FROM courses")
        List<Course> getAll();

        @Insert
        void insertAll(List<Course> list);
    }

    /* -------- DATABASE -------- */
    @Database(entities = {Course.class}, version = 1)
    abstract static class AppDatabase extends RoomDatabase {
        abstract CourseDao courseDao();
    }

    private static AppDatabase db;

    /* -------- PUBLIC METHODS -------- */
    public static void init(Context context) {
        if (db == null) {
            db = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "edubridge_offline_db"
            ).allowMainThreadQueries().build();
        }
    }

    public static List<Course> getCourses() {
        return db.courseDao().getAll();
    }

    public static void seedIfEmpty() {
        if (getCourses().isEmpty()) {
            List<Course> list = new ArrayList<>();

            Course c1 = new Course();
            c1.title = "Mathematics";
            c1.description = "Algebra & Geometry";

            Course c2 = new Course();
            c2.title = "Science";
            c2.description = "Physics & Biology";

            list.add(c1);
            list.add(c2);

            db.courseDao().insertAll(list);
        }
    }
}
