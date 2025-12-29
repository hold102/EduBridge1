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

                    // 2. Learning Goal (for M2.2)
                    String goal = snapshot.getString("learningGoal");
                    boolean goalChanged = (goal != null && !goal.equals(userLearningGoal))
                            || (goal == null && userLearningGoal != null);

                    userLearningGoal = goal;

                    // Refresh recommendations if goal matches changed
                    if (goalChanged && cachedCourses != null) {
                        updateRecommendationUI(cachedCourses);
                    }

                    // 3. Achievement Summary (M2.4)
                    updateAchievementSummary(points, snapshot.get("badges"));
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
     * Sets up the RecyclerView for course progress and observes the Room database
     * for real-time updates on course data.
     */
    private void setupProgressDashboard() {
        rvCourseProgress = findViewById(R.id.rv_course_progress);
        tvOverallProgress = findViewById(R.id.tv_overall_progress);
        pbOverallProgress = findViewById(R.id.pb_overall_progress);

        if (rvCourseProgress != null) {
            rvCourseProgress.setLayoutManager(new LinearLayoutManager(this));
            courseAdapter = new CourseProgressAdapter();
            rvCourseProgress.setAdapter(courseAdapter);

            // Enable nested scrolling so the box scrolls internally
            rvCourseProgress.setNestedScrollingEnabled(true);
        }

        // Observe Data
        AppDatabase.getInstance(this).courseDao().getAllCourses().observe(this, courses -> {
            boolean needsSeed = (courses == null || courses.isEmpty());
            boolean missingCategory = false;

            // Check if we need to upgrade data (i.e. if category field is missing)
            if (courses != null) {
                for (Course c : courses) {
                    if (c.category == null) {
                        missingCategory = true;
                        break;
                    }
                }
            }

            // Only seed if empty or we found courses with missing categories
            if (needsSeed || missingCategory) {
                seedDummyCourses();
            } else {
                updateProgressUI(courses);
            }
        });
    }

    // Recommendation UI Fields
    private RecyclerView rvRecommendations;
    private RecommendationAdapter recommendationAdapter;

    private void setupRecommendationEngine() {
        rvRecommendations = findViewById(R.id.rv_recommendations);
        if (rvRecommendations != null) {
            rvRecommendations.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recommendationAdapter = new RecommendationAdapter(this::openCourseDetails);
            rvRecommendations.setAdapter(recommendationAdapter);
        }
    }

    private void openCourseDetails(Course course) {
        Intent intent = new Intent(DashboardActivity.this, CourseDetailActivity.class);
        intent.putExtra("EXTRA_TITLE", course.title);
        intent.putExtra("EXTRA_DESC", course.description);
        // Simple icon mapping or default
        int iconResId = R.drawable.ic_nav_library; // Default
        if (course.title.contains("Math"))
            iconResId = R.drawable.ic_subject_math;
        else if (course.title.contains("Science"))
            iconResId = R.drawable.ic_subject_science;
        else if (course.title.contains("Computer"))
            iconResId = R.drawable.ic_subject_coding;
        else if (course.title.contains("Art"))
            iconResId = R.drawable.ic_subject_art;
        else if (course.title.contains("Geography"))
            iconResId = R.drawable.ic_subject_geo;

        intent.putExtra("EXTRA_ICON", iconResId);
        startActivity(intent);
    }

    private void updateRecommendationUI(List<Course> courses) {
        if (courses == null || courses.isEmpty())
            return;

        List<Course> sortedList = new ArrayList<>(courses);

        // Sorting Logic:
        // 1. Goal Match: Matches userLearningGoal (Yes > No)
        // 2. Status: Not Started (1) -> In Progress (2) -> Completed (3)
        // 3. Difficulty: Beginner (1) -> Intermediate (2) -> Advanced (3)
        sortedList.sort((c1, c2) -> {
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

        if (recommendationAdapter != null) {
            recommendationAdapter.setRecommendations(sortedList);
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

        // M2.2 Update Recommendation
        updateRecommendationUI(courses);

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
            // Synced with ContentLibraryActivity
            Course c1 = new Course();
            c1.courseId = "c1";
            c1.title = "Mathematics 101";
            c1.description = "Basic Algebra and Geometry";
            c1.category = "Math";
            c1.progress = 0;
            c1.totalLessons = 10;
            c1.status = "Not Started";
            c1.difficulty = "Beginner";
            c1.duration = "2 Weeks";

            Course c2 = new Course();
            c2.courseId = "c2";
            c2.title = "Science Basics";
            c2.description = "Introduction to Physics and Chemistry";
            c2.category = "Science";
            c2.progress = 3;
            c2.totalLessons = 12;
            c2.status = "In Progress";
            c2.difficulty = "Intermediate";
            c2.duration = "4 Weeks";

            Course c3 = new Course();
            c3.courseId = "c3";
            c3.title = "Computer Science";
            c3.description = "Java Programming for Beginners";
            c3.category = "Coding";
            c3.progress = 0;
            c3.totalLessons = 20;
            c3.status = "Not Started";
            c3.difficulty = "Advanced";
            c3.duration = "8 Weeks";

            Course c4 = new Course();
            c4.courseId = "c4";
            c4.title = "Art & Design";
            c4.description = "Digital Art Fundamentals";
            c4.category = "Arts";
            c4.progress = 10;
            c4.totalLessons = 10;
            c4.status = "Completed";
            c4.difficulty = "Beginner";
            c4.duration = "1 Week";

            Course c5 = new Course();
            c5.courseId = "c5";
            c5.title = "World Geography";
            c5.description = "Explore the Continents";
            c5.category = "Humanities";
            c5.progress = 5;
            c5.totalLessons = 15;
            c5.status = "In Progress";
            c5.difficulty = "Intermediate";
            c5.duration = "3 Weeks";

            AppDatabase db = AppDatabase.getInstance(getApplicationContext());

            // Clear old data to ensure sync with Content Library
            db.courseDao().deleteAll();

            db.courseDao().insert(c1);
            db.courseDao().insert(c2);
            db.courseDao().insert(c3);
            db.courseDao().insert(c4);
            db.courseDao().insert(c5);
        }).start();
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

        // Update badge count
        int totalBadges = 5; // We have 5 defined badges
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
                // Show badge icons (max 5)
                int limit = Math.min(unlockedBadgeIds.size(), 5);
                for (int i = 0; i < limit; i++) {
                    ImageView badgeIcon = new ImageView(this);
                    int size = (int) (48 * getResources().getDisplayMetrics().density);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
                    params.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), 0);
                    badgeIcon.setLayoutParams(params);
                    badgeIcon.setImageResource(R.drawable.ic_achievement_medal);
                    layoutRecentBadges.addView(badgeIcon);
                }
            }
        }
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
