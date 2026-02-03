package com.example.thrifttime.models;

public class User {
    private String userId;
    private String email;
    private String displayName;
    private String userType;
    private int points;
    private String photoUrl;
    private String createdAt;
    private String notificationToken;

    public User() {
        // Empty constructor needed for Firebase
    }

    public User(String userId, String email, String displayName, String userType) {
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.userType = userType;
        this.points = 0;
        this.createdAt = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getNotificationToken() { return notificationToken; }
    public void setNotificationToken(String notificationToken) { this.notificationToken = notificationToken; }
}