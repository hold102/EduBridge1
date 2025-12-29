package com.example.edubridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Daily Check-In Activity
 * 
 * Features:
 * - Shows current week with real dates
 * - Displays tick marks on days user has checked in
 * - Streak counting (resets to 1 if gap > 24 hours)
 * - Stores check-in dates to Firestore
 */
public class DailyCheckInActivity extends AppCompatActivity {

    private static final long CHECK_IN_REWARD = 50;

    // Malaysia timezone (UTC+8)
    private static final TimeZone MALAYSIA_TZ = TimeZone.getTimeZone("Asia/Kuala_Lumpur");

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI Elements
    private TextView tvMonthYear;
    private TextView tvStreakCount;
    private TextView tvStreakMessage;
    private MaterialButton btnClaim;

    // Day views (0 = Monday, 6 = Sunday)
    private TextView[] tvDays = new TextView[7];
    private ImageView[] ivChecks = new ImageView[7];
    private ImageView[] ivCircles = new ImageView[7];

    // Week dates
    private Date[] weekDates = new Date[7];
    private List<String> checkedInDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_check_in);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views
        tvMonthYear = findViewById(R.id.tv_month_year);
        tvStreakCount = findViewById(R.id.tv_streak_count);
        tvStreakMessage = findViewById(R.id.tv_streak_message);
        btnClaim = findViewById(R.id.btn_claim);

        // Find day views
        int[] dayTextIds = { R.id.tv_day_0, R.id.tv_day_1, R.id.tv_day_2, R.id.tv_day_3, R.id.tv_day_4, R.id.tv_day_5,
                R.id.tv_day_6 };
        int[] checkIds = { R.id.iv_check_0, R.id.iv_check_1, R.id.iv_check_2, R.id.iv_check_3, R.id.iv_check_4,
                R.id.iv_check_5, R.id.iv_check_6 };
        int[] circleIds = { R.id.iv_circle_0, R.id.iv_circle_1, R.id.iv_circle_2, R.id.iv_circle_3, R.id.iv_circle_4,
                R.id.iv_circle_5, R.id.iv_circle_6 };

        for (int i = 0; i < 7; i++) {
            tvDays[i] = findViewById(dayTextIds[i]);
            ivChecks[i] = findViewById(checkIds[i]);
            ivCircles[i] = findViewById(circleIds[i]);
        }

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        btnClaim.setOnClickListener(v -> doDailyCheckIn());

        // Setup week dates
        setupWeekDates();

        // Load user data
        loadUserData();
    }

    /**
     * Setup the current week dates (Monday to Sunday).
     * Uses Malaysia timezone for consistent date display.
     */
    private void setupWeekDates() {
        Calendar cal = Calendar.getInstance(MALAYSIA_TZ);

        // Set month/year header
        SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearFormat.setTimeZone(MALAYSIA_TZ);
        tvMonthYear.setText(monthYearFormat.format(cal.getTime()));

        // Find Monday of current week
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromMonday = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - Calendar.MONDAY);
        cal.add(Calendar.DAY_OF_YEAR, -daysFromMonday);

        // Fill week dates
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        dayFormat.setTimeZone(MALAYSIA_TZ);
        Date today = startOfDay(new Date());

        for (int i = 0; i < 7; i++) {
            weekDates[i] = cal.getTime();
            tvDays[i].setText(dayFormat.format(weekDates[i]));

            // Highlight today
            if (startOfDay(weekDates[i]).equals(today)) {
                ivCircles[i].setAlpha(1.0f);
                tvDays[i].setTextColor(getResources().getColor(R.color.brand_black, null));
            } else if (weekDates[i].after(today)) {
                // Future days - dimmed
                ivCircles[i].setAlpha(0.3f);
                tvDays[i].setTextColor(getResources().getColor(R.color.text_hint, null));
            } else {
                // Past days
                ivCircles[i].setAlpha(0.6f);
            }

            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /**
     * Load user check-in data from Firestore.
     * Uses string-based date comparison (yyyy-MM-dd) for reliability.
     */
    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null)
            return;

        db.collection("users").document(user.getUid())
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null || snapshot == null)
                        return;

                    // Get streak count
                    Long streak = snapshot.getLong("streakCount");
                    if (streak == null)
                        streak = 0L;

                    updateStreakUI(streak);

                    // ALWAYS clear first, then load fresh data
                    checkedInDates.clear();

                    // Get check-in dates array from Firestore
                    List<String> dates = (List<String>) snapshot.get("checkInDates");
                    if (dates != null) {
                        checkedInDates.addAll(dates);
                    }

                    // Update week UI with check marks
                    updateWeekUI();

                    // === SIMPLE STRING-BASED CHECK ===
                    // Get today's date as string key (Malaysia timezone)
                    SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    dateKeyFormat.setTimeZone(MALAYSIA_TZ);
                    String todayKey = dateKeyFormat.format(new Date());

                    // Check if today's key exists in the checkInDates array
                    boolean alreadyCheckedInToday = checkedInDates.contains(todayKey);

                    // Debug log
                    android.util.Log.d("DailyCheckIn",
                            "Today key: " + todayKey +
                                    " | CheckedIn dates: " + checkedInDates +
                                    " | Already checked in: " + alreadyCheckedInToday);

                    if (alreadyCheckedInToday) {
                        // Already checked in today - disable button
                        btnClaim.setText("Already Checked In ✓");
                        btnClaim.setEnabled(false);
                    } else {
                        // Can check in today - enable button (resets at midnight automatically)
                        btnClaim.setText("Check In");
                        btnClaim.setEnabled(true);
                    }
                });
    }

    /**
     * Update streak display.
     */
    private void updateStreakUI(long streak) {
        tvStreakCount.setText(streak + " Day Streak");

        if (streak == 0) {
            tvStreakMessage.setText("Check in now to start your streak!");
        } else if (streak == 1) {
            tvStreakMessage.setText("Great start! Keep it up tomorrow.");
        } else if (streak < 7) {
            tvStreakMessage.setText("You're on fire! Keep it up.");
        } else {
            tvStreakMessage.setText("Amazing! You're a streak champion! 🏆");
        }
    }

    /**
     * Update week UI with check marks.
     */
    private void updateWeekUI() {
        // Use Malaysia timezone for date key matching
        SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        dateKeyFormat.setTimeZone(MALAYSIA_TZ);

        for (int i = 0; i < 7; i++) {
            String dateKey = dateKeyFormat.format(weekDates[i]);
            boolean isCheckedIn = checkedInDates.contains(dateKey);

            if (isCheckedIn) {
                // Show check mark, hide circle and date
                ivChecks[i].setVisibility(View.VISIBLE);
                ivCircles[i].setVisibility(View.GONE);
                tvDays[i].setVisibility(View.GONE);
            } else {
                // Show date circle, hide check mark
                ivChecks[i].setVisibility(View.GONE);
                ivCircles[i].setVisibility(View.VISIBLE);
                tvDays[i].setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Perform daily check-in.
     */
    private void doDailyCheckIn() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String uid = user.getUid();
        if (TextUtils.isEmpty(uid)) {
            Toast.makeText(this, "Invalid user.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(uid);
        btnClaim.setEnabled(false);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snap = transaction.get(userRef);

            Timestamp lastCheckInTs = snap.getTimestamp("lastDailyCheckIn");
            Long streakCount = snap.getLong("streakCount");
            Timestamp lastStreakDateTs = snap.getTimestamp("lastStreakDate");

            if (streakCount == null)
                streakCount = 0L;

            Date now = new Date();
            Date todayStart = startOfDay(now);

            // Already checked in today?
            if (lastCheckInTs != null) {
                Date last = lastCheckInTs.toDate();
                if (!last.before(todayStart)) {
                    throw new RuntimeException("ALREADY_CHECKED_IN_TODAY");
                }
            }

            // Today's date key for storage (Malaysia timezone)
            SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            dateKeyFormat.setTimeZone(MALAYSIA_TZ);
            String todayKey = dateKeyFormat.format(now);

            // Compute new streak
            long newStreak = 1;

            if (lastStreakDateTs != null) {
                Date lastStreakDate = lastStreakDateTs.toDate();
                Date yesterdayStart = startOfDay(addDays(now, -1));
                Date yesterdayEnd = endOfDay(addDays(now, -1));

                boolean wasYesterday = !lastStreakDate.before(yesterdayStart) && !lastStreakDate.after(yesterdayEnd);

                if (wasYesterday) {
                    newStreak = streakCount + 1;
                }
                // If gap > 1 day, streak resets to 1
            }

            // Update user document
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastDailyCheckIn", Timestamp.now());
            updates.put("streakCount", newStreak);
            updates.put("lastStreakDate", Timestamp.now());
            transaction.set(userRef, updates, SetOptions.merge());

            // Add today's date to checkInDates array
            transaction.update(userRef, "checkInDates", FieldValue.arrayUnion(todayKey));

            // Add points
            PointsManager.applyAwardPoints(transaction, userRef, CHECK_IN_REWARD, "daily_check_in");

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Check-in success! +" + CHECK_IN_REWARD + " points ✅", Toast.LENGTH_LONG).show();
            btnClaim.setText("Already Checked In ✓");

            // Check for streak badges (M4.3.1)
            // Use uid from outer scope (already defined above)
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            Long streak = doc.getLong("streakCount");
                            if (streak != null) {
                                BadgeManager.checkAndAwardStreakBadge(uid, streak);
                            }
                        }
                    });
        }).addOnFailureListener(e -> {
            btnClaim.setEnabled(true);

            String msg = (e.getMessage() == null) ? "" : e.getMessage();
            if (msg.contains("ALREADY_CHECKED_IN_TODAY")) {
                Toast.makeText(this, "You already checked in today ✅", Toast.LENGTH_LONG).show();
                btnClaim.setText("Already Checked In ✓");
                btnClaim.setEnabled(false);
            } else {
                Toast.makeText(this, "Check-in failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== Date helpers (Malaysia timezone) =====

    private Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance(MALAYSIA_TZ);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance(MALAYSIA_TZ);
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance(MALAYSIA_TZ);
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }
}