package com.example.thrifttime.models;

public class Photo {
    private String photoId;
    private String storeId;
    private String userId;
    private String photoUrl;
    private String caption;
    private int likes;
    private String createdAt;

    public Photo() {
        // Empty constructor for Firebase
    }

    public Photo(String photoId, String storeId, String userId, String photoUrl) {
        this.photoId = photoId;
        this.storeId = storeId;
        this.userId = userId;
        this.photoUrl = photoUrl;
        this.likes = 0;
        this.createdAt = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getPhotoId() { return photoId; }
    public void setPhotoId(String photoId) { this.photoId = photoId; }

    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}