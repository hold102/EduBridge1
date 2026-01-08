package com.example.edubridge;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.edubridge.data.local.AppDatabase;
import com.example.edubridge.data.local.entity.Course;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

/**
 * The main Dashboard Activity for the application.
 * <p>
 * Functional Requirements:
 * 1. M2.1 Progress Dashboard: Displays real-time learning progress, including
 * overall completion percentage and detailed course-level progress.
 * Data is synced from the local Room database to support offline access.
 * 2. Navigation Hub: Provides access to other modules like Library, Community,
 * etc.
 * </p>
 */
public class DashboardActivity extends AppCompatActivity {

    private static final String TAG = "DashboardActivity";
    private static final int REQ_POST_NOTIF = 2001;

    // Save token locally (optional)
    private static final String SP_NAME = "edubridge_sp";
    private static final String KEY_FCM_TOKEN = "fcm_token";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private TextView tvUsername;
    private TextView tvPoints;

    private ListenerRegistration userDocListener;
    private String currentUid;

    // Progress Dashboard Fields
    private RecyclerView rvCourseProgress;
    private CourseProgressAdapter courseAdapter;
    private TextView tvOverallProgress;
    private LinearProgressIndicator pbOverallProgress;

    // Achievement Summary Fields (M2.4)
    private TextView tvAchievementXp;
    private TextView tvBadgeCount;
    private LinearLayout layoutRecentBadges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // ✅ Auth Guard
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // ✅ Email Verification Guard
        if (!currentUser.isEmailVerified()) {
            Toast.makeText(this, "Email not verified. Please verify first.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(DashboardActivity.this, VerifyEmailActivity.class));
            finish();
            return;
        }

        currentUid = currentUser.getUid();

        setContentView(R.layout.activity_dashboard);

        // M2.1 Progress Dashboard
        setupProgressDashboard();

        // M2.2 Course Recommendation
        setupRecommendationEngine();

        // Bind
        tvUsername = findViewById(R.id.tv_username);
        tvPoints = findViewById(R.id.tv_points);

        // username
        setUsernameFromAuth(currentUser);

        // points & goal realtime listener
        listenUserData(currentUid);

        // ✅ M2.3 token
        ensureNotificationPermissionThenSyncToken();

        // ✅ M2.1 Online/Offline Sync
        syncFirestoreToLocal(currentUid);

        // ✅ Dashboard cards (your original ids)
        setupCard(R.id.card_content_library, ContentLibraryActivity.class);
        setupCard(R.id.card_community, CommunityActivity.class);
        setupCard(R.id.card_learning_buddy, LearningBuddyActivity.class);
        setupCard(R.id.card_study_planner, StudyPlannerActivity.class);
        setupCard(R.id.card_profile, ProfileActivity.class);
        setupCard(R.id.card_settings, SettingsActivity.class);

        // M2.3 Notification Bell
        findViewById(R.id.btn_notification).setOnClickListener(
                v -> startActivity(new Intent(DashboardActivity.this, NotificationsActivity.class)));

        // DEBUG: Seed dummy notifications for verification
        seedDummyNotifications();

        // Seed dummy courses for local database
        seedDummyCourses();

        setupCard(R.id.card_badges, BadgesActivity.class);

        // M2.4 Achievement Summary
        tvAchievementXp = findViewById(R.id.tv_achievement_xp);
        tvBadgeCount = findViewById(R.id.tv_badge_count);
        layoutRecentBadges = findViewById(R.id.layout_recent_badges);
        findViewById(R.id.btn_view_all_badges).setOnClickListener(
                v -> startActivity(new Intent(DashboardActivity.this, BadgesActivity.class)));

        // ✅ Points box -> Leaderboard (IMPORTANT: use card_points, not tv_points)
        MaterialCardView pointsCard = findViewById(R.id.card_points);
        if (pointsCard != null) {
            pointsCard.setOnClickListener(
                    v -> startActivity(new Intent(DashboardActivity.this, LeaderboardActivity.class)));
        } else {
            // fallback (if card not found)
            if (tvPoints != null) {
                tvPoints.setOnClickListener(
                        v -> startActivity(new Intent(DashboardActivity.this, LeaderboardActivity.class)));
            }
        }

        // ✅ 3 Days -> Daily Check-In
        View streakLayout = findViewById(R.id.layout_streak);
        if (streakLayout != null) {
            streakLayout.setOnClickListener(
                    v -> startActivity(new Intent(DashboardActivity.this, DailyCheckInActivity.class)));
        }

        // ✅ Home button -> Logout and navigate to Login
        View btnHome = findViewById(R.id.btn_home);
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                auth.signOut();
                Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setUsernameFromAuth(FirebaseUser user) {
        String name;

        if (!TextUtils.isEmpty(user.getDisplayName())) {
            name = user.getDisplayName();
        } else if (!TextUtils.isEmpty(user.getEmail())) {
            String email = user.getEmail();
            int at = email.indexOf("@");
            name = (at > 0) ? email.substring(0, at) : email;
        } else {
            name = "Student";
        }

        if (tvUsername != null)
            tvUsername.setText(name);
    }

    private String userLearningGoal;
    private List<Course> cachedCourses;

    private void listenUserData(String uid) {
        if (tvPoints != null)
            tvPoints.setText("Points: 0");

        if (userDocListener != null)
            userDocListener.remove();

        userDocListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null)
                        return;

                    if (snapshot == null || !snapshot.exists()) {
                        if (tvPoints != null)
                            tvPoints.setText("Points: 0");
                        return;
                    }

                    // 1. Points
                    Long points = snapshot.getLong("totalPoints");
                    if (points == null)
                        points = 0L;
                    if (tvPoints != null)
                        tvPoints.setText("Points: " + points);

                    // 2. Streak Count - update dashboard display
                    Long streakCount = snapshot.getLong("streakCount");
                    if (streakCount == null)
                        streakCount = 0L;
                    TextView tvStreakDays = findViewById(R.id.tv_streak_days);
                    if (tvStreakDays != null) {
                        tvStreakDays.setText(streakCount + " Days");
                    }

                    // 3. Learning Goal (for M2.2)
                    String goal = snapshot.getString("learningGoal");
                    boolean goalChanged = (goal != null && !goal.equals(userLearningGoal))
                            || (goal == null && userLearningGoal != null);

                    userLearningGoal = goal;

                    // Refresh recommendations if goal matches changed
                    if (goalChanged) {
                        refreshRecommendations();
                    }

                    // 4. Achievement Summary (M2.4)
                    updateAchievementSummary(points, snapshot.get("badges"));

                    // 5. Level Progression (M4.2)
                    updateLevelProgression(points);

                    // 6. Level-Up Detection (M4.2.4 & M4.2.5)
                    Long storedLevel = snapshot.getLong("userLevel");
                    int currentLevel = LevelManager.getLevelForXp(points);
                    if (storedLevel != null && currentLevel > storedLevel.intValue()) {
                        // Level up detected - show notification
                        showLevelUpNotification(currentLevel);
                    }
                    // Update stored level if needed
                    if (storedLevel == null || storedLevel.intValue() != currentLevel) {
                        db.collection("users").document(uid)
                                .update("userLevel", currentLevel);
                    }
                });
    }

    private void setupCard(int cardId, Class<?> destinationActivity) {
        MaterialCardView card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, destinationActivity)));
        }
    }

    private void ensureNotificationPermissionThenSyncToken() {
        if (TextUtils.isEmpty(currentUid))
            return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                syncFcmTokenToFirestore(currentUid);
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] { Manifest.permission.POST_NOTIFICATIONS },
                        REQ_POST_NOTIF);
            }
        } else {
            syncFcmTokenToFirestore(currentUid);
        }
    }

    // ✅ Get token and write into Firestore + save + logcat
    private void syncFcmTokenToFirestore(String uid) {
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    if (TextUtils.isEmpty(token))
                        return;

                    Log.d(TAG, "FCM Token = " + token);

                    SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
                    sp.edit().putString(KEY_FCM_TOKEN, token).apply();

                    Map<String, Object> update = new HashMap<>();
                    update.put("fcmToken", token);
                    update.put("fcmUpdatedAt", FieldValue.serverTimestamp());

                    db.collection("users").document(uid).set(update, SetOptions.merge());
                })
                .addOnFailureListener(e -> Log.e(TAG, "Get token failed: " + e.getMessage()));
    }

    public String getSavedFcmToken() {
        SharedPreferences sp = getSharedPreferences(SP_NAME, MODE_PRIVATE);
        return sp.getString(KEY_FCM_TOKEN, "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIF) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled ✅", Toast.LENGTH_SHORT).show();
                if (!TextUtils.isEmpty(currentUid))
                    syncFcmTokenToFirestore(currentUid);
            } else {
                Toast.makeText(this, "Notifications disabled (you can enable in settings).", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Initializes the Progress Dashboard submodule (M2.1).
     * Now loads from user's Firestore enrollments and shows only in-progress
     * courses.
     * Courses are clickable to navigate to CourseDetail.
     */
    private void setupProgressDashboard() {
        rvCourseProgress = findViewById(R.id.rv_course_progress);
        tvOverallProgress = findViewById(R.id.tv_overall_progress);
        pbOverallProgress = findViewById(R.id.pb_overall_progress);

        if (rvCourseProgress != null) {
            rvCourseProgress.setLayoutManager(new LinearLayoutManager(this));
            courseAdapter = new CourseProgressAdapter(this::openCourseDetails);
            rvCourseProgress.setAdapter(courseAdapter);
            rvCourseProgress.setNestedScrollingEnabled(true);
        }

        // Load enrolled courses from Firestore with real-time updates
        loadEnrolledCoursesProgress();
    }

    private ListenerRegistration progressListener;

    private void loadEnrolledCoursesProgress() {
        if (currentUid == null) {
            updateProgressUI(new ArrayList<>());
            return;
        }

        // Listen to user's enrollments for real-time progress updates
        progressListener = db.collection("users").document(currentUid)
                .collection("enrollments")
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Error loading enrollment progress", error);
                        return;
                    }

                    List<Course> inProgressCourses = new ArrayList<>();

                    if (snapshots != null) {
                        for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                            String status = doc.getString("status");

                            // Only include courses that are enrolled or in_progress (not completed)
                            if (status != null && !status.equals("completed")) {
                                Course course = new Course();
                                course.courseId = doc.getId();
                                course.title = doc.getString("courseTitle");
                                course.category = doc.getString("category");

                                Long progress = doc.getLong("progress");
                                Long total = doc.getLong("totalLessons");

                                course.progress = progress != null ? progress.intValue() : 0;
                                course.totalLessons = total != null ? total.intValue() : 10;
                                course.status = course.progress > 0 ? "In Progress" : "Enrolled";

                                inProgressCourses.add(course);
                            }
                        }
                    }

                    updateProgressUI(inProgressCourses);
                });
    }

    // Recommendation UI Fields
    private RecyclerView rvRecommendations;
    private RecommendationAdapter recommendationAdapter;
    private List<Course> recommendationCourses = new ArrayList<>();
    private ListenerRegistration enrollmentListener;

    private void setupRecommendationEngine() {
        rvRecommendations = findViewById(R.id.rv_recommendations);
        if (rvRecommendations != null) {
            rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recommendationAdapter = new RecommendationAdapter(this::openCourseDetails);
            rvRecommendations.setAdapter(recommendationAdapter);
        }

        // Load all available courses for recommendations
        loadRecommendationCourses();

        // Listen for enrollment changes to update recommendations in real-time
        if (currentUid != null) {
            enrollmentListener = db.collection("users").document(currentUid)
                    .collection("enrollments")
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null)
                            return;
                        // Refresh recommendations when enrollments change
                        refreshRecommendations();
                    });
        }
    }

    private void loadRecommendationCourses() {
        recommendationCourses.clear();

        // Same 12 sample courses as ContentLibrary
        recommendationCourses.add(createRecommendationCourse("math1", "Basic Algebra",
                "Learn fundamental algebraic concepts", "Mathematics", "Beginner", "2 hours", 8));
        recommendationCourses.add(createRecommendationCourse("math2", "Calculus 101",
                "Introduction to derivatives and integrals", "Mathematics", "Intermediate", "4 hours", 12));
        recommendationCourses.add(createRecommendationCourse("math3", "Advanced Statistics",
                "Statistical analysis and probability", "Mathematics", "Advanced", "6 hours", 15));

        recommendationCourses.add(createRecommendationCourse("sci1", "Intro to Physics",
                "Newton's laws, motion, and forces", "Science", "Beginner", "3 hours", 10));
        recommendationCourses.add(createRecommendationCourse("sci2", "Organic Chemistry",
                "Carbon compounds and structures", "Science", "Advanced", "5 hours", 14));
        recommendationCourses.add(createRecommendationCourse("sci3", "Biology Fundamentals",
                "Cell structure and genetics", "Science", "Beginner", "3 hours", 9));

        recommendationCourses.add(createRecommendationCourse("code1", "Java Fundamentals",
                "Object-oriented programming", "Coding", "Beginner", "4 hours", 12));
        recommendationCourses.add(createRecommendationCourse("code2", "Web Development",
                "HTML, CSS, and JavaScript", "Coding", "Intermediate", "5 hours", 15));
        recommendationCourses.add(createRecommendationCourse("code3", "Python for Data Science",
                "Data analysis and ML", "Coding", "Advanced", "8 hours", 20));

        recommendationCourses.add(createRecommendationCourse("art1", "Drawing Basics",
                "Sketching and shading", "Art", "Beginner", "2 hours", 6));
        recommendationCourses.add(createRecommendationCourse("art2", "Digital Illustration",
                "Create art with digital tools", "Art", "Intermediate", "4 hours", 10));

        recommendationCourses.add(createRecommendationCourse("geo1", "World Geography",
                "Countries, capitals, and cultures", "Geography", "Beginner", "2 hours", 8));

        refreshRecommendations();
    }

    private Course createRecommendationCourse(String id, String title, String desc,
            String category, String difficulty,
            String duration, int lessons) {
        Course c = new Course();
        c.courseId = id;
        c.title = title;
        c.description = desc;
        c.category = category;
        c.difficulty = difficulty;
        c.duration = duration;
        c.totalLessons = lessons;
        c.status = "Not Started";
        c.isPublished = true;
        return c;
    }

    private void refreshRecommendations() {
        if (currentUid == null || recommendationCourses.isEmpty()) {
            applyRecommendationSorting(new ArrayList<>(recommendationCourses));
            return;
        }

        // Fetch enrolled course IDs and filter
        db.collection("users").document(currentUid)
                .collection("enrollments")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> enrolledIds = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        enrolledIds.add(doc.getId());
                    }

                    // Filter out enrolled courses
                    List<Course> filtered = new ArrayList<>();
                    for (Course c : recommendationCourses) {
                        if (!enrolledIds.contains(c.courseId)) {
                            filtered.add(c);
                        }
                    }

                    applyRecommendationSorting(filtered);
                })
                .addOnFailureListener(e -> {
                    applyRecommendationSorting(new ArrayList<>(recommendationCourses));
                });
    }

    private void openCourseDetails(Course course) {
        Intent intent = new Intent(DashboardActivity.this, CourseDetailActivity.class);
        intent.putExtra("EXTRA_COURSE_ID", course.courseId);
        intent.putExtra("EXTRA_TITLE", course.title);
        intent.putExtra("EXTRA_DESC", course.description);
        intent.putExtra("EXTRA_CATEGORY", course.category);
        intent.putExtra("EXTRA_DIFFICULTY", course.difficulty);
        intent.putExtra("EXTRA_DURATION", course.duration);
        intent.putExtra("EXTRA_LESSONS", course.totalLessons);
        startActivity(intent);
    }

    private void applyRecommendationSorting(List<Course> courses) {
        if (courses.isEmpty()) {
            if (recommendationAdapter != null) {
                recommendationAdapter.setRecommendations(courses);
            }
            return;
        }

        // Sorting Logic:
        // 1. Goal Match: Matches userLearningGoal (Yes > No)
        // 2. Status: Not Started (1) -> In Progress (2) -> Completed (3)
        // 3. Difficulty: Beginner (1) -> Intermediate (2) -> Advanced (3)
        courses.sort((c1, c2) -> {
            // Priority 0: Goal Match
            boolean m1 = isGoalMatch(c1);
            boolean m2 = isGoalMatch(c2);
            if (m1 != m2) {
                return m1 ? -1 : 1; // True comes first
            }

            // Priority 1: Status
            int s1 = getStatusPriority(c1.status);
            int s2 = getStatusPriority(c2.status);
            if (s1 != s2) {
                return Integer.compare(s1, s2);
            }

            // Priority 2: Difficulty
            int d1 = getDifficultyPriority(c1.difficulty);
            int d2 = getDifficultyPriority(c2.difficulty);
            return Integer.compare(d1, d2);
        });

        // Limit to 5 recommendations
        List<Course> limited = courses.size() > 5 ? courses.subList(0, 5) : courses;

        if (recommendationAdapter != null) {
            recommendationAdapter.setRecommendations(limited);
        }
    }

    private boolean isGoalMatch(Course course) {
        if (userLearningGoal == null || userLearningGoal.isEmpty())
            return false;
        String goal = userLearningGoal.toLowerCase().trim();

        // Priority Match: Category (Primary) with Bidirectional Check
        if (course.category != null) {
            String cat = course.category.toLowerCase();
            // "math".contains("mathematics") is FALSE. "mathematics".contains("math") is
            // TRUE.
            // Check if Category contains Goal OR Goal contains Category
            if (cat.contains(goal) || goal.contains(cat)) {
                return true;
            }
        }

        // Fallback: Title or Desc (Secondary)
        boolean inTitle = (course.title != null && course.title.toLowerCase().contains(goal));
        boolean inDesc = (course.description != null && course.description.toLowerCase().contains(goal));

        return inTitle || inDesc;
    }

    private int getStatusPriority(String status) {
        if (status == null)
            return 4;
        if (status.equalsIgnoreCase("Not Started"))
            return 1;
        if (status.equalsIgnoreCase("In Progress"))
            return 2;
        if (status.equalsIgnoreCase("Completed"))
            return 3;
        return 4; // Unknown
    }

    private int getDifficultyPriority(String difficulty) {
        if (difficulty == null)
            return 4;
        if (difficulty.equalsIgnoreCase("Beginner"))
            return 1;
        if (difficulty.equalsIgnoreCase("Intermediate"))
            return 2;
        if (difficulty.equalsIgnoreCase("Advanced"))
            return 3;
        return 4; // Unknown
    }

    /**
     * Updates the UI with the latest course data.
     * Calculates the overall progress percentage based on individual course
     * completion.
     *
     * @param courses The list of courses retrieved from the local database.
     */
    private void updateProgressUI(List<Course> courses) {
        // Cache for dynamic updates (e.g. if User Goal changes)
        cachedCourses = courses;

        if (courseAdapter != null) {
            courseAdapter.setCourses(courses);
        }

        // Note: Recommendations are now handled separately in
        // setupRecommendationEngine()

        int totalPercent = 0;
        int count = 0;
        for (Course c : courses) {
            if (c.totalLessons > 0) {
                float fractions = (float) c.progress / c.totalLessons;
                if (fractions > 1.0f)
                    fractions = 1.0f;
                totalPercent += (int) (fractions * 100);
            }
            count++;
        }
        int overall = (count > 0) ? (totalPercent / count) : 0;

        if (tvOverallProgress != null)
            tvOverallProgress.setText(overall + "%");
        if (pbOverallProgress != null)
            pbOverallProgress.setProgress(overall);
    }

    /**
     * M2.1 Sync Engine: Cloud -> Device
     * Listen for changes in Firestore and update Local Room DB.
     * This ensures if progress is made on another device (or connectivity
     * restored),
     * the local DB is updated, and the UI (observing Room) refreshes automatically.
     */
    private void syncFirestoreToLocal(String uid) {
        db.collection("users").document(uid).collection("courses")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        new Thread(() -> {
                            List<Course> cloudCourses = new ArrayList<>();
                            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                                Course c = doc.toObject(Course.class);
                                c.courseId = doc.getId(); // Ensure ID matches doc ID
                                cloudCourses.add(c);
                            }
                            // Update local DB Source of Truth
                            if (!cloudCourses.isEmpty()) {
                                AppDatabase.getInstance(getApplicationContext()).courseDao()
                                        .insert(cloudCourses.get(0)); // insert varargs/list if supported, else loop
                                // Simplified for loop to match DAO insert(Course... courses) or loop
                                for (Course c : cloudCourses) {
                                    AppDatabase.getInstance(getApplicationContext()).courseDao().insert(c);
                                }
                            }
                        }).start();
                    }
                });
    }

    /**
     * Helper for "Write Local, Sync Remote" strategy.
     * Call this when a user completes a lesson.
     */
    public void updateCourseProgress(Course course) {
        // 1. Write Local (Instant UI update via LiveData)
        new Thread(() -> {
            AppDatabase.getInstance(getApplicationContext()).courseDao().insert(course);
        }).start();

        // 2. Write Remote (Queued if Offline, Synced when Online)
        if (!TextUtils.isEmpty(currentUid)) {
            db.collection("users").document(currentUid).collection("courses")
                    .document(course.courseId)
                    .set(course, SetOptions.merge())
                    .addOnFailureListener(e -> Log.e(TAG, "Cloud sync failed (will retry): " + e.getMessage()));
        }
    }

    private void seedDummyCourses() {
        new Thread(() -> {
            // Synced with ContentLibraryActivity sample courses
            List<Course> courses = new ArrayList<>();

            // Mathematics
            courses.add(createCourse("math1", "Basic Algebra",
                    "Learn fundamental algebraic concepts and equations",
                    "Mathematics", "Beginner", "2 hours", 8, 0, "Not Started"));
            courses.add(createCourse("math2", "Calculus 101",
                    "Introduction to derivatives and integrals",
                    "Mathematics", "Intermediate", "4 hours", 12, 3, "In Progress"));
            courses.add(createCourse("math3", "Advanced Statistics",
                    "Statistical analysis and probability theory",
                    "Mathematics", "Advanced", "6 hours", 15, 0, "Not Started"));

            // Science
            courses.add(createCourse("sci1", "Intro to Physics",
                    "Newton's laws, motion, and forces explained",
                    "Science", "Beginner", "3 hours", 10, 5, "In Progress"));
            courses.add(createCourse("sci2", "Organic Chemistry",
                    "Carbon compounds and molecular structures",
                    "Science", "Advanced", "5 hours", 14, 0, "Not Started"));

            // Coding
            courses.add(createCourse("code1", "Java Fundamentals",
                    "Object-oriented programming with Java",
                    "Coding", "Beginner", "4 hours", 12, 0, "Not Started"));
            courses.add(createCourse("code2", "Web Development",
                    "HTML, CSS, and JavaScript basics",
                    "Coding", "Intermediate", "5 hours", 15, 8, "In Progress"));
            courses.add(createCourse("code3", "Python for Data Science",
                    "Data analysis and machine learning",
                    "Coding", "Advanced", "8 hours", 20, 0, "Not Started"));

            // Art
            courses.add(createCourse("art1", "Drawing Basics",
                    "Sketching, shading, and perspective",
                    "Art", "Beginner", "2 hours", 6, 6, "Completed"));

            // Geography
            courses.add(createCourse("geo1", "World Geography",
                    "Countries, capitals, and cultures",
                    "Geography", "Beginner", "2 hours", 8, 4, "In Progress"));

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            db.courseDao().deleteAll();
            for (Course c : courses) {
                db.courseDao().insert(c);
            }
        }).start();
    }

    private Course createCourse(String id, String title, String desc,
            String category, String difficulty,
            String duration, int totalLessons,
            int progress, String status) {
        Course c = new Course();
        c.courseId = id;
        c.title = title;
        c.description = desc;
        c.category = category;
        c.difficulty = difficulty;
        c.duration = duration;
        c.totalLessons = totalLessons;
        c.progress = progress;
        c.status = status;
        c.isPublished = true;
        return c;
    }

    private void seedDummyNotifications() {
        new Thread(() -> {
            com.example.edubridge.data.local.dao.NotificationDao dao = AppDatabase.getInstance(this).notificationDao();

            com.example.edubridge.data.local.entity.Notification n1 = new com.example.edubridge.data.local.entity.Notification();
            n1.id = "dummy_1";
            n1.title = "Welcome to EduBridge!";
            n1.body = "We are excited to have you here. Start a course now!";
            n1.type = "announcement";
            n1.timestamp = System.currentTimeMillis();
            n1.isRead = false;

            com.example.edubridge.data.local.entity.Notification n2 = new com.example.edubridge.data.local.entity.Notification();
            n2.id = "dummy_2";
            n2.title = "New Achievement Unlocked";
            n2.body = "You earned the 'Early Bird' badge.";
            n2.type = "achievement";
            n2.screen = "badges";
            n2.timestamp = System.currentTimeMillis() - 3600000; // 1 hour ago
            n2.isRead = false;

            com.example.edubridge.data.local.entity.Notification n3 = new com.example.edubridge.data.local.entity.Notification();
            n3.id = "dummy_3";
            n3.title = "Math 101 Update";
            n3.body = "New lesson 'Calculus Basics' is available.";
            n3.type = "course_update";
            n3.timestamp = System.currentTimeMillis() - 86400000; // 1 day ago
            n3.isRead = true;

            dao.insert(n1);
            dao.insert(n2);
            dao.insert(n3);
        }).start();
    }

    /**
     * Updates the Achievement Summary section on the Dashboard.
     * M2.4 Feature.
     */
    @SuppressWarnings("unchecked")
    private void updateAchievementSummary(long points, Object badgesObj) {
        // Update XP
        if (tvAchievementXp != null) {
            tvAchievementXp.setText(points + " XP");
        }

        // Get badges list
        List<String> unlockedBadgeIds = new ArrayList<>();
        if (badgesObj instanceof List) {
            unlockedBadgeIds = (List<String>) badgesObj;
        }

        // Update badge count (using BadgeDefinitions for total)
        int totalBadges = BadgeDefinitions.getTotalBadgeCount();
        int earnedBadges = unlockedBadgeIds.size();
        if (tvBadgeCount != null) {
            tvBadgeCount.setText(earnedBadges + "/" + totalBadges + " Badges");
        }

        // Populate recent badges (show icons for unlocked badges)
        if (layoutRecentBadges != null) {
            layoutRecentBadges.removeAllViews();

            if (unlockedBadgeIds.isEmpty()) {
                TextView emptyText = new TextView(this);
                emptyText.setText("No badges earned yet. Start learning!");
                emptyText.setTextColor(getResources().getColor(R.color.text_secondary));
                emptyText.setTextSize(12);
                layoutRecentBadges.addView(emptyText);
            } else {
                // Show badge icons (max 5) with correct icon per badge type
                int limit = Math.min(unlockedBadgeIds.size(), 5);
                for (int i = 0; i < limit; i++) {
                    String badgeId = unlockedBadgeIds.get(i);
                    Badge badge = BadgeDefinitions.getBadgeById(badgeId);

                    ImageView badgeIcon = new ImageView(this);
                    int size = (int) (48 * getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), 0);
                    badgeIcon.setLayoutParams(params);

                    // Use correct icon based on badge type
                    if (badge != null && badge.getIconRes() != 0) {
                        badgeIcon.setImageResource(badge.getIconRes());
                    } else {
                        badgeIcon.setImageResource(R.drawable.ic_achievement_medal);
                    }
                    layoutRecentBadges.addView(badgeIcon);
                }
            }
        }
    }

    // ===== M4.2 Level Progression Methods =====

    /**
     * M4.2.3 - Update level progression UI.
     * Shows current level, title, progress bar, and XP info.
     */
    private void updateLevelProgression(long totalXp) {
        int level = LevelManager.getLevelForXp(totalXp);
        String title = LevelManager.getLevelTitle(level);
        int progressPercent = LevelManager.getProgressPercent(totalXp);
        long xpToNext = LevelManager.getXpToNextLevel(totalXp);
        long currentLevelXp = LevelManager.getXpForLevel(level);
        long nextLevelXp = LevelManager.getXpForNextLevel(level);

        // Update level number badge
        TextView tvLevelNumber = findViewById(R.id.tv_level_number);
        if (tvLevelNumber != null) {
            tvLevelNumber.setText(String.valueOf(level));
        }

        // Update level title
        TextView tvLevelTitle = findViewById(R.id.tv_level_title);
        if (tvLevelTitle != null) {
            tvLevelTitle.setText(title);
        }

        // Update XP progress text
        TextView tvLevelXpProgress = findViewById(R.id.tv_level_xp_progress);
        if (tvLevelXpProgress != null) {
            if (level >= LevelManager.MAX_LEVEL) {
                tvLevelXpProgress.setText("Max level reached! 🏆");
            } else {
                long xpInCurrentLevel = totalXp - currentLevelXp;
                long xpNeeded = nextLevelXp - currentLevelXp;
                tvLevelXpProgress.setText(xpInCurrentLevel + " / " + xpNeeded + " XP to Level " + (level + 1));
            }
        }

        // Update progress bar
        com.google.android.material.progressindicator.LinearProgressIndicator pbLevelProgress = findViewById(
                R.id.pb_level_progress);
        if (pbLevelProgress != null) {
            pbLevelProgress.setProgress(progressPercent);
        }
    }

    /**
     * M4.2.5 - Show level-up notification dialog.
     * Called when user levels up.
     */
    private void showLevelUpNotification(int newLevel) {
        String title = LevelManager.getLevelTitle(newLevel);

        new android.app.AlertDialog.Builder(this)
                .setTitle("🎉 Level Up!")
                .setMessage("Congratulations! You've reached Level " + newLevel + "!\n\n" +
                        "You are now a " + title + "!\n\nKeep learning to unlock more achievements!")
                .setPositiveButton("Awesome!", null)
                .show();

        // Also show a Toast
        Toast.makeText(this, "Level Up! You are now Level " + newLevel + " 🎉", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDocListener != null) {
            userDocListener.remove();
            userDocListener = null;
        }
    }
}
