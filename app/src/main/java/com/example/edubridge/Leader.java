package com.example.edubridge;

public class Leader {
    public String uid;
    public String name;
    public long totalPoints;
    public int badgeCount;

    public Leader() {}

    public Leader(String uid, String name, long totalPoints, int badgeCount) {
        this.uid = uid;
        this.name = name;
        this.totalPoints = totalPoints;
        this.badgeCount = badgeCount;
    }
}
