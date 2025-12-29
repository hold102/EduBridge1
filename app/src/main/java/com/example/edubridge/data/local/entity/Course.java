package com.example.edubridge.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class CourseEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;

    public CourseEntity(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
