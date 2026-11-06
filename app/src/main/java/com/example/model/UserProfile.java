package com.example.model;

import java.io.Serializable;

public class UserProfile implements Serializable {
    private String email;
    private String childName;
    private int childAge;
    private int points;
    private int stars;
    private int lettersLearned;
    private int wordsLearned;
    private int booksRead;
    private int activitiesCompleted;
    private int totalTimeSpentMinutes;
    private float speechRate; // 1.0f, 1.5f, 2.0f
    private int timeLimitMinutes; // 15, 30, 45, 60, 0 = unlimited
    private boolean highContrast;
    private boolean soundEffects;
    private float fontSizeMultiplier;
    private boolean onboardingCompleted;
    private String subscriptionStatus; // "TRIAL", "ATIVO", "PENDENTE", "CANCELADO"
    private int trialDaysRemaining;
    private String paymentMethod;
    private boolean isDarkMode;

    public UserProfile() {
        this.email = "familia@leituradinamica.com";
        this.childName = "Davi";
        this.childAge = 6;
        this.points = 150;
        this.stars = 18;
        this.lettersLearned = 16;
        this.wordsLearned = 24;
        this.booksRead = 3;
        this.activitiesCompleted = 35;
        this.totalTimeSpentMinutes = 48;
        this.speechRate = 1.0f;
        this.timeLimitMinutes = 30;
        this.highContrast = false;
        this.soundEffects = true;
        this.fontSizeMultiplier = 1.0f;
        this.onboardingCompleted = false;
        this.subscriptionStatus = "TRIAL";
        this.trialDaysRemaining = 28;
        this.paymentMethod = "Pix";
        this.isDarkMode = false;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getChildName() { return childName; }
    public void setChildName(String childName) { this.childName = childName; }

    public int getChildAge() { return childAge; }
    public void setChildAge(int childAge) { this.childAge = childAge; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public void addPoints(int amount) { this.points += amount; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
    public void addStars(int amount) { this.stars += amount; }

    public int getLettersLearned() { return lettersLearned; }
    public void setLettersLearned(int lettersLearned) { this.lettersLearned = lettersLearned; }

    public int getWordsLearned() { return wordsLearned; }
    public void setWordsLearned(int wordsLearned) { this.wordsLearned = wordsLearned; }

    public int getBooksRead() { return booksRead; }
    public void setBooksRead(int booksRead) { this.booksRead = booksRead; }

    public int getActivitiesCompleted() { return activitiesCompleted; }
    public void setActivitiesCompleted(int activitiesCompleted) { this.activitiesCompleted = activitiesCompleted; }
    public void incrementActivities() { this.activitiesCompleted++; }

    public int getTotalTimeSpentMinutes() { return totalTimeSpentMinutes; }
    public void setTotalTimeSpentMinutes(int totalTimeSpentMinutes) { this.totalTimeSpentMinutes = totalTimeSpentMinutes; }

    public float getSpeechRate() { return speechRate; }
    public void setSpeechRate(float speechRate) { this.speechRate = speechRate; }

    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(int timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public boolean isHighContrast() { return highContrast; }
    public void setHighContrast(boolean highContrast) { this.highContrast = highContrast; }

    public boolean isSoundEffects() { return soundEffects; }
    public void setSoundEffects(boolean soundEffects) { this.soundEffects = soundEffects; }

    public float getFontSizeMultiplier() { return fontSizeMultiplier; }
    public void setFontSizeMultiplier(float fontSizeMultiplier) { this.fontSizeMultiplier = fontSizeMultiplier; }

    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }

    public String getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(String subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public int getTrialDaysRemaining() { return trialDaysRemaining; }
    public void setTrialDaysRemaining(int trialDaysRemaining) { this.trialDaysRemaining = trialDaysRemaining; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public boolean isDarkMode() { return isDarkMode; }
    public void setDarkMode(boolean darkMode) { this.isDarkMode = darkMode; }
}
