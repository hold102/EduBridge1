package com.example.edubridge.utils;

public class Course {

    private int id;
    private String title;
    private String description;
    private int imageResId;

    public Course(int id, String title, String description, int imageResId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageResId = imageResId;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageResId() { return imageResId; }
}