package com.example.model;

import java.io.Serializable;

public class SentenceItem implements Serializable {
    private final String id;
    private final String text;
    private final int iconResId;
    private final String themeName;

    public SentenceItem(String id, String text, int iconResId, String themeName) {
        this.id = id;
        this.text = text;
        this.iconResId = iconResId;
        this.themeName = themeName;
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public int getIconResId() { return iconResId; }
    public String getThemeName() { return themeName; }
}
