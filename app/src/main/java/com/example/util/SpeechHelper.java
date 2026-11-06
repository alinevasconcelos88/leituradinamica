package com.example.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpeechHelper {
    private static final String TAG = "SpeechHelper";
    private static SpeechHelper instance;

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private float currentSpeechRate = 1.0f; // 1.0f, 1.5f, 2.0f
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Runnable highlightTickerRunnable;
    private boolean isSpeaking = false;

    public interface WordHighlightListener {
        void onWordHighlighted(int wordIndex, String word, int startChar, int endChar);
        void onSpeechCompleted();
        void onSpeechStarted();
    }

    private SpeechHelper(Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(new Locale("pt", "BR"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to standard Portuguese or default
                    tts.setLanguage(new Locale("pt"));
                }
                tts.setPitch(1.08f);
                tts.setSpeechRate(currentSpeechRate);
                isInitialized = true;
                Log.d(TAG, "TTS Initialized successfully with speech rate: " + currentSpeechRate);
            } else {
                Log.e(TAG, "TTS initialization failed with code: " + status);
            }
        });
    }

    public static synchronized SpeechHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SpeechHelper(context);
        }
        return instance;
    }

    public void setSpeechRate(float rate) {
        this.currentSpeechRate = rate;
        if (tts != null && isInitialized) {
            tts.setSpeechRate(rate);
        }
    }

    public float getSpeechRate() {
        return currentSpeechRate;
    }

    public void speakSimple(String text) {
        if (tts == null || !isInitialized || text == null || text.trim().isEmpty()) {
            return;
        }
        stop();
        tts.setSpeechRate(currentSpeechRate);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utterance_simple_" + System.currentTimeMillis());
    }

    public void speakSynchronized(String fullText, WordHighlightListener listener) {
        if (fullText == null || fullText.trim().isEmpty()) {
            if (listener != null) listener.onSpeechCompleted();
            return;
        }

        stop();
        isSpeaking = true;
        if (listener != null) listener.onSpeechStarted();

        if (tts != null && isInitialized) {
            tts.setSpeechRate(currentSpeechRate);
            String utteranceId = "utterance_sync_" + System.currentTimeMillis();
            tts.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }

        // Tokenize words and character boundaries
        List<WordSpan> spans = parseWordSpans(fullText);
        if (spans.isEmpty()) {
            if (listener != null) listener.onSpeechCompleted();
            return;
        }

        // Calculate timing per word scaled inversely by currentSpeechRate
        // Base reading speed at 1x is approx 280ms base + 45ms per character
        final float rate = (currentSpeechRate <= 0.1f) ? 1.0f : currentSpeechRate;

        scheduleWordHighlight(spans, 0, rate, listener);
    }

    private void scheduleWordHighlight(List<WordSpan> spans, int index, float rate, WordHighlightListener listener) {
        if (!isSpeaking) return;

        if (index >= spans.size()) {
            isSpeaking = false;
            if (listener != null) listener.onSpeechCompleted();
            return;
        }

        WordSpan current = spans.get(index);
        if (listener != null) {
            listener.onWordHighlighted(index, current.word, current.start, current.end);
        }

        // Duration for this word based on word length and speech rate
        long durationMs = (long) ((260L + (current.word.length() * 45L)) / rate);

        highlightTickerRunnable = () -> {
            if (isSpeaking) {
                scheduleWordHighlight(spans, index + 1, rate, listener);
            }
        };
        mainHandler.postDelayed(highlightTickerRunnable, durationMs);
    }

    public void stop() {
        isSpeaking = false;
        if (highlightTickerRunnable != null) {
            mainHandler.removeCallbacks(highlightTickerRunnable);
            highlightTickerRunnable = null;
        }
        if (tts != null && isInitialized) {
            tts.stop();
        }
    }

    public boolean isSpeaking() {
        return isSpeaking || (tts != null && tts.isSpeaking());
    }

    private List<WordSpan> parseWordSpans(String text) {
        List<WordSpan> spans = new ArrayList<>();
        int len = text.length();
        int start = -1;

        for (int i = 0; i < len; i++) {
            char c = text.charAt(i);
            boolean isWordChar = Character.isLetterOrDigit(c) || c == '-' || c == '\'';
            if (isWordChar) {
                if (start == -1) {
                    start = i;
                }
            } else {
                if (start != -1) {
                    spans.add(new WordSpan(text.substring(start, i), start, i));
                    start = -1;
                }
            }
        }
        if (start != -1) {
            spans.add(new WordSpan(text.substring(start, len), start, len));
        }
        return spans;
    }

    private static class WordSpan {
        final String word;
        final int start;
        final int end;

        WordSpan(String word, int start, int end) {
            this.word = word;
            this.start = start;
            this.end = end;
        }
    }
}
