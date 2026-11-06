package com.example.model;

import java.io.Serializable;

public class WordItem implements Serializable {
    private final String word;
    private final String syllables;
    private final String category;
    private final int iconResId;
    private final String exampleSentence;

    public WordItem(String word, String syllables, String category, int iconResId, String exampleSentence) {
        this.word = word;
        this.syllables = syllables;
        this.category = category;
        this.iconResId = iconResId;
        this.exampleSentence = exampleSentence;
    }

    public String getWord() { return word; }
    public String getSyllables() { return syllables; }
    public String getCategory() { return category; }
    public int getIconResId() { return iconResId; }
    public String getExampleSentence() { return exampleSentence; }
}
