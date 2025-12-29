package com.example.edubridge;

import java.util.ArrayList;
import java.util.List;

/**
 * Central registry of all badge definitions (M4.3.3).
 * 
 * Categories:
 * - ACHIEVEMENT: Milestone achievements (points, first actions)
 * - PARTICIPATION: Community engagement
 * - CONSISTENCY: Daily streaks
 * - MASTERY: Learning milestones (courses, quizzes)
 */
public class BadgeDefinitions {

    private static List<Badge> allBadges = null;

    /**
     * Get all badge definitions.
     * Badges are created once and cached.
     */
    public static List<Badge> getAllBadges() {
        if (allBadges == null) {
            allBadges = createAllBadges();
        }
        return new ArrayList<>(allBadges);
    }

    /**
     * Get badges filtered by category.
     */
    public static List<Badge> getBadgesByCategory(Badge.Category category) {
        List<Badge> filtered = new ArrayList<>();
        for (Badge badge : getAllBadges()) {
            if (badge.getCategory() == category) {
                filtered.add(badge);
            }
        }
        return filtered;
    }

    /**
     * Get a badge by its ID.
     */
    public static Badge getBadgeById(String id) {
        for (Badge badge : getAllBadges()) {
            if (badge.getId().equals(id)) {
                return badge;
            }
        }
        return null;
    }

    /**
     * Get total number of badges.
     */
    public static int getTotalBadgeCount() {
        return getAllBadges().size();
    }

    /**
     * Create all badge definitions.
     */
    private static List<Badge> createAllBadges() {
        List<Badge> badges = new ArrayList<>();

        // ========== ACHIEVEMENT BADGES (Point milestones) ==========
        badges.add(new Badge(
                "first_post",
                "First Post",
                "You've taken your first step in the community!",
                Badge.Category.ACHIEVEMENT,
                Badge.ConditionType.POINTS,
                5,
                "Earn 5 points",
                R.drawable.ic_achievement_medal));

        badges.add(new Badge(
                "rising_star",
                "Rising Star",
                "You're becoming a standout learner!",
                Badge.Category.ACHIEVEMENT,
                Badge.ConditionType.POINTS,
                50,
                "Earn 50 points",
                R.drawable.ic_achievement_trophy));

        badges.add(new Badge(
                "legend",
                "Legend",
                "You've achieved legendary status!",
                Badge.Category.ACHIEVEMENT,
                Badge.ConditionType.POINTS,
                200,
                "Earn 200 points",
                R.drawable.ic_achievement_trophy));

        // ========== PARTICIPATION BADGES (Community engagement) ==========
        badges.add(new Badge(
                "contributor",
                "Contributor",
                "Thanks for contributing to the community!",
                Badge.Category.PARTICIPATION,
                Badge.ConditionType.POINTS,
                20,
                "Earn 20 points",
                R.drawable.ic_achievement_medal));

        badges.add(new Badge(
                "community_hero",
                "Community Hero",
                "You're a pillar of the community!",
                Badge.Category.PARTICIPATION,
                Badge.ConditionType.POINTS,
                100,
                "Earn 100 points",
                R.drawable.ic_achievement_trophy));

        // ========== CONSISTENCY BADGES (Daily streaks) ==========
        badges.add(new Badge(
                "streak_7",
                "Week Warrior",
                "You've checked in for 7 days straight!",
                Badge.Category.CONSISTENCY,
                Badge.ConditionType.STREAK,
                7,
                "Maintain a 7-day check-in streak",
                R.drawable.ic_streak_fire));

        badges.add(new Badge(
                "streak_30",
                "Monthly Master",
                "An entire month of dedication!",
                Badge.Category.CONSISTENCY,
                Badge.ConditionType.STREAK,
                30,
                "Maintain a 30-day check-in streak",
                R.drawable.ic_streak_fire));

        // ========== MASTERY BADGES (Learning milestones) ==========
        badges.add(new Badge(
                "first_course",
                "Course Completer",
                "You've completed your first course!",
                Badge.Category.MASTERY,
                Badge.ConditionType.COURSE_COMPLETE,
                1,
                "Complete any course",
                R.drawable.ic_study_plan));

        badges.add(new Badge(
                "quiz_ace",
                "Quiz Ace",
                "Perfect score! You've mastered the material!",
                Badge.Category.MASTERY,
                Badge.ConditionType.QUIZ_MASTERY,
                100,
                "Score 100% on any quiz",
                R.drawable.ic_achievement_trophy));

        return badges;
    }
}
