package com.example.model;

import java.io.Serializable;

public class ActivityHistoryItem implements Serializable {
    private final String title;
    private final String timestamp;
    private final int pointsEarned;
    private final String category;

    public ActivityHistoryItem(String title, String timestamp, int pointsEarned, String category) {
        this.title = title;
        this.timestamp = timestamp;
        this.pointsEarned = pointsEarned;
        this.category = category;
    }

    public String getTitle() { return title; }
    public String getTimestamp() { return timestamp; }
    public int getPointsEarned() { return pointsEarned; }
    public String getCategory() { return category; }
}
