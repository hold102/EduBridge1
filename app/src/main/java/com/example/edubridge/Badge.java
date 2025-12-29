package com.example.edubridge;

public class Badge {
    private String id;
    private String title;
    private String desc;
    private int requiredPoints;
    private boolean unlocked;

    public Badge() {}

    public Badge(String id, String title, String desc, int requiredPoints) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.requiredPoints = requiredPoints;
        this.unlocked = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDesc() { return desc; }
    public int getRequiredPoints() { return requiredPoints; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
}
