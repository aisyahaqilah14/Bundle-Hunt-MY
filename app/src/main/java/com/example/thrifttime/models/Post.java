package com.example.thrifttime.models;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private String postId;
    private String userId;
    private String userName;
    private String handle;
    private String userProfileUrl;
    private String description;
    private String imageUrl;
    private String location;
    private long timestamp;
    private int likesCount;
    private int commentCount;
    private List<String> likedBy;

    public Post() {} // REQUIRED

    public Post(String userId, String userName, String userProfileUrl, String description,
                String imageUrl, String location, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.userProfileUrl = userProfileUrl;
        this.description = description;
        this.imageUrl = imageUrl;
        this.location = location;
        this.timestamp = timestamp;
        this.likesCount = 0;
        this.commentCount = 0;
        this.likedBy = new ArrayList<>();
    }

    // Getters and Setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }
    public String getUserProfileUrl() { return userProfileUrl; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getLocation() { return location; }
    public long getTimestamp() { return timestamp; }
    public int getLikesCount() { return likesCount; }
    public void setLikesCount(int likesCount) { this.likesCount = likesCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
}