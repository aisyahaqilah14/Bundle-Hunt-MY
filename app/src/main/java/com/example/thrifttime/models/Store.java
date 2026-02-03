package com.example.thrifttime.models;

import java.util.ArrayList;
import java.util.List;

public class Store {
    private String storeId;
    private String name;
    private String description;
    private String address;
    private double latitude;
    private double longitude;
    private String phone;
    private String hours;
    private float rating;
    private int reviewCount;
    private List<String> photoUrls;

    public Store() {
        this.photoUrls = new ArrayList<>();
    }

    // --- ID Methods (Matches both your Adapter and MapsActivity) ---
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }

    // Alias for setId to prevent MapsActivity from crashing
    public void setId(String id) { this.storeId = id; }

    // --- Basic Info ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    // --- Hours and Description ---
    public String getOpeningHours() { return hours; }
    public void setOpeningHours(String openingHours) { this.hours = openingHours; }

    public String getHours() { return hours; }
    public void setHours(String hours) { this.hours = hours; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // --- Ratings and Reviews ---
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    // --- Photos ---
    public List<String> getPhotoUrls() { return photoUrls; }
    public void setPhotoUrls(List<String> photoUrls) { this.photoUrls = photoUrls; }
}