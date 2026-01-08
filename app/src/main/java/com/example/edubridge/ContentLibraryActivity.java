package com.example.edubridge;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edubridge.data.local.OfflineManager;
import com.example.edubridge.data.local.entity.Course;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class ContentLibraryActivity extends AppCompatActivity
        implements CourseBrowseAdapter.OnCourseClickListener {

    private static final String TAG = "ContentLibrary";

    private RecyclerView rvCourses;
    private CourseBrowseAdapter adapter;
    private EditText etSearch;
    private ChipGroup chipGroupCategory, chipGroupDifficulty;
    private TextView tvCourseCount;
    private View layoutEmpty;

    private FirebaseFirestore db;
    private ListenerRegistration courseListener;

    private List<Course> allCourses = new ArrayList<>();
    private List<Course> filteredCourses = new ArrayList<>();

    private String currentCategory = "All";
    private String currentDifficulty = "All";
    private String currentSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content_library);

        // Init Offline Manager (Merged from Local)
        OfflineManager.init(this);
        OfflineManager.seedIfEmpty();

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        setupSearch();
        setupCategoryFilter();
        setupDifficultyFilter();
        loadCourses();
    }

    private void initViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rvCourses = findViewById(R.id.rv_courses);
        etSearch = findViewById(R.id.et_search);
        chipGroupCategory = findViewById(R.id.chip_group_category);
        chipGroupDifficulty = findViewById(R.id.chip_group_difficulty);
        tvCourseCount = findViewById(R.id.tv_course_count);
        layoutEmpty = findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        rvCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourseBrowseAdapter(this, filteredCourses, this);
        rvCourses.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupCategoryFilter() {
        chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String chipText = selectedChip.getText().toString();
                    currentCategory = chipText.equals("All") ? "All" : chipText;
                }
            }
            applyFilters();
        });
    }

    private void setupDifficultyFilter() {
        chipGroupDifficulty.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentDifficulty = "All";
            } else {
                Chip selectedChip = findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String chipText = selectedChip.getText().toString();
                    currentDifficulty = chipText.equals("All Levels") ? "All" : chipText;
                }
            }
            applyFilters();
        });
    }

    private void loadCourses() {
        courseListener = db.collection("courses")
                .whereEqualTo("isPublished", true)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error loading courses", error);
                        if (allCourses.isEmpty()) {
                            loadSampleCourses();
                        }
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        allCourses.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Course course = documentToCourse(doc);
                            if (course != null) {
                                allCourses.add(course);
                            }
                        }
                        applyFilters();
                    } else {
                        loadSampleCourses();
                    }
                });
    }

    private Course documentToCourse(DocumentSnapshot doc) {
        Course course = new Course();
        course.courseId = doc.getId();
        course.title = doc.getString("title");
        course.description = doc.getString("description");
        course.category = doc.getString("category");
        course.difficulty = doc.getString("difficulty");
        course.duration = doc.getString("duration");
        course.thumbnailUrl = doc.getString("thumbnailUrl");

        Long lessons = doc.getLong("lessonCount");
        course.totalLessons = lessons != null ? lessons.intValue() : 0;

        Boolean published = doc.getBoolean("isPublished");
        course.isPublished = published != null && published;

        return course;
    }

    private void loadSampleCourses() {
        allCourses.clear();

        // Mathematics
        allCourses.add(createSampleCourse("math1", "Basic Algebra",
                "Learn fundamental algebraic concepts and equations",
                "Mathematics", "Beginner", "2 hours", 8));
        allCourses.add(createSampleCourse("math2", "Calculus 101",
                "Introduction to derivatives and integrals",
                "Mathematics", "Intermediate", "4 hours", 12));
        allCourses.add(createSampleCourse("math3", "Advanced Statistics",
                "Statistical analysis and probability theory",
                "Mathematics", "Advanced", "6 hours", 15));

        // Science
        allCourses.add(createSampleCourse("sci1", "Intro to Physics",
                "Newton's laws, motion, and forces explained",
                "Science", "Beginner", "3 hours", 10));
        allCourses.add(createSampleCourse("sci2", "Organic Chemistry",
                "Carbon compounds and molecular structures",
                "Science", "Advanced", "5 hours", 14));
        allCourses.add(createSampleCourse("sci3", "Biology Fundamentals",
                "Cell structure, genetics, and ecosystems",
                "Science", "Beginner", "3 hours", 9));

        // Coding
        allCourses.add(createSampleCourse("code1", "Java Fundamentals",
                "Object-oriented programming with Java",
                "Coding", "Beginner", "4 hours", 12));
        allCourses.add(createSampleCourse("code2", "Web Development",
                "HTML, CSS, and JavaScript basics",
                "Coding", "Intermediate", "5 hours", 15));
        allCourses.add(createSampleCourse("code3", "Python for Data Science",
                "Data analysis and machine learning",
                "Coding", "Advanced", "8 hours", 20));

        // Art
        allCourses.add(createSampleCourse("art1", "Drawing Basics",
                "Sketching, shading, and perspective",
                "Art", "Beginner", "2 hours", 6));
        allCourses.add(createSampleCourse("art2", "Digital Illustration",
                "Create art using digital tools",
                "Art", "Intermediate", "4 hours", 10));

        // Geography
        allCourses.add(createSampleCourse("geo1", "World Geography",
                "Countries, capitals, and cultures",
                "Geography", "Beginner", "2 hours", 8));

        applyFilters();
        Log.d(TAG, "Loaded " + allCourses.size() + " sample courses");
    }

    private Course createSampleCourse(String id, String title, String desc,
                                      String category, String difficulty,
                                      String duration, int lessons) {
        Course course = new Course(id, title, desc, category, difficulty, duration, lessons, true);
        course.instructorName = "EduBridge Instructor";
        course.instructorBio = "Expert educator";
        course.estimatedEffort = "2-3 hrs/week";
        course.objectives = "• Master core concepts\n• Apply knowledge practically\n• Build real skills";
        return course;
    }

    private void applyFilters() {
        filteredCourses.clear();
        for (Course course : allCourses) {
            boolean matchesSearch = course.matchesSearch(currentSearch);
            boolean matchesCategory = course.matchesCategory(currentCategory);
            boolean matchesDifficulty = course.matchesDifficulty(currentDifficulty);

            if (matchesSearch && matchesCategory && matchesDifficulty) {
                filteredCourses.add(course);
            }
        }
        adapter.updateCourses(filteredCourses);
        updateCourseCount();
        updateEmptyState();
    }

    private void updateCourseCount() {
        int count = filteredCourses.size();
        tvCourseCount.setText(count + (count == 1 ? " course found" : " courses found"));
    }

    private void updateEmptyState() {
        boolean isEmpty = filteredCourses.isEmpty();
        layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvCourses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onCourseClick(Course course) {
        Intent intent = new Intent(this, CourseDetailActivity.class);
        intent.putExtra("EXTRA_COURSE_ID", course.courseId);
        intent.putExtra("EXTRA_TITLE", course.title);
        intent.putExtra("EXTRA_DESC", course.description);
        intent.putExtra("EXTRA_CATEGORY", course.category);
        intent.putExtra("EXTRA_DIFFICULTY", course.difficulty);
        intent.putExtra("EXTRA_DURATION", course.duration);
        intent.putExtra("EXTRA_LESSONS", course.totalLessons);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (courseListener != null) {
            courseListener.remove();
        }
    }
}