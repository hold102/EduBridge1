package com.example.edubridge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.edubridge.utils.Course;

import java.util.ArrayList;
import java.util.List;

public class ContentLibraryActivity extends AppCompatActivity {

    private LinearLayout courseContainer;
    private List<Course> allCourses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_library);

        courseContainer = findViewById(R.id.course_container);

        initCourseData();
        displayCourses(allCourses);

        SearchView searchView = findViewById(R.id.searchView_course);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String keyword) {
                filterCourses(keyword.toLowerCase());
                return true;
            }
        });
    }

    // Initialize all courses with subject, difficulty and learning path
    private void initCourseData() {
        allCourses = new ArrayList<>();

        allCourses.add(new Course(
                "Mathematics",
                "Algebra, Geometry & Calculus",
                "Science",
                "Beginner",
                "Academic",
                R.drawable.ic_subject_math));

        allCourses.add(new Course(
                "Physics",
                "Mechanics, Waves & Energy",
                "Science",
                "Intermediate",
                "Academic",
                R.drawable.ic_subject_science));

        allCourses.add(new Course(
                "Computer Science",
                "Python, Java & Web Development",
                "Technology",
                "Beginner",
                "Skill",
                R.drawable.ic_subject_coding));

        allCourses.add(new Course(
                "Art & Design",
                "Sketching and Color Theory",
                "Arts",
                "Beginner",
                "Creative",
                R.drawable.ic_subject_art));

        allCourses.add(new Course(
                "Geography",
                "Maps, Climate and Cultures",
                "Humanities",
                "Advanced",
                "Exam",
                R.drawable.ic_subject_geo));
    }

    // Display courses dynamically
    private void displayCourses(List<Course> courses) {
        courseContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (Course course : courses) {
            View item = inflater.inflate(R.layout.item_course, courseContainer, false);

            TextView title = item.findViewById(R.id.tv_course_title);
            TextView desc = item.findViewById(R.id.tv_course_desc);
            ImageView icon = item.findViewById(R.id.img_course_icon);

            title.setText(course.getTitle());
            desc.setText(course.getDescription()
                    + "\nDifficulty: " + course.getDifficulty()
                    + " | Path: " + course.getLearningPath());
            icon.setImageResource(course.getImageResId());

            courseContainer.addView(item);
        }
    }

    // Keyword-based filtering across all attributes
    private void filterCourses(String keyword) {
        List<Course> filtered = new ArrayList<>();

        for (Course c : allCourses) {
            if (c.getTitle().toLowerCase().contains(keyword)
                    || c.getSubject().toLowerCase().contains(keyword)
                    || c.getDifficulty().toLowerCase().contains(keyword)
                    || c.getLearningPath().toLowerCase().contains(keyword)) {

                filtered.add(c);
            }
        }

        displayCourses(filtered);
    }
}
