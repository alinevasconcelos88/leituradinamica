package com.example.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LetterItem implements Serializable {
    private final String letter;
    private final String pronunciation;
    private final List<String> exampleWords;
    private final int iconResId;
    private final String soundDescription;

    public LetterItem(String letter, String pronunciation, List<String> exampleWords, int iconResId, String soundDescription) {
        this.letter = letter;
        this.pronunciation = pronunciation;
        this.exampleWords = exampleWords != null ? exampleWords : new ArrayList<>();
        this.iconResId = iconResId;
        this.soundDescription = soundDescription;
    }

    public LetterItem(String letter, String pronunciation, String exampleWord, int iconResId, String soundDescription) {
        this.letter = letter;
        this.pronunciation = pronunciation;
        this.exampleWords = new ArrayList<>(Arrays.asList(exampleWord));
        this.iconResId = iconResId;
        this.soundDescription = soundDescription;
    }

    public String getLetter() { return letter; }
    public String getPronunciation() { return pronunciation; }
    public List<String> getExampleWords() { return exampleWords; }
    
    public String getExampleWord() { 
        return (exampleWords != null && !exampleWords.isEmpty()) ? exampleWords.get(0) : letter;
    }
    
    public String getWordsSummary() {
        if (exampleWords == null || exampleWords.isEmpty()) return letter;
        if (exampleWords.size() <= 2) {
            return String.join(", ", exampleWords);
        }
        return exampleWords.get(0) + ", " + exampleWords.get(1) + " (+" + (exampleWords.size() - 2) + ")";
    }

    public String getWordsDisplay() {
        if (exampleWords == null || exampleWords.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exampleWords.size(); i++) {
            sb.append(exampleWords.get(i));
            if (i < exampleWords.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public String getWordsCountText() {
        int count = exampleWords != null ? exampleWords.size() : 0;
        return count + " palavras";
    }

    public int getIconResId() { return iconResId; }
    public String getSoundDescription() { return soundDescription; }
}
