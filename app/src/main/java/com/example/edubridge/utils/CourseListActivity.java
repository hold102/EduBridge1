package com.example.edubridge.utils;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.CourseDetailActivity;
import com.example.edubridge.R;

import java.util.ArrayList;

public class CourseListActivity extends AppCompatActivity implements CourseAdapter.OnCourseClickListener {

    private RecyclerView recyclerView;
    private CourseAdapter adapter;
    private ArrayList<Course> courseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_list);

        recyclerView = findViewById(R.id.recyclerView_courses);

        courseList = new ArrayList<>();
        courseList.add(new Course(1, "Mathematics", "Algebra, Geometry & Calculus", R.drawable.ic_subject_math));
        courseList.add(new Course(2, "Science", "Physics, Chemistry & Biology", R.drawable.ic_subject_science));
        courseList.add(new Course(3, "Computer Science", "Python, Java & Web Development", R.drawable.ic_subject_coding));
        courseList.add(new Course(4, "Art & Design", "Sketching, Color Theory & Art History", R.drawable.ic_subject_art));
        courseList.add(new Course(5, "Geography", "World Map, Cultures & Climate", R.drawable.ic_subject_geo));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseAdapter(courseList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("EXTRA_TITLE", course.getTitle());
        intent.putExtra("EXTRA_DESC", course.getDescription());
        intent.putExtra("EXTRA_ICON", course.getImageResId());
        startActivity(intent);
    }
}
