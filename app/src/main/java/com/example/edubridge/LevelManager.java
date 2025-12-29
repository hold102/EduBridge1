package com.example.edubridge;

/**
 * M4.2 Level Progression Manager
 * 
 * Handles XP threshold management (M4.2.1) and level calculation (M4.2.2).
 * 
 * XP Thresholds (exponential growth):
 * - Level 1: 0 XP
 * - Level 2: 100 XP
 * - Level 3: 300 XP
 * - Level 4: 600 XP
 * - Level 5: 1000 XP
 * - Level 6: 1500 XP
 * - Level 7: 2100 XP
 * - Level 8: 2800 XP
 * - Level 9: 3600 XP
 * - Level 10: 4500 XP (max level)
 */
public class LevelManager {

    // XP thresholds for each level (index = level - 1)
    private static final long[] XP_THRESHOLDS = {
            0, // Level 1
            100, // Level 2
            300, // Level 3
            600, // Level 4
            1000, // Level 5
            1500, // Level 6
            2100, // Level 7
            2800, // Level 8
            3600, // Level 9
            4500 // Level 10 (max)
    };

    public static final int MAX_LEVEL = XP_THRESHOLDS.length;

    /**
     * M4.2.2 - Calculate user level from total XP.
     * Idempotent: same XP always returns same level.
     * 
     * @param totalXp User's total accumulated XP
     * @return Current level (1 to MAX_LEVEL)
     */
    public static int getLevelForXp(long totalXp) {
        if (totalXp < 0)
            totalXp = 0;

        for (int i = XP_THRESHOLDS.length - 1; i >= 0; i--) {
            if (totalXp >= XP_THRESHOLDS[i]) {
                return i + 1; // Level is 1-indexed
            }
        }
        return 1; // Default to level 1
    }

    /**
     * Get XP required to reach a specific level.
     * 
     * @param level Target level (1 to MAX_LEVEL)
     * @return XP threshold for that level
     */
    public static long getXpForLevel(int level) {
        if (level < 1)
            return 0;
        if (level > MAX_LEVEL)
            return XP_THRESHOLDS[MAX_LEVEL - 1];
        return XP_THRESHOLDS[level - 1];
    }

    /**
     * Get XP required for the next level.
     * 
     * @param currentLevel Current level
     * @return XP threshold for next level, or current max if at max level
     */
    public static long getXpForNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) {
            return XP_THRESHOLDS[MAX_LEVEL - 1];
        }
        return XP_THRESHOLDS[currentLevel]; // Next level threshold
    }

    /**
     * M4.2.3 - Calculate progress percentage to next level.
     * Used for progress bar visualization.
     * 
     * @param totalXp User's total accumulated XP
     * @return Progress percentage (0-100)
     */
    public static int getProgressPercent(long totalXp) {
        int currentLevel = getLevelForXp(totalXp);

        if (currentLevel >= MAX_LEVEL) {
            return 100; // Max level reached
        }

        long currentLevelXp = getXpForLevel(currentLevel);
        long nextLevelXp = getXpForNextLevel(currentLevel);
        long xpInCurrentLevel = totalXp - currentLevelXp;
        long xpNeeded = nextLevelXp - currentLevelXp;

        if (xpNeeded <= 0)
            return 100;

        return (int) ((xpInCurrentLevel * 100) / xpNeeded);
    }

    /**
     * Get XP remaining until next level.
     * 
     * @param totalXp User's total accumulated XP
     * @return XP remaining, or 0 if at max level
     */
    public static long getXpToNextLevel(long totalXp) {
        int currentLevel = getLevelForXp(totalXp);

        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }

        long nextLevelXp = getXpForNextLevel(currentLevel);
        return nextLevelXp - totalXp;
    }

    /**
     * Get level title/name for display.
     * 
     * @param level User level (1 to MAX_LEVEL)
     * @return Display title for the level
     */
    public static String getLevelTitle(int level) {
        switch (level) {
            case 1:
                return "Beginner";
            case 2:
                return "Learner";
            case 3:
                return "Student";
            case 4:
                return "Scholar";
            case 5:
                return "Expert";
            case 6:
                return "Master";
            case 7:
                return "Guru";
            case 8:
                return "Sage";
            case 9:
                return "Legend";
            case 10:
                return "Champion";
            default:
                return "Beginner";
        }
    }

    /**
     * M4.2.4 - Check if XP update triggers a level-up.
     * 
     * @param oldXp Previous total XP
     * @param newXp New total XP
     * @return New level if leveled up, -1 if no level change
     */
    public static int checkLevelUp(long oldXp, long newXp) {
        int oldLevel = getLevelForXp(oldXp);
        int newLevel = getLevelForXp(newXp);

        if (newLevel > oldLevel) {
            return newLevel;
        }
        return -1; // No level up
    }
}
