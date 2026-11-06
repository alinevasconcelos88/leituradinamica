package com.example;

import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adapter.LetterAdapter;
import com.example.data.AppRepository;
import com.example.model.ActivityHistoryItem;
import com.example.model.BookPage;
import com.example.model.BookStory;
import com.example.model.FeedbackMessage;
import com.example.model.LetterItem;
import com.example.model.PaymentRecord;
import com.example.model.SentenceItem;
import com.example.model.UserProfile;
import com.example.model.WordItem;
import com.example.util.SpeechHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    // Screen Indices in ViewFlipper
    private static final int SCREEN_SPLASH = 0;
    private static final int SCREEN_AUTH = 1;
    private static final int SCREEN_ONBOARDING = 2;
    private static final int SCREEN_MAIN_MENU = 3;
    private static final int SCREEN_ALPHABET = 4;
    private static final int SCREEN_WORDS = 5;
    private static final int SCREEN_SENTENCES = 6;
    private static final int SCREEN_GAMES = 7;
    private static final int SCREEN_GAME_MEMORY = 8;
    private static final int SCREEN_GAME_FILL_LETTER = 9;
    private static final int SCREEN_GAME_PICK_IMAGE = 10;
    private static final int SCREEN_BOOKS_LIST = 11;
    private static final int SCREEN_BOOK_READER = 12;
    private static final int SCREEN_EVOLUTION = 13;
    private static final int SCREEN_PARENT_GATE = 14;
    private static final int SCREEN_PARENT_DASHBOARD = 15;
    private static final int SCREEN_CONTACT_US = 16;
    private static final int SCREEN_ADMIN_DASHBOARD = 17;

    private String selectedContactCategory = "Sugestão";
    private String currentFinancialFilter = "TODOS";

    private ViewFlipper viewFlipper;
    private final Deque<Integer> navigationStack = new ArrayDeque<>();

    private AppRepository repository;
    private SpeechHelper speechHelper;
    private UserProfile currentProfile;

    // Navigation state
    private int currentWordIndex = 0;
    private int currentSentenceIndex = 0;
    private BookStory currentBook;
    private int currentBookPageIndex = 0;
    private int onboardingStep = 0;

    // Game 1: Memory Game State
    private static class MemoryCardSource {
        final String word;
        final int iconRes;

        MemoryCardSource(String word, int iconRes) {
            this.word = word;
            this.iconRes = iconRes;
        }
    }

    private static class MemoryCard {
        final String text;
        final int iconRes;
        final int pairId;
        boolean isRevealed = false;
        boolean isMatched = false;

        MemoryCard(String text, int iconRes, int pairId) {
            this.text = text;
            this.iconRes = iconRes;
            this.pairId = pairId;
        }
    }
    private final List<MemoryCard> memoryCards = new ArrayList<>();
    private int firstSelectedCardIndex = -1;
    private int matchedPairsCount = 0;
    private boolean isMemoryBusy = false;

    // Game 2: Fill Letter State
    private int fillRoundIndex = 0;
    private static class FillRound {
        final String wordPrompt;
        final String fullWord;
        final String missingLetter;
        final String[] options;
        final int imageRes;

        FillRound(String wordPrompt, String fullWord, String missingLetter, String[] options, int imageRes) {
            this.wordPrompt = wordPrompt;
            this.fullWord = fullWord;
            this.missingLetter = missingLetter;
            this.options = options;
            this.imageRes = imageRes;
        }
    }
    private final List<FillRound> fillRounds = new ArrayList<>();

    // Game 3: Pick Image State
    private int pickRoundIndex = 0;
    private static class PickRound {
        final String wordToFind;
        final int correctImageRes;
        final int[] optionsImageRes;
        final int correctOptionIndex;

        PickRound(String wordToFind, int correctImageRes, int[] optionsImageRes, int correctOptionIndex) {
            this.wordToFind = wordToFind;
            this.correctImageRes = correctImageRes;
            this.optionsImageRes = optionsImageRes;
            this.correctOptionIndex = correctOptionIndex;
        }
    }
    private final List<PickRound> pickRounds = new ArrayList<>();

    // Parental Gate state
    private int gateAnswer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        repository = AppRepository.getInstance(this);
        currentProfile = repository.getCurrentProfile();

        // Apply saved theme (Light mode or Dark mode)
        if (currentProfile.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speechHelper = SpeechHelper.getInstance(this);
        speechHelper.setSpeechRate(currentProfile.getSpeechRate());

        viewFlipper = findViewById(R.id.view_flipper);

        setupBackHandler();
        setupSplash();
        setupAuth();
        setupOnboarding();
        setupMainMenu();
        setupAlphabet();
        setupWords();
        setupSentences();
        setupGamesHub();
        setupGameMemory();
        setupGameFillLetter();
        setupGamePickImage();
        setupBooksList();
        setupBookReader();
        setupEvolution();
        setupParentGate();
        setupParentDashboard();
        setupContactUs();
        setupAdminDashboard();

        // Restore screen across theme changes or start at Splash
        if (savedInstanceState != null) {
            int savedScreen = savedInstanceState.getInt("CURRENT_SCREEN", SCREEN_SPLASH);
            ArrayList<Integer> savedStack = savedInstanceState.getIntegerArrayList("NAV_STACK");
            if (savedStack != null && !savedStack.isEmpty()) {
                navigationStack.clear();
                navigationStack.addAll(savedStack);
                viewFlipper.setDisplayedChild(savedScreen);
                refreshScreen(savedScreen);
            } else {
                navigateTo(savedScreen, false);
            }
        } else {
            navigateTo(SCREEN_SPLASH, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewFlipper != null) {
            outState.putInt("CURRENT_SCREEN", viewFlipper.getDisplayedChild());
        }
        outState.putIntegerArrayList("NAV_STACK", new ArrayList<>(navigationStack));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da Conta")
                .setMessage("Deseja sair da conta de " + currentProfile.getChildName() + " (" + currentProfile.getEmail() + ") e voltar para a tela de login?")
                .setPositiveButton("Sair e Trocar de Conta", (dialog, which) -> {
                    speechHelper.stop();
                    navigateTo(SCREEN_AUTH, true);
                    Toast.makeText(this, "Você saiu da conta. Digite os dados para entrar em outra conta.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void setupBackHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                speechHelper.stop();
                if (navigationStack.size() > 1) {
                    navigationStack.pop(); // Pop current
                    int previousScreen = navigationStack.peek();
                    viewFlipper.setDisplayedChild(previousScreen);
                    refreshScreen(previousScreen);
                } else {
                    finish();
                }
            }
        });
    }

    private void navigateTo(int screenIndex, boolean clearStack) {
        speechHelper.stop();
        if (clearStack) {
            navigationStack.clear();
        }
        navigationStack.push(screenIndex);
        viewFlipper.setDisplayedChild(screenIndex);
        refreshScreen(screenIndex);
    }

    private void refreshScreen(int screenIndex) {
        currentProfile = repository.getCurrentProfile();
        if (screenIndex == SCREEN_MAIN_MENU) {
            updateMainMenuHeader();
        } else if (screenIndex == SCREEN_PARENT_DASHBOARD) {
            updateParentDashboardUI();
        } else if (screenIndex == SCREEN_EVOLUTION) {
            updateEvolutionUI();
        } else if (screenIndex == SCREEN_PARENT_GATE) {
            generateNewParentGateChallenge();
        } else if (screenIndex == SCREEN_GAME_MEMORY) {
            initGameMemoryCards();
        } else if (screenIndex == SCREEN_GAME_FILL_LETTER) {
            initGameFillRounds();
        } else if (screenIndex == SCREEN_CONTACT_US) {
            updateContactUsUI();
        } else if (screenIndex == SCREEN_ADMIN_DASHBOARD) {
            updateAdminDashboardUI();
        }
    }

    // -------------------------------------------------------------
    // SCREEN 0: SPLASH
    // -------------------------------------------------------------
    private void setupSplash() {
        MaterialButton btnStart = findViewById(R.id.btn_splash_start);
        btnStart.setOnClickListener(v -> {
            if (currentProfile.isOnboardingCompleted()) {
                navigateTo(SCREEN_MAIN_MENU, true);
            } else {
                navigateTo(SCREEN_AUTH, false);
            }
        });
    }

    // -------------------------------------------------------------
    // SCREEN 1: AUTH (Login / Cadastro)
    // -------------------------------------------------------------
    private boolean isLoginMode = true;

    private void setupAuth() {
        Button btnTabLogin = findViewById(R.id.btn_tab_login);
        Button btnTabRegister = findViewById(R.id.btn_tab_register);
        EditText etEmail = findViewById(R.id.et_auth_email);
        EditText etPassword = findViewById(R.id.et_auth_password);
        EditText etPasswordConfirm = findViewById(R.id.et_auth_password_confirm);
        EditText etChildName = findViewById(R.id.et_auth_child_name);
        TextView tvForgot = findViewById(R.id.tv_auth_forgot_password);
        TextView tvStatus = findViewById(R.id.tv_auth_status);
        MaterialButton btnSubmit = findViewById(R.id.btn_auth_submit);
        MaterialButton btnGoogle = findViewById(R.id.btn_auth_google);
        Button btnDemo = findViewById(R.id.btn_auth_demo);

        btnTabLogin.setOnClickListener(v -> {
            isLoginMode = true;
            btnTabLogin.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
            btnTabLogin.setTextColor(Color.WHITE);
            btnTabRegister.setBackgroundColor(Color.TRANSPARENT);
            btnTabRegister.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            etPasswordConfirm.setVisibility(View.GONE);
            etChildName.setVisibility(View.GONE);
            tvForgot.setVisibility(View.VISIBLE);
            btnSubmit.setText("Acessar Minha Conta");
            tvStatus.setVisibility(View.GONE);
        });

        btnTabRegister.setOnClickListener(v -> {
            isLoginMode = false;
            btnTabRegister.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
            btnTabRegister.setTextColor(Color.WHITE);
            btnTabLogin.setBackgroundColor(Color.TRANSPARENT);
            btnTabLogin.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));

            etPasswordConfirm.setVisibility(View.VISIBLE);
            etChildName.setVisibility(View.VISIBLE);
            tvForgot.setVisibility(View.GONE);
            btnSubmit.setText("Criar Conta e Começar");
            tvStatus.setVisibility(View.GONE);
        });

        btnSubmit.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || !email.contains("@")) {
                tvStatus.setText("Por favor, digite um e-mail válido.");
                tvStatus.setVisibility(View.VISIBLE);
                return;
            }
            if (password.length() < 6) {
                tvStatus.setText("A senha deve ter pelo menos 6 caracteres.");
                tvStatus.setVisibility(View.VISIBLE);
                return;
            }

            if (!isLoginMode) {
                String confirm = etPasswordConfirm.getText().toString().trim();
                String childName = etChildName.getText().toString().trim();
                if (!password.equals(confirm)) {
                    tvStatus.setText("As senhas não coincidem.");
                    tvStatus.setVisibility(View.VISIBLE);
                    return;
                }
                if (!childName.isEmpty()) {
                    currentProfile.setChildName(childName);
                }
                boolean success = repository.registerUser(email, password);
                if (success) {
                    new AlertDialog.Builder(this)
                            .setTitle("Período Gratuito de Teste! 🎁")
                            .setMessage("Seja bem-vindo ao Leitura Dinâmica!\n\nVocê terá 30 dias de acesso gratuito e irrestrito para sua criança explorar todas as histórias, atividades de letras e jogos.\n\nApós este período de 30 dias, será cobrada uma taxa de apenas R$ 9,99 por mês para continuar utilizando o aplicativo.")
                            .setPositiveButton("Começar Meus 30 Dias Grátis", (dialog, which) -> {
                                navigateTo(SCREEN_ONBOARDING, true);
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    tvStatus.setText("Erro ao cadastrar. Tente novamente.");
                    tvStatus.setVisibility(View.VISIBLE);
                }
            } else {
                if ("admin@leituradinamica.com".equalsIgnoreCase(email) && "admin123".equals(password)) {
                    Toast.makeText(this, "Acesso Administrativo Autenticado!", Toast.LENGTH_SHORT).show();
                    navigateTo(SCREEN_ADMIN_DASHBOARD, true);
                    return;
                }

                boolean success = repository.loginUser(email, password);
                if (success) {
                    Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    if (currentProfile.isOnboardingCompleted()) {
                        navigateTo(SCREEN_MAIN_MENU, true);
                    } else {
                        navigateTo(SCREEN_ONBOARDING, true);
                    }
                } else {
                    tvStatus.setText("E-mail ou senha incorretos.");
                    tvStatus.setVisibility(View.VISIBLE);
                }
            }
        });

        tvForgot.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Recuperação de Senha");
            builder.setMessage("Digite a nova senha para sua conta " + (email.isEmpty() ? "" : "(" + email + ")") + ":");

            final EditText inputNewPwd = new EditText(this);
            inputNewPwd.setHint("Nova senha (mínimo 6 dígitos)");
            builder.setView(inputNewPwd);

            builder.setPositiveButton("Redefinir", (dialog, which) -> {
                String newPwd = inputNewPwd.getText().toString().trim();
                if (newPwd.length() >= 6) {
                    repository.resetPassword(email.isEmpty() ? "familia@leituradinamica.com" : email, newPwd);
                    Toast.makeText(this, "Senha redefinida com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "A senha deve ter no mínimo 6 dígitos.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });

        btnGoogle.setOnClickListener(v -> {
            repository.loginWithGoogle("familia.google@gmail.com", "Davi");
            Toast.makeText(this, "Autenticado via Google com sucesso!", Toast.LENGTH_SHORT).show();
            if (currentProfile.isOnboardingCompleted()) {
                navigateTo(SCREEN_MAIN_MENU, true);
            } else {
                navigateTo(SCREEN_ONBOARDING, true);
            }
        });

        btnDemo.setOnClickListener(v -> {
            repository.loginUser("demonstracao@leituradinamica.com", "123456");
            navigateTo(SCREEN_ONBOARDING, true);
        });
    }

    // -------------------------------------------------------------
    // SCREEN 2: ONBOARDING
    // -------------------------------------------------------------
    private void setupOnboarding() {
        ImageView imgArt = findViewById(R.id.img_onboarding_art);
        TextView tvTitle = findViewById(R.id.tv_onboarding_title);
        TextView tvDesc = findViewById(R.id.tv_onboarding_description);
        View dot1 = findViewById(R.id.dot_onboarding_1);
        View dot2 = findViewById(R.id.dot_onboarding_2);
        View dot3 = findViewById(R.id.dot_onboarding_3);
        MaterialButton btnPrev = findViewById(R.id.btn_onboarding_prev);
        MaterialButton btnNext = findViewById(R.id.btn_onboarding_next);
        Button btnSkip = findViewById(R.id.btn_onboarding_skip);

        Runnable updateSlides = () -> {
            if (onboardingStep == 0) {
                imgArt.setImageResource(R.drawable.img_hero_reading);
                tvTitle.setText("Aprenda Letras e Palavras");
                tvDesc.setText("Atividades educativas lúdicas com associação exata entre letras, imagens e sons.");
                dot1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                dot2.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                dot3.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                btnPrev.setVisibility(View.INVISIBLE);
                btnNext.setText("Próximo");
            } else if (onboardingStep == 1) {
                imgArt.setImageResource(R.drawable.img_book_story);
                tvTitle.setText("Leitura Dinâmica com Destaque");
                tvDesc.setText("Acompanhe histórias infantis com a palavra destacada exatamente no ritmo da voz falada!");
                dot1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                dot2.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                dot3.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setText("Próximo");
            } else {
                imgArt.setImageResource(R.drawable.img_reward_trophy);
                tvTitle.setText("Acompanhamento dos Pais");
                tvDesc.setText("Área protegida com controle funcional da velocidade da voz (1x, 1.5x, 2x) e limite de tempo.");
                dot1.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                dot2.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                dot3.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                btnPrev.setVisibility(View.VISIBLE);
                btnNext.setText("Começar Aventura!");
            }
        };

        btnNext.setOnClickListener(v -> {
            if (onboardingStep < 2) {
                onboardingStep++;
                updateSlides.run();
            } else {
                currentProfile.setOnboardingCompleted(true);
                repository.saveProfile(currentProfile);
                navigateTo(SCREEN_MAIN_MENU, true);
            }
        });

        btnPrev.setOnClickListener(v -> {
            if (onboardingStep > 0) {
                onboardingStep--;
                updateSlides.run();
            }
        });

        btnSkip.setOnClickListener(v -> {
            currentProfile.setOnboardingCompleted(true);
            repository.saveProfile(currentProfile);
            navigateTo(SCREEN_MAIN_MENU, true);
        });
    }

    // -------------------------------------------------------------
    // SCREEN 3: MENU PRINCIPAL
    // -------------------------------------------------------------
    private void setupMainMenu() {
        findViewById(R.id.card_menu_alphabet).setOnClickListener(v -> navigateTo(SCREEN_ALPHABET, false));
        findViewById(R.id.card_menu_words).setOnClickListener(v -> navigateTo(SCREEN_WORDS, false));
        findViewById(R.id.card_menu_sentences).setOnClickListener(v -> navigateTo(SCREEN_SENTENCES, false));
        findViewById(R.id.card_menu_games).setOnClickListener(v -> navigateTo(SCREEN_GAMES, false));
        findViewById(R.id.card_menu_books).setOnClickListener(v -> navigateTo(SCREEN_BOOKS_LIST, false));
        findViewById(R.id.card_menu_evolution).setOnClickListener(v -> navigateTo(SCREEN_EVOLUTION, false));
        findViewById(R.id.card_menu_parents).setOnClickListener(v -> navigateTo(SCREEN_PARENT_GATE, false));
        findViewById(R.id.btn_menu_stars).setOnClickListener(v -> navigateTo(SCREEN_EVOLUTION, false));

        // Click on person name / profile header to show logout and switch account
        View profileHeader = findViewById(R.id.layout_menu_profile_header);
        if (profileHeader != null) {
            profileHeader.setOnClickListener(v -> showLogoutDialog());
        }
        View tvGreeting = findViewById(R.id.tv_menu_greeting);
        if (tvGreeting != null) {
            tvGreeting.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void updateMainMenuHeader() {
        TextView tvGreeting = findViewById(R.id.tv_menu_greeting);
        TextView tvPoints = findViewById(R.id.tv_menu_points);
        TextView tvSpeedBadge = findViewById(R.id.tv_menu_speed_badge);

        tvGreeting.setText("Olá, " + currentProfile.getChildName() + "! 🌟");
        tvPoints.setText(currentProfile.getPoints() + " pts");
        tvSpeedBadge.setText("Velocidade da voz: " + currentProfile.getSpeechRate() + "x • Controle de tempo e progresso");
    }

    // -------------------------------------------------------------
    // SCREEN 4: ALFABETO
    // -------------------------------------------------------------
    private LetterItem selectedLetterItem;

    private void setupAlphabet() {
        ImageButton btnBack = findViewById(R.id.btn_alphabet_back);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialButton btnSpeakAll = findViewById(R.id.btn_alphabet_speak_all);
        MaterialButton btnQuiz = findViewById(R.id.btn_alphabet_quiz);

        RecyclerView rv = findViewById(R.id.rv_alphabet_grid);
        rv.setLayoutManager(new GridLayoutManager(this, 4));

        List<LetterItem> alphabet = repository.getAlphabetList();
        LetterAdapter adapter = new LetterAdapter(alphabet, item -> {
            displayLetterDetails(item);
            speechHelper.speakSimple(item.getPronunciation() + ". As palavras são: " + item.getWordsDisplay());
            repository.logActivity("Explorar Letra " + item.getLetter(), 5, "Alfabeto");
        });
        rv.setAdapter(adapter);

        if (btnSpeakAll != null) {
            btnSpeakAll.setOnClickListener(v -> {
                if (selectedLetterItem != null) {
                    speechHelper.speakSimple(selectedLetterItem.getPronunciation() + ". As palavras com a letra " + selectedLetterItem.getLetter() + " são: " + selectedLetterItem.getWordsDisplay());
                }
            });
        }

        btnQuiz.setOnClickListener(v -> navigateTo(SCREEN_GAME_FILL_LETTER, false));

        if (!alphabet.isEmpty()) {
            displayLetterDetails(alphabet.get(0));
        }
    }

    private void displayLetterDetails(LetterItem item) {
        if (item == null) return;
        selectedLetterItem = item;

        TextView tvDetailLetter = findViewById(R.id.tv_detail_letter);
        TextView tvDetailWord = findViewById(R.id.tv_detail_word);
        TextView tvDetailPhoneme = findViewById(R.id.tv_detail_phoneme);
        ImageView imgDetailExample = findViewById(R.id.img_detail_example);
        LinearLayout wordsContainer = findViewById(R.id.ll_letter_words_container);

        if (tvDetailLetter != null) tvDetailLetter.setText(item.getLetter());
        if (tvDetailWord != null) tvDetailWord.setText("Letra " + item.getLetter() + " • " + item.getExampleWords().size() + " palavras");
        if (tvDetailPhoneme != null) tvDetailPhoneme.setText(item.getSoundDescription());
        if (imgDetailExample != null) imgDetailExample.setImageResource(item.getIconResId());

        if (wordsContainer != null) {
            wordsContainer.removeAllViews();
            List<String> words = item.getExampleWords();
            float density = getResources().getDisplayMetrics().density;
            int margin = (int) (4 * density);
            int height = (int) (46 * density);

            LinearLayout currentRow = null;
            for (int i = 0; i < words.size(); i++) {
                if (i % 2 == 0) {
                    currentRow = new LinearLayout(this);
                    currentRow.setOrientation(LinearLayout.HORIZONTAL);
                    LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    rowParams.setMargins(0, (int) (3 * density), 0, (int) (3 * density));
                    currentRow.setLayoutParams(rowParams);
                    wordsContainer.addView(currentRow);
                }

                String word = words.get(i);
                MaterialButton chipBtn = new MaterialButton(this);
                chipBtn.setText(word);
                chipBtn.setAllCaps(false);
                chipBtn.setTextSize(14);
                chipBtn.setTypeface(null, android.graphics.Typeface.BOLD);
                chipBtn.setTextColor(Color.WHITE);
                chipBtn.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_volume));
                chipBtn.setIconTint(android.content.res.ColorStateList.valueOf(Color.WHITE));
                chipBtn.setIconSize((int) (16 * density));
                chipBtn.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);

                int bgRes = (i % 2 == 0) ? R.color.primary : R.color.secondary;
                chipBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(this, bgRes)
                ));
                chipBtn.setCornerRadius((int) (23 * density));

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        0,
                        height,
                        1.0f
                );
                btnParams.setMargins(margin, 0, margin, 0);
                chipBtn.setLayoutParams(btnParams);

                chipBtn.setOnClickListener(v -> {
                    speechHelper.speakSimple(word);
                    Toast.makeText(MainActivity.this, "Palavra com " + item.getLetter() + ": " + word + " 🔊", Toast.LENGTH_SHORT).show();
                });

                if (currentRow != null) {
                    currentRow.addView(chipBtn);
                }
            }
        }
    }

    // -------------------------------------------------------------
    // SCREEN 5: PALAVRAS
    // -------------------------------------------------------------
    private void setupWords() {
        ImageButton btnBack = findViewById(R.id.btn_words_back);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvCounter = findViewById(R.id.tv_words_counter);
        TextView tvCategory = findViewById(R.id.tv_word_category);
        ImageView imgPicture = findViewById(R.id.img_word_picture);
        TextView tvTitle = findViewById(R.id.tv_word_title);
        TextView tvSyllables = findViewById(R.id.tv_word_syllables);
        TextView tvSentence = findViewById(R.id.tv_word_sentence);
        MaterialButton btnSpeakWord = findViewById(R.id.btn_word_speak);
        MaterialButton btnSpeakSentence = findViewById(R.id.btn_word_speak_sentence);
        MaterialButton btnPrev = findViewById(R.id.btn_word_prev);
        MaterialButton btnNext = findViewById(R.id.btn_word_next);

        List<WordItem> words = repository.getWordsList();

        Runnable displayWord = () -> {
            if (currentWordIndex < 0) currentWordIndex = 0;
            if (currentWordIndex >= words.size()) currentWordIndex = words.size() - 1;

            WordItem item = words.get(currentWordIndex);
            tvCounter.setText((currentWordIndex + 1) + " / " + words.size());
            tvCategory.setText(item.getCategory());
            imgPicture.setImageResource(item.getIconResId());
            tvTitle.setText(item.getWord());
            tvSyllables.setText(item.getSyllables());
            tvSentence.setText(item.getExampleSentence());

            btnPrev.setEnabled(currentWordIndex > 0);
            btnNext.setEnabled(currentWordIndex < words.size() - 1);
        };

        displayWord.run();

        btnSpeakWord.setOnClickListener(v -> {
            WordItem item = words.get(currentWordIndex);
            speechHelper.speakSimple(item.getWord());
            repository.logActivity("Palavra: " + item.getWord(), 5, "Palavras");
        });

        btnSpeakSentence.setOnClickListener(v -> {
            WordItem item = words.get(currentWordIndex);
            speechHelper.speakSimple(item.getExampleSentence());
        });

        btnPrev.setOnClickListener(v -> {
            if (currentWordIndex > 0) {
                currentWordIndex--;
                displayWord.run();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentWordIndex < words.size() - 1) {
                currentWordIndex++;
                displayWord.run();
            }
        });
    }

    // -------------------------------------------------------------
    // SCREEN 6: FRASES (Com destaque sincronizado)
    // -------------------------------------------------------------
    private void setupSentences() {
        ImageButton btnBack = findViewById(R.id.btn_sentences_back);
        btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        TextView tvCounter = findViewById(R.id.tv_sentences_counter);
        ImageView imgIllustration = findViewById(R.id.img_sentence_illustration);
        TextView tvTheme = findViewById(R.id.tv_sentence_theme);
        TextView tvText = findViewById(R.id.tv_sentence_text);
        MaterialButton btnSpeak = findViewById(R.id.btn_sentence_speak);
        MaterialButton btnPrev = findViewById(R.id.btn_sentence_prev);
        MaterialButton btnNext = findViewById(R.id.btn_sentence_next);

        List<SentenceItem> sentences = repository.getSentencesList();

        Runnable displaySentence = () -> {
            if (currentSentenceIndex < 0) currentSentenceIndex = 0;
            if (currentSentenceIndex >= sentences.size()) currentSentenceIndex = sentences.size() - 1;

            SentenceItem item = sentences.get(currentSentenceIndex);
            tvCounter.setText((currentSentenceIndex + 1) + " / " + sentences.size());
            tvTheme.setText(item.getThemeName());
            imgIllustration.setImageResource(item.getIconResId());
            tvText.setText(item.getText());

            btnPrev.setEnabled(currentSentenceIndex > 0);
            btnNext.setEnabled(currentSentenceIndex < sentences.size() - 1);
        };

        displaySentence.run();

        btnSpeak.setOnClickListener(v -> {
            SentenceItem item = sentences.get(currentSentenceIndex);
            String fullText = item.getText();

            speechHelper.speakSynchronized(fullText, new SpeechHelper.WordHighlightListener() {
                @Override
                public void onWordHighlighted(int wordIndex, String word, int startChar, int endChar) {
                    SpannableString spannable = new SpannableString(fullText);
                    spannable.setSpan(
                            new BackgroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.highlight_reading)),
                            startChar,
                            endChar,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    spannable.setSpan(
                            new ForegroundColorSpan(ContextCompat.getColor(MainActivity.this, R.color.text_primary)),
                            startChar,
                            endChar,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    tvText.setText(spannable);
                }

                @Override
                public void onSpeechCompleted() {
                    tvText.setText(fullText);
                    repository.logActivity("Leitura de Frase Concluída", 10, "Frases");
                }

                @Override
                public void onSpeechStarted() {}
            });
        });

        btnPrev.setOnClickListener(v -> {
            if (currentSentenceIndex > 0) {
                currentSentenceIndex--;
                displaySentence.run();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentSentenceIndex < sentences.size() - 1) {
                currentSentenceIndex++;
                displaySentence.run();
            }
        });
    }

    // -------------------------------------------------------------
    // SCREEN 7: JOGOS HUB
    // -------------------------------------------------------------
    private void setupGamesHub() {
        findViewById(R.id.btn_games_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.card_game_memory_select).setOnClickListener(v -> {
            initGameMemoryCards();
            navigateTo(SCREEN_GAME_MEMORY, false);
        });
        findViewById(R.id.card_game_fill_letter_select).setOnClickListener(v -> {
            initGameFillRounds();
            navigateTo(SCREEN_GAME_FILL_LETTER, false);
        });
        findViewById(R.id.card_game_pick_image_select).setOnClickListener(v -> {
            initGamePickRounds();
            navigateTo(SCREEN_GAME_PICK_IMAGE, false);
        });
    }

    // -------------------------------------------------------------
    // SCREEN 8: JOGO DA MEMÓRIA
    // -------------------------------------------------------------
    private void setupGameMemory() {
        findViewById(R.id.btn_memory_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.btn_memory_restart).setOnClickListener(v -> {
            initGameMemoryCards();
        });
    }

    private void initGameMemoryCards() {
        memoryCards.clear();
        matchedPairsCount = 0;
        firstSelectedCardIndex = -1;
        isMemoryBusy = false;

        TextView tvScore = findViewById(R.id.tv_memory_score);
        TextView tvInstruction = findViewById(R.id.tv_memory_instruction);
        if (tvScore != null) tvScore.setText("Pares: 0 / 3");
        if (tvInstruction != null) tvInstruction.setText("Encontre os pares com as duas palavras iguais!");

        // Rich candidate pool of words - randomly selects different words every time!
        List<MemoryCardSource> candidatePool = new ArrayList<>();
        candidatePool.add(new MemoryCardSource("BOLA", R.drawable.ic_word_bola));
        candidatePool.add(new MemoryCardSource("GATO", R.drawable.ic_word_gato));
        candidatePool.add(new MemoryCardSource("CASA", R.drawable.ic_word_casa));
        candidatePool.add(new MemoryCardSource("SOL", R.drawable.ic_word_sol));
        candidatePool.add(new MemoryCardSource("DADO", R.drawable.ic_word_dado));
        candidatePool.add(new MemoryCardSource("FLOR", R.drawable.ic_word_flor));
        candidatePool.add(new MemoryCardSource("PEIXE", R.drawable.ic_word_peixe));
        candidatePool.add(new MemoryCardSource("URSO", R.drawable.ic_word_urso));
        candidatePool.add(new MemoryCardSource("AVIÃO", R.drawable.ic_word_aviao));
        candidatePool.add(new MemoryCardSource("ABELHA", R.drawable.ic_word_abelha));
        candidatePool.add(new MemoryCardSource("ESTRELA", R.drawable.ic_word_estrela));
        candidatePool.add(new MemoryCardSource("MAÇÃ", R.drawable.ic_word_maca));

        Collections.shuffle(candidatePool);

        // Pick 3 distinct words; create TWO IDENTICAL CARDS for each word
        for (int p = 0; p < 3; p++) {
            MemoryCardSource src = candidatePool.get(p);
            int pairId = p + 1;
            memoryCards.add(new MemoryCard(src.word, src.iconRes, pairId));
            memoryCards.add(new MemoryCard(src.word, src.iconRes, pairId));
        }

        Collections.shuffle(memoryCards);

        int[] cardIds = {R.id.card_mem_0, R.id.card_mem_1, R.id.card_mem_2, R.id.card_mem_3, R.id.card_mem_4, R.id.card_mem_5};
        int[] textIds = {R.id.tv_mem_0, R.id.tv_mem_1, R.id.tv_mem_2, R.id.tv_mem_3, R.id.tv_mem_4, R.id.tv_mem_5};
        int[] imgIds = {R.id.img_mem_0, R.id.img_mem_1, R.id.img_mem_2, R.id.img_mem_3, R.id.img_mem_4, R.id.img_mem_5};

        for (int i = 0; i < 6; i++) {
            final int index = i;
            androidx.cardview.widget.CardView cardView = findViewById(cardIds[i]);
            TextView tv = findViewById(textIds[i]);
            ImageView img = findViewById(imgIds[i]);

            if (tv != null) {
                tv.setText("❓");
                tv.setTextSize(32);
                tv.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
            }
            if (img != null) {
                img.setVisibility(View.GONE);
            }
            if (cardView != null) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_container));
                cardView.setOnClickListener(v -> handleMemoryCardClick(index, cardIds, textIds, imgIds));
            }
        }
    }

    private void handleMemoryCardClick(int index, int[] cardIds, int[] textIds, int[] imgIds) {
        if (isMemoryBusy) return;
        MemoryCard card = memoryCards.get(index);
        if (card.isRevealed || card.isMatched) return;

        card.isRevealed = true;
        androidx.cardview.widget.CardView cardView = findViewById(cardIds[index]);
        TextView tv = findViewById(textIds[index]);
        ImageView img = findViewById(imgIds[index]);

        if (img != null) {
            img.setImageResource(card.iconRes);
            img.setVisibility(View.VISIBLE);
        }
        if (tv != null) {
            tv.setText(card.text);
            tv.setTextSize(15);
            tv.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
        }
        if (cardView != null) {
            cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.surface));
        }

        speechHelper.speakSimple(card.text);

        if (firstSelectedCardIndex == -1) {
            firstSelectedCardIndex = index;
        } else {
            MemoryCard firstCard = memoryCards.get(firstSelectedCardIndex);
            final int firstIdx = firstSelectedCardIndex;

            if (firstCard.pairId == card.pairId) {
                // Match! Both cards have the exact same word
                firstCard.isMatched = true;
                card.isMatched = true;
                matchedPairsCount++;

                androidx.cardview.widget.CardView cardView1 = findViewById(cardIds[firstIdx]);
                TextView tv1 = findViewById(textIds[firstIdx]);
                if (cardView1 != null) cardView1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.tertiary_container));
                if (cardView != null) cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.tertiary_container));
                if (tv1 != null) {
                    tv1.setTextColor(ContextCompat.getColor(this, R.color.on_tertiary_container));
                    tv1.setText("✓ " + firstCard.text);
                }
                if (tv != null) {
                    tv.setTextColor(ContextCompat.getColor(this, R.color.on_tertiary_container));
                    tv.setText("✓ " + card.text);
                }

                TextView tvScore = findViewById(R.id.tv_memory_score);
                if (tvScore != null) tvScore.setText("Pares: " + matchedPairsCount + " / 3");

                speechHelper.speakSimple("Muito bem! As duas palavras " + card.text + " são iguais!");
                firstSelectedCardIndex = -1;

                if (matchedPairsCount == 3) {
                    TextView tvInstruction = findViewById(R.id.tv_memory_instruction);
                    if (tvInstruction != null) tvInstruction.setText("🎉 Parabéns! Você encontrou todos os pares de palavras iguais!");
                    repository.logActivity("Jogo da Memória Concluído", 25, "Jogos");
                    Toast.makeText(this, "🎉 Parabéns! Você acertou todas as palavras iguais! +25 Pontos!", Toast.LENGTH_LONG).show();
                }
            } else {
                // Not match - Flip both cards back after short delay
                isMemoryBusy = true;
                firstSelectedCardIndex = -1;
                tv.postDelayed(() -> {
                    firstCard.isRevealed = false;
                    card.isRevealed = false;

                    androidx.cardview.widget.CardView cardView1 = findViewById(cardIds[firstIdx]);
                    TextView tv1 = findViewById(textIds[firstIdx]);
                    ImageView img1 = findViewById(imgIds[firstIdx]);

                    if (cardView1 != null) cardView1.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_container));
                    if (tv1 != null) {
                        tv1.setText("❓");
                        tv1.setTextSize(32);
                        tv1.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
                    }
                    if (img1 != null) img1.setVisibility(View.GONE);

                    if (cardView != null) cardView.setCardBackgroundColor(ContextCompat.getColor(this, R.color.primary_container));
                    if (tv != null) {
                        tv.setText("❓");
                        tv.setTextSize(32);
                        tv.setTextColor(ContextCompat.getColor(this, R.color.primary_dark));
                    }
                    if (img != null) img.setVisibility(View.GONE);

                    isMemoryBusy = false;
                }, 1000);
            }
        }
    }

    // -------------------------------------------------------------
    // SCREEN 9: COMPLETE A PALAVRA
    // -------------------------------------------------------------
    private void setupGameFillLetter() {
        findViewById(R.id.btn_fill_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initGameFillRounds() {
        fillRounds.clear();
        fillRounds.add(new FillRound("G _ T O", "GATO", "A", new String[]{"E", "A", "U"}, R.drawable.ic_word_gato));
        fillRounds.add(new FillRound("B _ L A", "BOLA", "O", new String[]{"I", "O", "E"}, R.drawable.ic_word_bola));
        fillRounds.add(new FillRound("C _ S A", "CASA", "A", new String[]{"A", "E", "O"}, R.drawable.ic_word_casa));
        fillRounds.add(new FillRound("S _ L", "SOL", "O", new String[]{"U", "A", "O"}, R.drawable.ic_word_sol));
        fillRounds.add(new FillRound("D _ D O", "DADO", "A", new String[]{"O", "A", "I"}, R.drawable.ic_word_dado));
        fillRounds.add(new FillRound("F L _ R", "FLOR", "O", new String[]{"E", "O", "A"}, R.drawable.ic_word_flor));
        fillRounds.add(new FillRound("P _ I X E", "PEIXE", "E", new String[]{"A", "I", "E"}, R.drawable.ic_word_peixe));
        fillRounds.add(new FillRound("_ R S O", "URSO", "U", new String[]{"O", "U", "A"}, R.drawable.ic_word_urso));
        fillRounds.add(new FillRound("M _ Ç Ã", "MAÇÃ", "A", new String[]{"E", "A", "O"}, R.drawable.ic_word_maca));
        fillRounds.add(new FillRound("_ B E L H A", "ABELHA", "A", new String[]{"O", "E", "A"}, R.drawable.ic_word_abelha));

        Collections.shuffle(fillRounds);
        fillRoundIndex = 0;
        displayFillRound();
    }

    private void displayFillRound() {
        if (fillRoundIndex >= fillRounds.size()) {
            fillRoundIndex = 0;
        }
        FillRound round = fillRounds.get(fillRoundIndex);

        ImageView imgPic = findViewById(R.id.img_fill_picture);
        TextView tvPrompt = findViewById(R.id.tv_fill_prompt);
        TextView tvFeedback = findViewById(R.id.tv_fill_feedback);
        MaterialButton btnHear = findViewById(R.id.btn_fill_hear);
        MaterialButton btn1 = findViewById(R.id.btn_fill_opt_1);
        MaterialButton btn2 = findViewById(R.id.btn_fill_opt_2);
        MaterialButton btn3 = findViewById(R.id.btn_fill_opt_3);
        MaterialButton btnNext = findViewById(R.id.btn_fill_next_round);

        imgPic.setImageResource(round.imageRes);
        tvPrompt.setText(round.wordPrompt);
        tvFeedback.setVisibility(View.GONE);

        // Ensure big, bold white text on solid blue button
        btn1.setText(round.options[0]);
        btn1.setTextColor(Color.WHITE);
        btn1.setTextSize(34);
        btn1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        btn1.setEnabled(true);

        btn2.setText(round.options[1]);
        btn2.setTextColor(Color.WHITE);
        btn2.setTextSize(34);
        btn2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        btn2.setEnabled(true);

        btn3.setText(round.options[2]);
        btn3.setTextColor(Color.WHITE);
        btn3.setTextSize(34);
        btn3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
        btn3.setEnabled(true);

        btnHear.setOnClickListener(v -> speechHelper.speakSimple(round.fullWord));

        View.OnClickListener optClick = v -> {
            MaterialButton clicked = (MaterialButton) v;
            String chosen = clicked.getText().toString();
            if (chosen.equalsIgnoreCase(round.missingLetter)) {
                tvPrompt.setText(round.fullWord);
                tvFeedback.setText("⭐ Muito bem! Você completou: " + round.fullWord);
                tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.success));
                tvFeedback.setVisibility(View.VISIBLE);
                clicked.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tertiary)));
                clicked.setTextColor(Color.WHITE);
                speechHelper.speakSimple("Muito bem! Letra " + round.missingLetter + " completa a palavra " + round.fullWord);
                repository.logActivity("Complete a Palavra: " + round.fullWord, 20, "Jogos");
            } else {
                tvFeedback.setText("Tente novamente! Escute o som da palavra.");
                tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
                tvFeedback.setVisibility(View.VISIBLE);
                clicked.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
                clicked.setTextColor(Color.WHITE);
                speechHelper.speakSimple("Tente outra letra!");
            }
        };

        btn1.setOnClickListener(optClick);
        btn2.setOnClickListener(optClick);
        btn3.setOnClickListener(optClick);

        btnNext.setOnClickListener(v -> {
            fillRoundIndex++;
            displayFillRound();
        });
    }

    // -------------------------------------------------------------
    // SCREEN 10: QUAL É A IMAGEM?
    // -------------------------------------------------------------
    private void setupGamePickImage() {
        findViewById(R.id.btn_pick_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void initGamePickRounds() {
        pickRounds.clear();
        pickRounds.add(new PickRound(
                "FLOR",
                R.drawable.ic_word_flor,
                new int[]{R.drawable.ic_word_aviao, R.drawable.ic_word_flor, R.drawable.ic_word_bola},
                1
        ));
        pickRounds.add(new PickRound(
                "ABELHA",
                R.drawable.ic_word_abelha,
                new int[]{R.drawable.ic_word_abelha, R.drawable.ic_word_casa, R.drawable.ic_word_dado},
                0
        ));
        pickRounds.add(new PickRound(
                "PEIXE",
                R.drawable.ic_word_peixe,
                new int[]{R.drawable.ic_word_sol, R.drawable.ic_word_urso, R.drawable.ic_word_peixe},
                2
        ));

        pickRoundIndex = 0;
        displayPickRound();
    }

    private void displayPickRound() {
        if (pickRoundIndex >= pickRounds.size()) {
            pickRoundIndex = 0;
        }
        PickRound round = pickRounds.get(pickRoundIndex);

        TextView tvPrompt = findViewById(R.id.tv_pick_word_prompt);
        TextView tvFeedback = findViewById(R.id.tv_pick_feedback);
        MaterialButton btnSpeakAgain = findViewById(R.id.btn_pick_speak_again);
        ImageView img0 = findViewById(R.id.img_pick_opt_0);
        ImageView img1 = findViewById(R.id.img_pick_opt_1);
        ImageView img2 = findViewById(R.id.img_pick_opt_2);
        MaterialButton btnNext = findViewById(R.id.btn_pick_next_round);

        tvPrompt.setText("Toque na: " + round.wordToFind + " 🎯");
        tvFeedback.setVisibility(View.GONE);

        img0.setImageResource(round.optionsImageRes[0]);
        img1.setImageResource(round.optionsImageRes[1]);
        img2.setImageResource(round.optionsImageRes[2]);

        speechHelper.speakSimple("Toque na imagem da palavra: " + round.wordToFind);

        btnSpeakAgain.setOnClickListener(v -> speechHelper.speakSimple(round.wordToFind));

        View.OnClickListener pickClick = v -> {
            int selectedOpt = (v.getId() == R.id.card_pick_opt_0) ? 0 :
                    (v.getId() == R.id.card_pick_opt_1) ? 1 : 2;

            if (selectedOpt == round.correctOptionIndex) {
                tvFeedback.setText("🎉 Excelente! Você acertou!");
                tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.success));
                tvFeedback.setVisibility(View.VISIBLE);
                speechHelper.speakSimple("Excelente! Você acertou!");
                repository.logActivity("Qual é a Imagem: " + round.wordToFind, 20, "Jogos");
            } else {
                tvFeedback.setText("Essa não é a figura correta. Tente de novo!");
                tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
                tvFeedback.setVisibility(View.VISIBLE);
                speechHelper.speakSimple("Tente de novo!");
            }
        };

        findViewById(R.id.card_pick_opt_0).setOnClickListener(pickClick);
        findViewById(R.id.card_pick_opt_1).setOnClickListener(pickClick);
        findViewById(R.id.card_pick_opt_2).setOnClickListener(pickClick);

        btnNext.setOnClickListener(v -> {
            pickRoundIndex++;
            displayPickRound();
        });
    }

    // -------------------------------------------------------------
    // SCREEN 11: LISTA DE LIVROS
    // -------------------------------------------------------------
    private void setupBooksList() {
        findViewById(R.id.btn_books_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        findViewById(R.id.btn_read_book_balao).setOnClickListener(v -> openBook("book_balao"));
        findViewById(R.id.card_book_balao).setOnClickListener(v -> openBook("book_balao"));

        findViewById(R.id.btn_read_book_abelha).setOnClickListener(v -> openBook("book_abelha"));
        findViewById(R.id.card_book_abelha).setOnClickListener(v -> openBook("book_abelha"));

        View cardPipoca = findViewById(R.id.card_book_pipoca);
        if (cardPipoca != null) cardPipoca.setOnClickListener(v -> openBook("book_pipoca"));
        View btnPipoca = findViewById(R.id.btn_read_book_pipoca);
        if (btnPipoca != null) btnPipoca.setOnClickListener(v -> openBook("book_pipoca"));

        View cardPeixe = findViewById(R.id.card_book_peixe);
        if (cardPeixe != null) cardPeixe.setOnClickListener(v -> openBook("book_peixe"));
        View btnPeixe = findViewById(R.id.btn_read_book_peixe);
        if (btnPeixe != null) btnPeixe.setOnClickListener(v -> openBook("book_peixe"));

        View cardEstrela = findViewById(R.id.card_book_estrela);
        if (cardEstrela != null) cardEstrela.setOnClickListener(v -> openBook("book_estrela"));
        View btnEstrela = findViewById(R.id.btn_read_book_estrela);
        if (btnEstrela != null) btnEstrela.setOnClickListener(v -> openBook("book_estrela"));
    }

    private void openBook(String bookId) {
        List<BookStory> books = repository.getBooksList();
        for (BookStory b : books) {
            if (b.getId().equals(bookId)) {
                currentBook = b;
                currentBookPageIndex = 0;
                navigateTo(SCREEN_BOOK_READER, false);
                displayBookPage();
                return;
            }
        }
    }

    // -------------------------------------------------------------
    // SCREEN 12: LEITURA DO LIVRO (Leitura Dinâmica Sincronizada)
    // -------------------------------------------------------------
    private void setupBookReader() {
        findViewById(R.id.btn_reader_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        MaterialButton btnSpeedToggle = findViewById(R.id.btn_reader_speed_toggle);
        MaterialButton btnSpeedCycle = findViewById(R.id.btn_reader_speed_cycle);
        FloatingActionButton fabPlay = findViewById(R.id.fab_reader_play_pause);
        ImageButton btnReplay = findViewById(R.id.btn_reader_replay);
        MaterialButton btnPrev = findViewById(R.id.btn_reader_prev_page);
        MaterialButton btnNext = findViewById(R.id.btn_reader_next_page);

        View.OnClickListener cycleSpeedListener = v -> {
            float currentRate = currentProfile.getSpeechRate();
            float nextRate;
            if (currentRate < 0.85f) {
                nextRate = 0.95f;
            } else if (currentRate < 0.98f) {
                nextRate = 1.0f;
            } else if (currentRate < 1.2f) {
                nextRate = 1.5f;
            } else if (currentRate < 1.7f) {
                nextRate = 2.0f;
            } else {
                nextRate = 0.75f;
            }
            currentProfile.setSpeechRate(nextRate);
            repository.saveProfile(currentProfile);
            speechHelper.setSpeechRate(nextRate);

            btnSpeedToggle.setText(nextRate + "x");
            Toast.makeText(this, "Velocidade ajustada para: " + nextRate + "x", Toast.LENGTH_SHORT).show();

            // If reading is running, replay at new rate
            if (speechHelper.isSpeaking()) {
                playSynchronizedPageReading();
            }
        };

        btnSpeedToggle.setOnClickListener(cycleSpeedListener);
        btnSpeedCycle.setOnClickListener(cycleSpeedListener);

        fabPlay.setOnClickListener(v -> {
            if (speechHelper.isSpeaking()) {
                speechHelper.stop();
                fabPlay.setImageResource(R.drawable.ic_play);
            } else {
                playSynchronizedPageReading();
            }
        });

        btnReplay.setOnClickListener(v -> playSynchronizedPageReading());

        btnPrev.setOnClickListener(v -> {
            speechHelper.stop();
            if (currentBook != null && currentBookPageIndex > 0) {
                currentBookPageIndex--;
                displayBookPage();
            }
        });

        btnNext.setOnClickListener(v -> {
            speechHelper.stop();
            if (currentBook != null && currentBookPageIndex < currentBook.getPages().size() - 1) {
                currentBookPageIndex++;
                displayBookPage();
            } else if (currentBook != null && currentBookPageIndex == currentBook.getPages().size() - 1) {
                // Book completed!
                Toast.makeText(this, "🎉 Parabéns! Você concluiu a leitura de " + currentBook.getTitle() + "!", Toast.LENGTH_LONG).show();
                currentProfile.setBooksRead(currentProfile.getBooksRead() + 1);
                repository.logActivity("Livro Lido: " + currentBook.getTitle(), 30, "Livros");
                navigateTo(SCREEN_EVOLUTION, false);
            }
        });
    }

    private void displayBookPage() {
        if (currentBook == null) return;
        List<BookPage> pages = currentBook.getPages();
        if (currentBookPageIndex < 0) currentBookPageIndex = 0;
        if (currentBookPageIndex >= pages.size()) currentBookPageIndex = pages.size() - 1;

        BookPage page = pages.get(currentBookPageIndex);

        TextView tvTitle = findViewById(R.id.tv_reader_book_title);
        TextView tvPageIndicator = findViewById(R.id.tv_reader_page_indicator);
        ImageView imgIllustration = findViewById(R.id.img_reader_illustration);
        TextView tvText = findViewById(R.id.tv_reader_synchronized_text);
        MaterialButton btnSpeedToggle = findViewById(R.id.btn_reader_speed_toggle);
        MaterialButton btnPrev = findViewById(R.id.btn_reader_prev_page);
        MaterialButton btnNext = findViewById(R.id.btn_reader_next_page);
        FloatingActionButton fabPlay = findViewById(R.id.fab_reader_play_pause);
        TextView tvStatusHint = findViewById(R.id.tv_reader_status_hint);

        tvTitle.setText(currentBook.getTitle());
        tvPageIndicator.setText("Página " + (currentBookPageIndex + 1) + " de " + pages.size());
        imgIllustration.setImageResource(page.getIllustrationResId());
        tvText.setText(page.getText());

        btnSpeedToggle.setText(currentProfile.getSpeechRate() + "x");
        fabPlay.setImageResource(R.drawable.ic_play);
        tvStatusHint.setText("Velocidade atual: " + currentProfile.getSpeechRate() + "x • Toque em Play");

        btnPrev.setEnabled(currentBookPageIndex > 0);
        if (currentBookPageIndex == pages.size() - 1) {
            btnNext.setText("Concluir Livro ⭐");
        } else {
            btnNext.setText("Próxima Página");
        }
    }

    private void playSynchronizedPageReading() {
        if (currentBook == null) return;
        BookPage page = currentBook.getPages().get(currentBookPageIndex);
        String fullText = page.getText();

        FloatingActionButton fabPlay = findViewById(R.id.fab_reader_play_pause);
        TextView tvText = findViewById(R.id.tv_reader_synchronized_text);
        TextView tvStatusHint = findViewById(R.id.tv_reader_status_hint);

        fabPlay.setImageResource(R.drawable.ic_pause);
        tvStatusHint.setText("Lendo em " + currentProfile.getSpeechRate() + "x com destaque sincronizado...");

        speechHelper.speakSynchronized(fullText, new SpeechHelper.WordHighlightListener() {
            @Override
            public void onWordHighlighted(int wordIndex, String word, int startChar, int endChar) {
                SpannableString spannable = new SpannableString(fullText);
                // Guaranteed high-contrast highlight: lemon yellow background + bold black text
                spannable.setSpan(
                        new BackgroundColorSpan(Color.parseColor("#FEF08A")),
                        startChar,
                        endChar,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannable.setSpan(
                        new ForegroundColorSpan(Color.parseColor("#0F172A")),
                        startChar,
                        endChar,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannable.setSpan(
                        new StyleSpan(Typeface.BOLD),
                        startChar,
                        endChar,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                tvText.setText(spannable);
            }

            @Override
            public void onSpeechCompleted() {
                tvText.setText(fullText);
                fabPlay.setImageResource(R.drawable.ic_play);
                tvStatusHint.setText("Leitura da página concluída.");
            }

            @Override
            public void onSpeechStarted() {
                fabPlay.setImageResource(R.drawable.ic_pause);
            }
        });
    }

    // -------------------------------------------------------------
    // SCREEN 13: MINHA EVOLUÇÃO
    // -------------------------------------------------------------
    private void setupEvolution() {
        findViewById(R.id.btn_evolution_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void updateEvolutionUI() {
        TextView tvPoints = findViewById(R.id.tv_evo_total_points);
        TextView tvLetters = findViewById(R.id.tv_evo_letters_count);
        TextView tvWords = findViewById(R.id.tv_evo_words_count);
        TextView tvBooks = findViewById(R.id.tv_evo_books_count);
        TextView tvActivities = findViewById(R.id.tv_evo_activities_count);

        tvPoints.setText(currentProfile.getPoints() + " Pontos Acumulados");
        tvLetters.setText(currentProfile.getLettersLearned() + " / 26");
        tvWords.setText(String.valueOf(currentProfile.getWordsLearned()));
        tvBooks.setText(String.valueOf(currentProfile.getBooksRead()));
        tvActivities.setText(String.valueOf(currentProfile.getActivitiesCompleted()));
    }

    // -------------------------------------------------------------
    // SCREEN 14: PORTÃO DOS PAIS (Parental Gate)
    // -------------------------------------------------------------
    private void setupParentGate() {
        EditText etAnswer = findViewById(R.id.et_parent_gate_answer);
        TextView tvError = findViewById(R.id.tv_parent_gate_error);
        MaterialButton btnUnlock = findViewById(R.id.btn_parent_gate_unlock);
        Button btnCancel = findViewById(R.id.btn_parent_gate_cancel);
        View btnRefresh = findViewById(R.id.btn_parent_gate_refresh);

        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                if (tvError != null) tvError.setVisibility(View.GONE);
                generateNewParentGateChallenge();
            });
        }

        btnCancel.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        btnUnlock.setOnClickListener(v -> {
            String ansStr = etAnswer != null ? etAnswer.getText().toString().trim() : "";
            if (!ansStr.isEmpty() && ansStr.equals(String.valueOf(gateAnswer))) {
                if (tvError != null) tvError.setVisibility(View.GONE);
                if (etAnswer != null) etAnswer.setText("");
                navigateTo(SCREEN_PARENT_DASHBOARD, false);
            } else {
                if (tvError != null) {
                    tvError.setText("Resposta incorreta! Nova conta gerada para sua segurança.");
                    tvError.setVisibility(View.VISIBLE);
                }
                generateNewParentGateChallenge();
            }
        });

        generateNewParentGateChallenge();
    }

    private void generateNewParentGateChallenge() {
        TextView tvQuestion = findViewById(R.id.tv_parent_gate_question);
        EditText etAnswer = findViewById(R.id.et_parent_gate_answer);
        if (tvQuestion == null) return;
        if (etAnswer != null) etAnswer.setText("");

        Random random = new Random();
        int operationType = random.nextInt(4);

        switch (operationType) {
            case 0: { // 2-digit addition with carry: e.g. 37 + 48
                int a = random.nextInt(35) + 26; // 26 to 60
                int b = random.nextInt(35) + 18; // 18 to 52
                gateAnswer = a + b;
                tvQuestion.setText("Quanto é " + a + " + " + b + " ?");
                break;
            }
            case 1: { // 2-digit subtraction with borrow: e.g. 83 - 46
                int a = random.nextInt(38) + 61; // 61 to 98
                int b = random.nextInt(30) + 19; // 19 to 48
                gateAnswer = a - b;
                tvQuestion.setText("Quanto é " + a + " - " + b + " ?");
                break;
            }
            case 2: { // Simple 2-digit multiplication: e.g. 14 × 6 or 13 × 7
                int a = random.nextInt(6) + 12; // 12 to 17
                int b = random.nextInt(5) + 4;  // 4 to 8
                gateAnswer = a * b;
                tvQuestion.setText("Quanto é " + a + " × " + b + " ?");
                break;
            }
            default: { // Two-step combination: e.g. (8 × 7) + 15
                int a = random.nextInt(4) + 6; // 6 to 9
                int b = random.nextInt(4) + 6; // 6 to 9
                int c = random.nextInt(15) + 12; // 12 to 26
                gateAnswer = (a * b) + c;
                tvQuestion.setText("Quanto é (" + a + " × " + b + ") + " + c + " ?");
                break;
            }
        }
    }

    // -------------------------------------------------------------
    // SCREEN 15: PAINEL DOS RESPONSÁVEIS
    // -------------------------------------------------------------
    private void setupParentDashboard() {
        findViewById(R.id.btn_parent_dash_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Button btnSpeed075 = findViewById(R.id.btn_speed_075x);
        Button btnSpeed095 = findViewById(R.id.btn_speed_095x);
        Button btnSpeed1x = findViewById(R.id.btn_speed_1x);
        Button btnSpeed15x = findViewById(R.id.btn_speed_15x);
        Button btnSpeed2x = findViewById(R.id.btn_speed_2x);
        MaterialButton btnTestSpeed = findViewById(R.id.btn_test_voice_speed);

        View.OnClickListener speedClick = v -> {
            float chosenRate = (v.getId() == R.id.btn_speed_075x) ? 0.75f :
                    (v.getId() == R.id.btn_speed_095x) ? 0.95f :
                            (v.getId() == R.id.btn_speed_1x) ? 1.0f :
                                    (v.getId() == R.id.btn_speed_15x) ? 1.5f : 2.0f;

            currentProfile.setSpeechRate(chosenRate);
            repository.saveProfile(currentProfile);
            speechHelper.setSpeechRate(chosenRate);

            updateParentSpeedButtons(chosenRate);
            Toast.makeText(this, "Velocidade " + chosenRate + "x salva no perfil!", Toast.LENGTH_SHORT).show();
        };

        if (btnSpeed075 != null) btnSpeed075.setOnClickListener(speedClick);
        if (btnSpeed095 != null) btnSpeed095.setOnClickListener(speedClick);
        if (btnSpeed1x != null) btnSpeed1x.setOnClickListener(speedClick);
        if (btnSpeed15x != null) btnSpeed15x.setOnClickListener(speedClick);
        if (btnSpeed2x != null) btnSpeed2x.setOnClickListener(speedClick);

        if (btnTestSpeed != null) {
            btnTestSpeed.setOnClickListener(v -> {
                float rate = currentProfile.getSpeechRate();
                speechHelper.speakSimple("Olá! Esta é a narração na velocidade de " + rate + "x para a leitura das crianças.");
            });
        }

        // Time limits
        Button btnTime15 = findViewById(R.id.btn_time_15m);
        Button btnTime30 = findViewById(R.id.btn_time_30m);
        Button btnTime45 = findViewById(R.id.btn_time_45m);
        Button btnTimeUnl = findViewById(R.id.btn_time_unlimited);

        View.OnClickListener timeClick = v -> {
            int mins = (v.getId() == R.id.btn_time_15m) ? 15 :
                    (v.getId() == R.id.btn_time_30m) ? 30 :
                            (v.getId() == R.id.btn_time_45m) ? 45 : 0;
            currentProfile.setTimeLimitMinutes(mins);
            repository.saveProfile(currentProfile);
            updateTimeButtons(mins);
            Toast.makeText(this, "Limite de tempo atualizado!", Toast.LENGTH_SHORT).show();
        };

        if (btnTime15 != null) btnTime15.setOnClickListener(timeClick);
        if (btnTime30 != null) btnTime30.setOnClickListener(timeClick);
        if (btnTime45 != null) btnTime45.setOnClickListener(timeClick);
        if (btnTimeUnl != null) btnTimeUnl.setOnClickListener(timeClick);

        // Theme Mode Toggle (Modo Claro / Modo Escuro with green background & white text)
        MaterialButton btnThemeMode = findViewById(R.id.btn_toggle_contrast);
        if (btnThemeMode != null) {
            btnThemeMode.setOnClickListener(v -> {
                boolean isDark = currentProfile.isDarkMode();
                boolean newDark = !isDark;
                currentProfile.setDarkMode(newDark);
                repository.saveProfile(currentProfile);

                // Dynamically toggle Day/Night theme
                AppCompatDelegate.setDefaultNightMode(
                        newDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );

                btnThemeMode.setText(newDark ? "Modo Escuro" : "Modo Claro");
                btnThemeMode.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tertiary)));
                btnThemeMode.setTextColor(ContextCompat.getColor(this, R.color.white));
                Toast.makeText(this, "Tema alterado para: " + (newDark ? "Modo Escuro" : "Modo Claro"), Toast.LENGTH_SHORT).show();
            });
        }

        MaterialButton btnSoundFx = findViewById(R.id.btn_toggle_sound_fx);
        if (btnSoundFx != null) {
            btnSoundFx.setOnClickListener(v -> {
                boolean current = currentProfile.isSoundEffects();
                currentProfile.setSoundEffects(!current);
                repository.saveProfile(currentProfile);
                btnSoundFx.setText(!current ? "Ativado" : "Desativado");
                Toast.makeText(this, "Efeitos sonoros: " + (!current ? "Ativado" : "Desativado"), Toast.LENGTH_SHORT).show();
            });
        }

        View btnMakePayment = findViewById(R.id.btn_parent_make_payment);
        if (btnMakePayment != null) {
            btnMakePayment.setOnClickListener(v -> showPaymentDialog());
        }

        View btnOpenContact = findViewById(R.id.btn_parent_open_contact_us);
        if (btnOpenContact != null) {
            btnOpenContact.setOnClickListener(v -> navigateTo(SCREEN_CONTACT_US, false));
        }

        View btnLogout = findViewById(R.id.btn_parent_dash_logout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> showLogoutDialog());
        }

        // Click on person name / profile layout to show logout and switch account
        View parentProfileInfo = findViewById(R.id.layout_parent_profile_info);
        if (parentProfileInfo != null) {
            parentProfileInfo.setOnClickListener(v -> showLogoutDialog());
        }
        View tvParentChildName = findViewById(R.id.tv_parent_child_name);
        if (tvParentChildName != null) {
            tvParentChildName.setOnClickListener(v -> showLogoutDialog());
        }
        View tvParentAccount = findViewById(R.id.tv_parent_account_email);
        if (tvParentAccount != null) {
            tvParentAccount.setOnClickListener(v -> showLogoutDialog());
        }
    }

    private void updateParentDashboardUI() {
        TextView tvChildName = findViewById(R.id.tv_parent_child_name);
        TextView tvAccount = findViewById(R.id.tv_parent_account_email);

        if (tvChildName != null) tvChildName.setText(currentProfile.getChildName() + " (" + currentProfile.getChildAge() + " anos)");
        if (tvAccount != null) tvAccount.setText(currentProfile.getEmail());

        updateParentSpeedButtons(currentProfile.getSpeechRate());
        updateTimeButtons(currentProfile.getTimeLimitMinutes());
        populateHistoryList();

        // Update Theme Button
        MaterialButton btnThemeMode = findViewById(R.id.btn_toggle_contrast);
        if (btnThemeMode != null) {
            btnThemeMode.setText(currentProfile.isDarkMode() ? "Modo Escuro" : "Modo Claro");
            btnThemeMode.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tertiary)));
            btnThemeMode.setTextColor(ContextCompat.getColor(this, R.color.white));
        }

        // Update Subscription status
        TextView tvSubBadge = findViewById(R.id.tv_parent_sub_status_badge);
        TextView tvSubDesc = findViewById(R.id.tv_parent_sub_description);
        MaterialButton btnPay = findViewById(R.id.btn_parent_make_payment);

        if (tvSubBadge != null && tvSubDesc != null) {
            if ("ATIVO".equalsIgnoreCase(currentProfile.getSubscriptionStatus())) {
                tvSubBadge.setText("Assinatura Ativa (R$ 9,99/mês)");
                tvSubBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                tvSubDesc.setText("Sua assinatura mensal está em dia via " + currentProfile.getPaymentMethod() + ". Acesso ilimitado e completo liberado!");
                if (btnPay != null) btnPay.setText("Assinatura em Dia (R$ 9,99/mês)");
            } else {
                tvSubBadge.setText("Período de Teste (30 Dias)");
                tvSubBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                tvSubDesc.setText("Você está no período gratuito de 30 dias (" + currentProfile.getTrialDaysRemaining() + " dias restantes). Após este período, taxa de R$ 9,99/mês.");
                if (btnPay != null) btnPay.setText("Realizar Pagamento (R$ 9,99/mês)");
            }
        }
    }

    private void updateParentSpeedButtons(float rate) {
        Button b075 = findViewById(R.id.btn_speed_075x);
        Button b095 = findViewById(R.id.btn_speed_095x);
        Button b1 = findViewById(R.id.btn_speed_1x);
        Button b15 = findViewById(R.id.btn_speed_15x);
        Button b2 = findViewById(R.id.btn_speed_2x);
        TextView tvStatus = findViewById(R.id.tv_speed_save_status);

        if (b075 != null) {
            b075.setBackground(ContextCompat.getDrawable(this, Math.abs(rate - 0.75f) < 0.05f ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b075.setTextColor(Math.abs(rate - 0.75f) < 0.05f ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b095 != null) {
            b095.setBackground(ContextCompat.getDrawable(this, Math.abs(rate - 0.95f) < 0.05f ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b095.setTextColor(Math.abs(rate - 0.95f) < 0.05f ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b1 != null) {
            b1.setBackground(ContextCompat.getDrawable(this, Math.abs(rate - 1.0f) < 0.05f ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b1.setTextColor(Math.abs(rate - 1.0f) < 0.05f ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b15 != null) {
            b15.setBackground(ContextCompat.getDrawable(this, Math.abs(rate - 1.5f) < 0.05f ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b15.setTextColor(Math.abs(rate - 1.5f) < 0.05f ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b2 != null) {
            b2.setBackground(ContextCompat.getDrawable(this, Math.abs(rate - 2.0f) < 0.05f ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b2.setTextColor(Math.abs(rate - 2.0f) < 0.05f ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }

        if (tvStatus != null) {
            tvStatus.setText("✓ Velocidade " + rate + "x ativa e salva no perfil de " + currentProfile.getChildName());
        }
    }

    private void updateTimeButtons(int mins) {
        Button b15 = findViewById(R.id.btn_time_15m);
        Button b30 = findViewById(R.id.btn_time_30m);
        Button b45 = findViewById(R.id.btn_time_45m);
        Button bUnl = findViewById(R.id.btn_time_unlimited);

        if (b15 != null) {
            b15.setBackground(ContextCompat.getDrawable(this, mins == 15 ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b15.setTextColor(mins == 15 ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b30 != null) {
            b30.setBackground(ContextCompat.getDrawable(this, mins == 30 ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b30.setTextColor(mins == 30 ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (b45 != null) {
            b45.setBackground(ContextCompat.getDrawable(this, mins == 45 ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            b45.setTextColor(mins == 45 ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
        if (bUnl != null) {
            bUnl.setBackground(ContextCompat.getDrawable(this, mins == 0 ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
            bUnl.setTextColor(mins == 0 ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
        }
    }

    private void populateHistoryList() {
        LinearLayout container = findViewById(R.id.container_history_items);
        if (container == null) return;
        container.removeAllViews();

        List<ActivityHistoryItem> history = repository.getHistoryList();
        int count = Math.min(history.size(), 5);

        for (int i = 0; i < count; i++) {
            ActivityHistoryItem item = history.get(i);
            View itemView = getLayoutInflater().inflate(R.layout.item_history, container, false);

            TextView tvTitle = itemView.findViewById(R.id.tv_item_history_title);
            TextView tvDate = itemView.findViewById(R.id.tv_item_history_date);
            TextView tvPoints = itemView.findViewById(R.id.tv_item_history_points);

            tvTitle.setText(item.getTitle());
            tvDate.setText(item.getTimestamp() + " • " + item.getCategory());
            tvPoints.setText("+" + item.getPointsEarned() + " pts");

            container.addView(itemView);
        }
    }

    private void showPaymentDialog() {
        String[] options = {"Pix Instantâneo (R$ 9,99/mês)", "Cartão de Crédito (R$ 9,99/mês)"};
        final int[] selected = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pagamento da Assinatura Mensal");
        builder.setSingleChoiceItems(options, 0, (dialog, which) -> selected[0] = which);
        builder.setPositiveButton("Confirmar Pagamento de R$ 9,99", (dialog, which) -> {
            String method = selected[0] == 0 ? "Pix" : "Cartão de Crédito";
            repository.recordUserPayment(method);
            updateParentDashboardUI();
            Toast.makeText(this, "🎉 Pagamento de R$ 9,99 confirmado via " + method + "! Assinatura ativa.", Toast.LENGTH_LONG).show();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // -------------------------------------------------------------
    // SCREEN 16: FALE CONOSCO (Suporte & Sugestões)
    // -------------------------------------------------------------
    private void setupContactUs() {
        View btnBack = findViewById(R.id.btn_contact_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Button btnSugestao = findViewById(R.id.btn_category_sugestao);
        Button btnCadastro = findViewById(R.id.btn_category_cadastro);
        Button btnDuvida = findViewById(R.id.btn_category_duvida);
        Button btnFinanceiro = findViewById(R.id.btn_category_financeiro);
        Button btnOutro = findViewById(R.id.btn_category_outro);

        Button[] catButtons = {btnSugestao, btnCadastro, btnDuvida, btnFinanceiro, btnOutro};
        String[] catNames = {"Sugestão", "Cadastro", "Dúvidas", "Pagamento", "Outro"};

        View.OnClickListener catClick = v -> {
            for (int i = 0; i < catButtons.length; i++) {
                if (catButtons[i] != null) {
                    boolean isSelected = catButtons[i].getId() == v.getId();
                    catButtons[i].setBackground(ContextCompat.getDrawable(this, isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
                    catButtons[i].setTextColor(isSelected ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
                    if (isSelected) {
                        selectedContactCategory = catNames[i];
                    }
                }
            }
        };

        for (Button b : catButtons) {
            if (b != null) b.setOnClickListener(catClick);
        }

        EditText etSubject = findViewById(R.id.et_contact_subject);
        EditText etMessage = findViewById(R.id.et_contact_message);
        View btnSend = findViewById(R.id.btn_contact_send);

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> {
                String subject = etSubject != null ? etSubject.getText().toString().trim() : "";
                String message = etMessage != null ? etMessage.getText().toString().trim() : "";

                if (subject.isEmpty()) {
                    Toast.makeText(this, "Por favor, informe o assunto da mensagem.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (message.isEmpty()) {
                    Toast.makeText(this, "Por favor, digite sua mensagem para a equipe.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FeedbackMessage newMsg = new FeedbackMessage(
                        "fb_" + System.currentTimeMillis(),
                        currentProfile.getEmail(),
                        selectedContactCategory,
                        subject,
                        message,
                        "Hoje",
                        "Pendente",
                        null,
                        null
                );

                repository.addFeedbackMessage(newMsg);
                if (etSubject != null) etSubject.setText("");
                if (etMessage != null) etMessage.setText("");

                Toast.makeText(this, "Mensagem enviada com sucesso! Nossa equipe responderá em breve.", Toast.LENGTH_LONG).show();
                updateContactUsUI();
            });
        }
    }

    private void updateContactUsUI() {
        LinearLayout container = findViewById(R.id.container_contact_history);
        if (container == null) return;
        container.removeAllViews();

        List<FeedbackMessage> messages = repository.getFeedbackMessages();
        for (FeedbackMessage msg : messages) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card_rounded));
            card.setPadding(24, 24, 24, 24);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 20);
            card.setLayoutParams(lp);

            TextView tvCategory = new TextView(this);
            tvCategory.setText("📁 " + msg.getCategory() + " • " + msg.getTimestamp());
            tvCategory.setTextSize(12);
            tvCategory.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tvCategory.setTypeface(null, Typeface.BOLD);
            card.addView(tvCategory);

            TextView tvSubject = new TextView(this);
            tvSubject.setText(msg.getSubject());
            tvSubject.setTextSize(16);
            tvSubject.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvSubject.setTypeface(null, Typeface.BOLD);
            tvSubject.setPadding(0, 8, 0, 4);
            card.addView(tvSubject);

            TextView tvBody = new TextView(this);
            tvBody.setText(msg.getMessage());
            tvBody.setTextSize(14);
            tvBody.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            card.addView(tvBody);

            if (msg.getReply() != null) {
                LinearLayout replyBox = new LinearLayout(this);
                replyBox.setOrientation(LinearLayout.VERTICAL);
                replyBox.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
                replyBox.setPadding(16, 16, 16, 16);
                LinearLayout.LayoutParams replyLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                replyLp.setMargins(0, 16, 0, 0);
                replyBox.setLayoutParams(replyLp);

                TextView tvReplyTitle = new TextView(this);
                tvReplyTitle.setText("💬 Resposta da Equipe (" + msg.getReplyTimestamp() + "):");
                tvReplyTitle.setTextSize(12);
                tvReplyTitle.setTextColor(ContextCompat.getColor(this, R.color.tertiary));
                tvReplyTitle.setTypeface(null, Typeface.BOLD);
                replyBox.addView(tvReplyTitle);

                TextView tvReplyContent = new TextView(this);
                tvReplyContent.setText(msg.getReply());
                tvReplyContent.setTextSize(13);
                tvReplyContent.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                tvReplyContent.setPadding(0, 4, 0, 0);
                replyBox.addView(tvReplyContent);

                card.addView(replyBox);
            } else {
                TextView tvPending = new TextView(this);
                tvPending.setText("⏳ Status: Aguardando resposta da equipe pedagógica");
                tvPending.setTextSize(12);
                tvPending.setTextColor(ContextCompat.getColor(this, R.color.accent_amber));
                tvPending.setPadding(0, 12, 0, 0);
                card.addView(tvPending);
            }

            container.addView(card);
        }
    }

    // -------------------------------------------------------------
    // SCREEN 17: PORTAL ADMINISTRATIVO (Admin Dashboard)
    // -------------------------------------------------------------
    private void setupAdminDashboard() {
        View btnBack = findViewById(R.id.btn_admin_back);
        if (btnBack != null) btnBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Button btnAll = findViewById(R.id.btn_filter_fin_all);
        Button btnActive = findViewById(R.id.btn_filter_fin_active);
        Button btnPending = findViewById(R.id.btn_filter_fin_pending);
        Button btnCancelled = findViewById(R.id.btn_filter_fin_cancelled);

        Button[] filterButtons = {btnAll, btnActive, btnPending, btnCancelled};
        String[] filterKeys = {"TODOS", "ATIVO", "PENDENTE", "CANCELADO"};

        View.OnClickListener filterClick = v -> {
            for (int i = 0; i < filterButtons.length; i++) {
                if (filterButtons[i] != null) {
                    boolean isSelected = filterButtons[i].getId() == v.getId();
                    filterButtons[i].setBackground(ContextCompat.getDrawable(this, isSelected ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected));
                    filterButtons[i].setTextColor(isSelected ? Color.WHITE : ContextCompat.getColor(this, R.color.text_secondary));
                    if (isSelected) {
                        currentFinancialFilter = filterKeys[i];
                    }
                }
            }
            renderAdminFinancialList();
        };

        for (Button b : filterButtons) {
            if (b != null) b.setOnClickListener(filterClick);
        }
    }

    private void updateAdminDashboardUI() {
        TextView tvTotal = findViewById(R.id.tv_admin_total_users);
        if (tvTotal != null) tvTotal.setText(String.valueOf(repository.getTotalUsersCount()));

        renderAdminFinancialList();
        renderAdminFeedbackList();
    }

    private void renderAdminFinancialList() {
        LinearLayout container = findViewById(R.id.container_admin_payments);
        if (container == null) return;
        container.removeAllViews();

        List<PaymentRecord> records = repository.getPaymentRecords();
        for (PaymentRecord record : records) {
            if (!"TODOS".equals(currentFinancialFilter) && !record.getStatus().equalsIgnoreCase(currentFinancialFilter)) {
                continue;
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card_rounded));
            card.setPadding(20, 20, 20, 20);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvName = new TextView(this);
            tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvName.setText(record.getParentName() + " (" + record.getChildAge() + " anos)");
            tvName.setTextSize(15);
            tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvName.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvName);

            TextView tvBadge = new TextView(this);
            tvBadge.setText(record.getStatus());
            tvBadge.setTextSize(11);
            tvBadge.setTypeface(null, Typeface.BOLD);
            tvBadge.setPadding(16, 6, 16, 6);
            if ("ATIVO".equalsIgnoreCase(record.getStatus())) {
                tvBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                tvBadge.setTextColor(Color.WHITE);
            } else if ("PENDENTE".equalsIgnoreCase(record.getStatus())) {
                tvBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            } else {
                tvBadge.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_unselected));
                tvBadge.setTextColor(ContextCompat.getColor(this, R.color.error));
            }
            topRow.addView(tvBadge);
            card.addView(topRow);

            TextView tvDetails = new TextView(this);
            tvDetails.setText("E-mail: " + record.getUserEmail() + " • Data: " + record.getDate());
            tvDetails.setTextSize(12);
            tvDetails.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tvDetails.setPadding(0, 6, 0, 2);
            card.addView(tvDetails);

            TextView tvAmount = new TextView(this);
            tvAmount.setText("Valor Mensal: R$ " + String.format(Locale.getDefault(), "%.2f", record.getAmount()) + " via " + record.getMethod());
            tvAmount.setTextSize(13);
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tvAmount.setTypeface(null, Typeface.BOLD);
            card.addView(tvAmount);

            container.addView(card);
        }
    }

    private void renderAdminFeedbackList() {
        LinearLayout container = findViewById(R.id.container_admin_feedback);
        if (container == null) return;
        container.removeAllViews();

        List<FeedbackMessage> messages = repository.getFeedbackMessages();
        for (FeedbackMessage msg : messages) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_card_rounded));
            card.setPadding(20, 20, 20, 20);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(0, 0, 0, 16);
            card.setLayoutParams(lp);

            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvCategory = new TextView(this);
            tvCategory.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvCategory.setText("📩 " + msg.getCategory() + " • " + msg.getUserEmail());
            tvCategory.setTextSize(12);
            tvCategory.setTextColor(ContextCompat.getColor(this, R.color.primary));
            tvCategory.setTypeface(null, Typeface.BOLD);
            topRow.addView(tvCategory);

            TextView tvStatus = new TextView(this);
            tvStatus.setText(msg.getStatus());
            tvStatus.setTextSize(11);
            tvStatus.setTypeface(null, Typeface.BOLD);
            tvStatus.setPadding(12, 4, 12, 4);
            if ("Respondida".equalsIgnoreCase(msg.getStatus())) {
                tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_chip_selected));
                tvStatus.setTextColor(Color.WHITE);
            } else {
                tvStatus.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_letter_badge));
                tvStatus.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
            topRow.addView(tvStatus);
            card.addView(topRow);

            TextView tvSubject = new TextView(this);
            tvSubject.setText(msg.getSubject());
            tvSubject.setTextSize(15);
            tvSubject.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            tvSubject.setTypeface(null, Typeface.BOLD);
            tvSubject.setPadding(0, 8, 0, 2);
            card.addView(tvSubject);

            TextView tvBody = new TextView(this);
            tvBody.setText(msg.getMessage());
            tvBody.setTextSize(13);
            tvBody.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            card.addView(tvBody);

            if (msg.getReply() != null) {
                TextView tvReply = new TextView(this);
                tvReply.setText("✓ Resposta enviada pelo Admin (" + msg.getReplyTimestamp() + "):\n\"" + msg.getReply() + "\"");
                tvReply.setTextSize(12);
                tvReply.setTextColor(ContextCompat.getColor(this, R.color.tertiary));
                tvReply.setPadding(0, 8, 0, 0);
                card.addView(tvReply);
            } else {
                MaterialButton btnReply = new MaterialButton(this);
                btnReply.setText("Responder Mensagem");
                btnReply.setTextSize(12);
                btnReply.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                btnReply.setTextColor(Color.WHITE);
                btnReply.setCornerRadius(24);
                LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                btnLp.setMargins(0, 12, 0, 0);
                btnReply.setLayoutParams(btnLp);

                btnReply.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Responder a " + msg.getUserEmail());
                    builder.setMessage("Assunto: " + msg.getSubject() + "\n\nDigite a resposta oficial da equipe pedagógica:");

                    final EditText inputReply = new EditText(this);
                    inputReply.setHint("Digite aqui a resposta para o responsável...");
                    inputReply.setMinLines(3);
                    builder.setView(inputReply);

                    builder.setPositiveButton("Enviar Resposta", (dialog, which) -> {
                        String reply = inputReply.getText().toString().trim();
                        if (!reply.isEmpty()) {
                            repository.replyToFeedbackMessage(msg.getId(), reply);
                            Toast.makeText(this, "Resposta enviada com sucesso!", Toast.LENGTH_SHORT).show();
                            renderAdminFeedbackList();
                        } else {
                            Toast.makeText(this, "A resposta não pode ficar em branco.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("Cancelar", null);
                    builder.show();
                });

                card.addView(btnReply);
            }

            container.addView(card);
        }
    }
}
