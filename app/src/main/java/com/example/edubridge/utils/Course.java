package com.example.edubridge.utils;

public class Course {

    private String title;
    private String description;
    private String subject;
    private String difficulty;   // Beginner / Intermediate / Advanced
    private String learningPath; // Academic / Skill / Exam
    private int imageResId;

    public Course(String title,
                  String description,
                  String subject,
                  String difficulty,
                  String learningPath,
                  int imageResId) {

        this.title = title;
        this.description = description;
        this.subject = subject;
        this.difficulty = difficulty;
        this.learningPath = learningPath;
        this.imageResId = imageResId;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getSubject() { return subject; }
    public String getDifficulty() { return difficulty; }
    public String getLearningPath() { return learningPath; }
    public int getImageResId() { return imageResId; }
}
