package com.example.edubridge;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DailyCheckInActivity extends AppCompatActivity {

    private static final long CHECK_IN_REWARD = 50;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_check_in);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
        findViewById(R.id.btn_claim).setOnClickListener(v -> doDailyCheckIn());
    }

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

        // ✅ prevent double click
        findViewById(R.id.btn_claim).setEnabled(false);

        db.runTransaction((Transaction.Function<Void>) transaction -> {

            DocumentSnapshot snap = transaction.get(userRef);

            Timestamp lastCheckInTs = snap.getTimestamp("lastDailyCheckIn");
            Long streakCount = snap.getLong("streakCount");
            Timestamp lastStreakDateTs = snap.getTimestamp("lastStreakDate");

            if (streakCount == null) streakCount = 0L;

            Date now = new Date();
            Date todayStart = startOfDay(now);

            // ✅ already checked in today
            if (lastCheckInTs != null) {
                Date last = lastCheckInTs.toDate();
                if (!last.before(todayStart)) {
                    // last >= todayStart means already checked in today
                    throw new RuntimeException("ALREADY_CHECKED_IN_TODAY");
                }
            }

            // ✅ compute new streak: if last streak was yesterday => +1 else reset to 1
            long newStreak = 1;

            if (lastStreakDateTs != null) {
                Date lastStreakDate = lastStreakDateTs.toDate();
                Date yesterdayStart = startOfDay(addDays(now, -1));
                Date yesterdayEnd = endOfDay(addDays(now, -1));

                boolean wasYesterday =
                        !lastStreakDate.before(yesterdayStart) && !lastStreakDate.after(yesterdayEnd);

                if (wasYesterday) {
                    newStreak = streakCount + 1;
                }
            }

            // ✅ Update streak + last checkin fields (transaction merge)
            Map<String, Object> updates = new HashMap<>();
            updates.put("lastDailyCheckIn", Timestamp.now());
            updates.put("streakCount", newStreak);
            updates.put("lastStreakDate", Timestamp.now());
            transaction.set(userRef, updates, com.google.firebase.firestore.SetOptions.merge());

            // ✅ Add points via centralized manager (transaction version)
            PointsManager.applyAwardPoints(transaction, userRef, CHECK_IN_REWARD, "daily_check_in");

            return null;
        }).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Check-in success! +" + CHECK_IN_REWARD + " points ✅", Toast.LENGTH_LONG).show();
            finish();
        }).addOnFailureListener(e -> {
            findViewById(R.id.btn_claim).setEnabled(true);

            String msg = (e.getMessage() == null) ? "" : e.getMessage();
            if (msg.contains("ALREADY_CHECKED_IN_TODAY")) {
                Toast.makeText(this, "You already checked in today ✅", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Check-in failed: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    // ===== Date helpers =====

    private Date startOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date endOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    private Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, days);
        return cal.getTime();
    }
}