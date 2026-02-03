package com.example.thrifttime.models;

public class Comment {
    private String commentId;
    private String userId;
    private String userName;
    private String commentText;
    private long timestamp;

    public Comment() {}

    public Comment(String commentId, String userId, String userName, String commentText, long timestamp) {
        this.commentId = commentId;
        this.userId = userId;
        this.userName = userName;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    public String getCommentId() { return commentId; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getCommentText() { return commentText; }
    public long getTimestamp() { return timestamp; }
}