package com.example.edubridge.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.edubridge.data.local.entity.Course;

import java.util.List;

@Dao
public interface CourseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Course course);

    @Query("SELECT * FROM courses")
    LiveData<List<Course>> getAllCourses();

    @Query("SELECT * FROM courses")
    List<Course> getAllCoursesSync();

    @Query("SELECT * FROM courses WHERE courseId = :courseId LIMIT 1")
    Course getCourseByIdSync(String courseId);

    @Query("DELETE FROM courses")
    void deleteAll();
}
