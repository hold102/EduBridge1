package com.example.edubridge;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * M3.3 Enrollment Management Helper
 * 
 * Features:
 * - M3.3.1: Enroll in courses
 * - M3.3.2: Prevent duplicate enrollment
 * - M3.3.3: Unenroll from courses
 * - M3.3.4: Record timestamps
 * - M3.3.5: Sync to dashboard
 */
public class EnrollmentManager {

    private static final String TAG = "EnrollmentManager";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Enrollment status enum
     */
    public enum EnrollmentStatus {
        NOT_ENROLLED,
        ENROLLED,
        IN_PROGRESS,
        COMPLETED
    }

    /**
     * Callback for enrollment operations
     */
    public interface EnrollmentCallback {
        void onSuccess(String message);

        void onError(String error);
    }

    /**
     * Callback for checking enrollment status
     */
    public interface StatusCallback {
        void onResult(EnrollmentStatus status, int progress, long enrolledAt);
    }

    // ===== M3.3.1 & M3.3.2: Enroll with duplicate check =====

    /**
     * Enroll user in a course.
     * Prevents duplicate enrollment (M3.3.2).
     * Records enrollment timestamp (M3.3.4).
     * Syncs to dashboard (M3.3.5).
     */
    public static void enrollInCourse(String userId, String courseId,
            String courseTitle, String category,
            int totalLessons, EnrollmentCallback callback) {
        if (userId == null || courseId == null) {
            callback.onError("Invalid user or course");
            return;
        }

        DocumentReference enrollmentRef = db.collection("users").document(userId)
                .collection("enrollments").document(courseId);

        // M3.3.2: Check for duplicate enrollment first
        enrollmentRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                callback.onError("Already enrolled in this course");
                return;
            }

            // Create enrollment record with timestamp (M3.3.4)
            long now = System.currentTimeMillis();
            Map<String, Object> enrollment = new HashMap<>();
            enrollment.put("courseId", courseId);
            enrollment.put("courseTitle", courseTitle);
            enrollment.put("category", category);
            enrollment.put("totalLessons", totalLessons);
            enrollment.put("progress", 0);
            enrollment.put("status", "enrolled");
            enrollment.put("enrolledAt", now); // M3.3.4
            enrollment.put("lastAccessedAt", now); // M3.3.4
            enrollment.put("completedAt", null);

            // Use batch write for atomicity
            WriteBatch batch = db.batch();

            // Add to user's enrollments
            batch.set(enrollmentRef, enrollment);

            // M3.3.5: Update user's enrolled courses count
            // Use set with merge to ensure it works even if field doesn't exist
            DocumentReference userRef = db.collection("users").document(userId);
            Map<String, Object> userUpdate = new HashMap<>();
            userUpdate.put("enrolledCoursesCount", FieldValue.increment(1));
            batch.set(userRef, userUpdate, com.google.firebase.firestore.SetOptions.merge());

            Log.d(TAG, "Attempting to enroll in course: " + courseId);

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Successfully enrolled in course: " + courseId);
                        callback.onSuccess("Enrolled successfully!");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Enrollment failed for course: " + courseId, e);
                        callback.onError("Enrollment failed: " + e.getMessage());
                    });
        });
    }

    // ===== M3.3.3: Unenroll =====

    /**
     * Unenroll user from a course before completion.
     * Only allowed if course is not completed.
     */
    public static void unenrollFromCourse(String userId, String courseId,
            EnrollmentCallback callback) {
        if (userId == null || courseId == null) {
            callback.onError("Invalid user or course");
            return;
        }

        DocumentReference enrollmentRef = db.collection("users").document(userId)
                .collection("enrollments").document(courseId);

        // Check if enrolled and not completed
        enrollmentRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                callback.onError("Not enrolled in this course");
                return;
            }

            DocumentSnapshot doc = task.getResult();
            String status = doc.getString("status");

            // M3.3.3: Prevent unenroll if completed
            if ("completed".equals(status)) {
                callback.onError("Cannot unenroll from a completed course");
                return;
            }

            // Remove enrollment
            WriteBatch batch = db.batch();
            batch.delete(enrollmentRef);

            // Decrement enrolled courses count
            DocumentReference userRef = db.collection("users").document(userId);
            batch.update(userRef, "enrolledCoursesCount", FieldValue.increment(-1));

            batch.commit()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Unenrolled from course: " + courseId);
                        callback.onSuccess("Unenrolled successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Unenroll failed", e);
                        callback.onError("Unenroll failed: " + e.getMessage());
                    });
        });
    }

    // ===== Check enrollment status =====

    /**
     * Check if user is enrolled in a course.
     * Returns enrollment status and progress.
     */
    public static void checkEnrollmentStatus(String userId, String courseId,
            StatusCallback callback) {
        if (userId == null || courseId == null) {
            callback.onResult(EnrollmentStatus.NOT_ENROLLED, 0, 0);
            return;
        }

        db.collection("users").document(userId)
                .collection("enrollments").document(courseId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onResult(EnrollmentStatus.NOT_ENROLLED, 0, 0);
                        return;
                    }

                    Long progress = doc.getLong("progress");
                    Long enrolledAt = doc.getLong("enrolledAt");
                    String status = doc.getString("status");

                    int progressValue = progress != null ? progress.intValue() : 0;
                    long enrolledAtValue = enrolledAt != null ? enrolledAt : 0;

                    EnrollmentStatus enrollmentStatus;
                    if ("completed".equals(status)) {
                        enrollmentStatus = EnrollmentStatus.COMPLETED;
                    } else if (progressValue > 0) {
                        enrollmentStatus = EnrollmentStatus.IN_PROGRESS;
                    } else {
                        enrollmentStatus = EnrollmentStatus.ENROLLED;
                    }

                    callback.onResult(enrollmentStatus, progressValue, enrolledAtValue);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking enrollment", e);
                    callback.onResult(EnrollmentStatus.NOT_ENROLLED, 0, 0);
                });
    }

    // ===== Update progress =====

    /**
     * Update user's progress in a course.
     * Also updates lastAccessedAt timestamp.
     */
    public static void updateProgress(String userId, String courseId,
            int lessonsCompleted, int totalLessons,
            EnrollmentCallback callback) {
        if (userId == null || courseId == null) {
            if (callback != null)
                callback.onError("Invalid user or course");
            return;
        }

        DocumentReference enrollmentRef = db.collection("users").document(userId)
                .collection("enrollments").document(courseId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("progress", lessonsCompleted);
        updates.put("lastAccessedAt", System.currentTimeMillis());

        // Check if completed
        if (lessonsCompleted >= totalLessons) {
            updates.put("status", "completed");
            updates.put("completedAt", System.currentTimeMillis());
        } else if (lessonsCompleted > 0) {
            updates.put("status", "in_progress");
        }

        enrollmentRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) {
                        callback.onSuccess("Progress updated");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating progress", e);
                    if (callback != null) {
                        callback.onError("Failed to update progress");
                    }
                });
    }

    /**
     * Get current user ID helper.
     */
    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
}
