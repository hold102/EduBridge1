package com.example.edubridge;

import android.util.Log;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Badge Manager for M4.3 Digital Badges.
 * 
 * Handles:
 * - M4.3.1: Award badges for predefined milestones
 * - M4.3.5: Prevent duplicate badge issuance (via FieldValue.arrayUnion)
 */
public class BadgeManager {

    private static final String TAG = "BadgeManager";

    /**
     * Check and award point-based badges (M4.3.1).
     * Called after points are awarded.
     * 
     * @param uid         User ID
     * @param totalPoints User's new total points
     */
    public static void checkAndAwardPointBadges(String uid, long totalPoints) {
        if (uid == null || uid.isEmpty())
            return;

        List<Badge> allBadges = BadgeDefinitions.getAllBadges();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Badge badge : allBadges) {
            if (badge.getConditionType() == Badge.ConditionType.POINTS) {
                if (totalPoints >= badge.getConditionValue()) {
                    // Award badge (arrayUnion prevents duplicates - M4.3.5)
                    awardBadge(db, uid, badge.getId());
                }
            }
        }
    }

    /**
     * Check and award streak-based badges (M4.3.1).
     * Called after daily check-in.
     * 
     * @param uid         User ID
     * @param streakCount Current streak count
     */
    public static void checkAndAwardStreakBadge(String uid, long streakCount) {
        if (uid == null || uid.isEmpty())
            return;

        List<Badge> allBadges = BadgeDefinitions.getAllBadges();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (Badge badge : allBadges) {
            if (badge.getConditionType() == Badge.ConditionType.STREAK) {
                if (streakCount >= badge.getConditionValue()) {
                    awardBadge(db, uid, badge.getId());
                }
            }
        }
    }

    /**
     * Award course completion badge (M4.3.1).
     * Called when user completes a course.
     * 
     * @param uid User ID
     */
    public static void awardCourseCompleteBadge(String uid) {
        if (uid == null || uid.isEmpty())
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        awardBadge(db, uid, "first_course");
    }

    /**
     * Award quiz mastery badge (M4.3.1).
     * Called when user scores 100% on a quiz.
     * 
     * @param uid User ID
     */
    public static void awardQuizMasteryBadge(String uid) {
        if (uid == null || uid.isEmpty())
            return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        awardBadge(db, uid, "quiz_ace");
    }

    /**
     * Award a specific badge to user (M4.3.5).
     * Uses FieldValue.arrayUnion to prevent duplicate issuance.
     * 
     * @param db      Firestore instance
     * @param uid     User ID
     * @param badgeId Badge ID to award
     */
    public static void awardBadge(FirebaseFirestore db, String uid, String badgeId) {
        if (db == null || uid == null || badgeId == null)
            return;

        // FieldValue.arrayUnion automatically prevents duplicates!
        db.collection("users")
                .document(uid)
                .update("badges", FieldValue.arrayUnion(badgeId))
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Badge awarded: " + badgeId + " to user: " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to award badge: " + badgeId, e);
                });
    }

    /**
     * Check if a badge is unlocked based on user's badges list.
     * 
     * @param badgeId    Badge ID to check
     * @param userBadges List of badge IDs the user has earned
     * @return true if badge is unlocked
     */
    public static boolean isBadgeUnlocked(String badgeId, List<String> userBadges) {
        if (userBadges == null || badgeId == null)
            return false;
        return userBadges.contains(badgeId);
    }
}
