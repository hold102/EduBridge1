package com.example.edubridge.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CourseDao {

    @Insert
    void insertCourse(CourseEntity course);

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();
}
