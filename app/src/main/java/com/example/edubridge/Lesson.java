package com.example.edubridge;

import java.util.ArrayList;
import java.util.List;

/**
 * M3.4.1 Lesson - Individual learning unit within a module
 * M3.4.5: Supports different content types
 */
public class Lesson {

    // Content type constants
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_QUIZ = "quiz";
    public static final String TYPE_DOWNLOAD = "download";

    private String lessonId;
    private String moduleId;
    private String title;
    private String description;
    private int orderIndex;
    private String contentType; // M3.4.5: text, video, quiz, download
    private String contentUrl; // Video URL or download URL
    private String textContent; // For text lessons
    private int durationMinutes; // Estimated time
    private boolean isLocked; // M3.4.2: Sequential progression
    private boolean isCompleted; // M3.4.4: Completion status
    private long completedAt; // Timestamp of completion
    private int lastPosition; // M3.4.3: Resume position (for video)
    private List<QuizQuestion> quizQuestions; // For quiz lessons

    public Lesson() {
        this.quizQuestions = new ArrayList<>();
    }

    public Lesson(String lessonId, String moduleId, String title,
            String contentType, int orderIndex) {
        this.lessonId = lessonId;
        this.moduleId = moduleId;
        this.title = title;
        this.contentType = contentType;
        this.orderIndex = orderIndex;
        this.isLocked = orderIndex > 0;
        this.isCompleted = false;
        this.quizQuestions = new ArrayList<>();
    }

    // Getters and Setters
    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public List<QuizQuestion> getQuizQuestions() {
        return quizQuestions;
    }

    public void setQuizQuestions(List<QuizQuestion> quizQuestions) {
        this.quizQuestions = quizQuestions;
    }

    /**
     * Get icon resource based on content type.
     */
    public int getTypeIcon() {
        switch (contentType) {
            case TYPE_VIDEO:
                return android.R.drawable.ic_media_play;
            case TYPE_QUIZ:
                return android.R.drawable.ic_menu_help;
            case TYPE_DOWNLOAD:
                return android.R.drawable.stat_sys_download;
            case TYPE_TEXT:
            default:
                return android.R.drawable.ic_menu_edit;
        }
    }

    /**
     * Inner class for quiz questions.
     */
    public static class QuizQuestion {
        private String question;
        private List<String> options;
        private int correctAnswerIndex;

        public QuizQuestion() {
            this.options = new ArrayList<>();
        }

        public QuizQuestion(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctIndex;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }

        public List<String> getOptions() {
            return options;
        }

        public void setOptions(List<String> options) {
            this.options = options;
        }

        public int getCorrectAnswerIndex() {
            return correctAnswerIndex;
        }

        public void setCorrectAnswerIndex(int correctAnswerIndex) {
            this.correctAnswerIndex = correctAnswerIndex;
        }

        public boolean isCorrect(int selectedIndex) {
            return selectedIndex == correctAnswerIndex;
        }
    }
}
