package com.example.edubridge;

/**
 * Badge model class for M4.3 Digital Badges.
 * 
 * Supports:
 * - Categories (M4.3.3): ACHIEVEMENT, PARTICIPATION, CONSISTENCY, MASTERY
 * - Unlock conditions display (M4.3.4)
 * - Locked/unlocked state
 */
public class Badge {

    // Badge categories (M4.3.3)
    public enum Category {
        ACHIEVEMENT, // Milestone achievements (points, first actions)
        PARTICIPATION, // Community engagement
        CONSISTENCY, // Daily streaks
        MASTERY // Learning milestones (courses, quizzes)
    }

    // Condition types for awarding logic
    public enum ConditionType {
        POINTS, // Requires X total points
        STREAK, // Requires X day streak
        COURSE_COMPLETE, // Requires completing a course
        QUIZ_MASTERY // Requires 100% on a quiz
    }

    private String id;
    private String title;
    private String description;
    private Category category;
    private ConditionType conditionType;
    private int conditionValue; // e.g., 50 for "50 points", 7 for "7 day streak"
    private String unlockCondition; // Human-readable: "Earn 50 points"
    private int iconRes; // Drawable resource ID
    private boolean unlocked;

    // Empty constructor for Firebase
    public Badge() {
    }

    // Full constructor
    public Badge(String id, String title, String description, Category category,
            ConditionType conditionType, int conditionValue, String unlockCondition, int iconRes) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.unlockCondition = unlockCondition;
        this.iconRes = iconRes;
        this.unlocked = false;
    }

    // Legacy constructor for backward compatibility
    public Badge(String id, String title, String desc, int requiredPoints) {
        this.id = id;
        this.title = title;
        this.description = desc;
        this.category = Category.ACHIEVEMENT;
        this.conditionType = ConditionType.POINTS;
        this.conditionValue = requiredPoints;
        this.unlockCondition = "Earn " + requiredPoints + " points";
        this.iconRes = R.drawable.ic_achievement_medal;
        this.unlocked = false;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDesc() {
        return description;
    } // Legacy alias

    public Category getCategory() {
        return category;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public int getConditionValue() {
        return conditionValue;
    }

    public int getRequiredPoints() {
        return conditionType == ConditionType.POINTS ? conditionValue : 0;
    }

    public String getUnlockCondition() {
        return unlockCondition;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    // Setters
    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    // Helper to get category display name
    public String getCategoryDisplayName() {
        if (category == null)
            return "Achievement";
        switch (category) {
            case ACHIEVEMENT:
                return "Achievement";
            case PARTICIPATION:
                return "Participation";
            case CONSISTENCY:
                return "Consistency";
            case MASTERY:
                return "Mastery";
            default:
                return "Achievement";
        }
    }
}
