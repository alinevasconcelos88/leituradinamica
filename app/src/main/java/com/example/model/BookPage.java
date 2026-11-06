package com.example.model;

import java.io.Serializable;

public class BookPage implements Serializable {
    private final int pageNumber;
    private final String text;
    private final int illustrationResId;

    public BookPage(int pageNumber, String text, int illustrationResId) {
        this.pageNumber = pageNumber;
        this.text = text;
        this.illustrationResId = illustrationResId;
    }

    public int getPageNumber() { return pageNumber; }
    public String getText() { return text; }
    public int getIllustrationResId() { return illustrationResId; }
}
