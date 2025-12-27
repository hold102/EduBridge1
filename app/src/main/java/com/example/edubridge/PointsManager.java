package com.example.edubridge;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * ✅ Central place for awarding points (M4.1/M4.2/M4.3...)
 * - Use in WriteBatch to keep atomic with other writes (e.g., create post + points)
 */
public final class PointsManager {

    private PointsManager() {}

    public static final String FIELD_TOTAL_POINTS = "totalPoints";
    public static final String FIELD_POINTS_UPDATED_AT = "pointsUpdatedAt";
    public static final String FIELD_USER_NAME = "userName";



    /**
     * ✅ Add points in a batch (recommended).
     * Example: applyAwardPoints(batch, userRef, 5, "create_post");
     */
    public static void applyAwardPoints(
            @NonNull WriteBatch batch,
            @NonNull DocumentReference userRef,
            long delta,
            @NonNull String reason
    ) {
        Map<String, Object> update = new HashMap<>();
        update.put(FIELD_TOTAL_POINTS, FieldValue.increment(delta));
        update.put(FIELD_POINTS_UPDATED_AT, FieldValue.serverTimestamp());
        update.put("lastPointReason", reason); // optional debug field
        batch.set(userRef, update, SetOptions.merge());
    }
    public static void applyAwardPoints(
            @NonNull Transaction tx,
            @NonNull DocumentReference userRef,
            long delta,
            @NonNull String reason
    ) {
        Map<String, Object> update = new HashMap<>();
        update.put(FIELD_TOTAL_POINTS, FieldValue.increment(delta));
        update.put(FIELD_POINTS_UPDATED_AT, FieldValue.serverTimestamp());
        update.put("lastPointReason", reason);
        tx.set(userRef, update, SetOptions.merge());
    }
    /**
     * ✅ Store username to users/{uid} (for leaderboard display).
     */
    public static void applyUserName(
            @NonNull WriteBatch batch,
            @NonNull DocumentReference userRef,
            @NonNull String userName
    ) {
        Map<String, Object> update = new HashMap<>();
        update.put(FIELD_USER_NAME, userName);
        batch.set(userRef, update, SetOptions.merge());
    }
}

