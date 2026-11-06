package com.example.model;

import java.io.Serializable;
import java.util.List;

public class BookStory implements Serializable {
    private final String id;
    private final String title;
    private final String summary;
    private final int coverResId;
    private final List<BookPage> pages;

    public BookStory(String id, String title, String summary, int coverResId, List<BookPage> pages) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.coverResId = coverResId;
        this.pages = pages;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public int getCoverResId() { return coverResId; }
    public List<BookPage> getPages() { return pages; }
}
