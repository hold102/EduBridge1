package com.example.edubridge;

public class CommunityPost {
    private final String name;
    private final String content;
    private final String time;
    private final int avatarResId;

    public CommunityPost(String name, String content, String time, int avatarResId) {
        this.name = name;
        this.content = content;
        this.time = time;
        this.avatarResId = avatarResId;
    }

    public String getName() {
        return name;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public int getAvatarResId() {
        return avatarResId;
    }
}
