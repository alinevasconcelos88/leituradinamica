package com.example.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.R;
import com.example.model.ActivityHistoryItem;
import com.example.model.BookPage;
import com.example.model.BookStory;
import com.example.model.FeedbackMessage;
import com.example.model.LetterItem;
import com.example.model.PaymentRecord;
import com.example.model.SentenceItem;
import com.example.model.UserProfile;
import com.example.model.WordItem;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AppRepository {
    private static final String PREFS_NAME = "leitura_dinamica_prefs";
    private static AppRepository instance;

    private final SharedPreferences prefs;
    private UserProfile currentProfile;
    private final List<ActivityHistoryItem> historyList = new ArrayList<>();

    private final List<LetterItem> alphabetList = new ArrayList<>();
    private final List<WordItem> wordsList = new ArrayList<>();
    private final List<SentenceItem> sentencesList = new ArrayList<>();
    private final List<BookStory> booksList = new ArrayList<>();
    private final List<FeedbackMessage> feedbackList = new ArrayList<>();
    private final List<PaymentRecord> paymentRecordsList = new ArrayList<>();

    private AppRepository(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadProfile();
        initCurriculumData();
        initHistoryData();
        initFeedbackAndPaymentData();
    }

    public static synchronized AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context);
        }
        return instance;
    }

    public UserProfile getCurrentProfile() {
        return currentProfile;
    }

    public void saveProfile(UserProfile profile) {
        this.currentProfile = profile;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", profile.getEmail());
        editor.putString("child_name", profile.getChildName());
        editor.putInt("child_age", profile.getChildAge());
        editor.putInt("points", profile.getPoints());
        editor.putInt("stars", profile.getStars());
        editor.putInt("letters_learned", profile.getLettersLearned());
        editor.putInt("words_learned", profile.getWordsLearned());
        editor.putInt("books_read", profile.getBooksRead());
        editor.putInt("activities_completed", profile.getActivitiesCompleted());
        editor.putInt("total_time_minutes", profile.getTotalTimeSpentMinutes());
        editor.putFloat("speech_rate", profile.getSpeechRate());
        editor.putInt("time_limit", profile.getTimeLimitMinutes());
        editor.putBoolean("high_contrast", profile.isHighContrast());
        editor.putBoolean("sound_effects", profile.isSoundEffects());
        editor.putBoolean("onboarding_done", profile.isOnboardingCompleted());
        editor.putString("subscription_status", profile.getSubscriptionStatus());
        editor.putInt("trial_days_remaining", profile.getTrialDaysRemaining());
        editor.putString("payment_method", profile.getPaymentMethod());
        editor.putBoolean("is_dark_mode", profile.isDarkMode());
        editor.apply();
    }

    private void loadProfile() {
        currentProfile = new UserProfile();
        if (prefs.contains("child_name")) {
            currentProfile.setEmail(prefs.getString("email", "familia@leituradinamica.com"));
            currentProfile.setChildName(prefs.getString("child_name", "Davi"));
            currentProfile.setChildAge(prefs.getInt("child_age", 6));
            currentProfile.setPoints(prefs.getInt("points", 150));
            currentProfile.setStars(prefs.getInt("stars", 18));
            currentProfile.setLettersLearned(prefs.getInt("letters_learned", 16));
            currentProfile.setWordsLearned(prefs.getInt("words_learned", 24));
            currentProfile.setBooksRead(prefs.getInt("books_read", 3));
            currentProfile.setActivitiesCompleted(prefs.getInt("activities_completed", 35));
            currentProfile.setTotalTimeSpentMinutes(prefs.getInt("total_time_minutes", 48));
            currentProfile.setSpeechRate(prefs.getFloat("speech_rate", 1.0f));
            currentProfile.setTimeLimitMinutes(prefs.getInt("time_limit", 30));
            currentProfile.setHighContrast(prefs.getBoolean("high_contrast", false));
            currentProfile.setSoundEffects(prefs.getBoolean("sound_effects", true));
            currentProfile.setOnboardingCompleted(prefs.getBoolean("onboarding_done", false));
            currentProfile.setSubscriptionStatus(prefs.getString("subscription_status", "TRIAL"));
            currentProfile.setTrialDaysRemaining(prefs.getInt("trial_days_remaining", 28));
            currentProfile.setPaymentMethod(prefs.getString("payment_method", "Pix"));
            currentProfile.setDarkMode(prefs.getBoolean("is_dark_mode", false));
        }
    }

    // Authentication methods
    public boolean registerUser(String email, String password) {
        if (email == null || !email.contains("@") || password == null || password.length() < 6) {
            return false;
        }
        String hashedPassword = hashPassword(password);
        prefs.edit()
                .putString("user_pwd_" + email.toLowerCase().trim(), hashedPassword)
                .putString("logged_user", email.toLowerCase().trim())
                .apply();
        currentProfile.setEmail(email.toLowerCase().trim());
        saveProfile(currentProfile);
        return true;
    }

    public boolean loginUser(String email, String password) {
        if (email == null || password == null) return false;
        String cleanEmail = email.toLowerCase().trim();
        String storedHash = prefs.getString("user_pwd_" + cleanEmail, null);
        String enteredHash = hashPassword(password);

        // If user not previously registered, register demo access or check hash
        if (storedHash == null) {
            // First time demo login allowed for ease of test
            prefs.edit().putString("user_pwd_" + cleanEmail, enteredHash).apply();
            prefs.edit().putString("logged_user", cleanEmail).apply();
            currentProfile.setEmail(cleanEmail);
            saveProfile(currentProfile);
            return true;
        }

        if (storedHash.equals(enteredHash)) {
            prefs.edit().putString("logged_user", cleanEmail).apply();
            currentProfile.setEmail(cleanEmail);
            saveProfile(currentProfile);
            return true;
        }
        return false;
    }

    public boolean loginWithGoogle(String googleEmail, String displayName) {
        String cleanEmail = googleEmail.toLowerCase().trim();
        prefs.edit().putString("logged_user", cleanEmail).apply();
        currentProfile.setEmail(cleanEmail);
        if (displayName != null && !displayName.isEmpty()) {
            currentProfile.setChildName(displayName);
        }
        saveProfile(currentProfile);
        return true;
    }

    public boolean resetPassword(String email, String newPassword) {
        if (email == null || newPassword == null || newPassword.length() < 6) return false;
        String cleanEmail = email.toLowerCase().trim();
        String hash = hashPassword(newPassword);
        prefs.edit().putString("user_pwd_" + cleanEmail, hash).apply();
        return true;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(password.hashCode());
        }
    }

    public void logActivity(String title, int points, String category) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        String time = sdf.format(new Date());
        historyList.add(0, new ActivityHistoryItem(title, time, points, category));
        currentProfile.addPoints(points);
        currentProfile.incrementActivities();
        saveProfile(currentProfile);
    }

    public List<ActivityHistoryItem> getHistoryList() {
        return historyList;
    }

    public List<LetterItem> getAlphabetList() {
        return alphabetList;
    }

    public List<WordItem> getWordsList() {
        return wordsList;
    }

    public List<SentenceItem> getSentencesList() {
        return sentencesList;
    }

    public List<BookStory> getBooksList() {
        return booksList;
    }

    private void initHistoryData() {
        historyList.add(new ActivityHistoryItem("Leitura: O Balão Viajante", "Hoje 09:15", 30, "Livros"));
        historyList.add(new ActivityHistoryItem("Alfabeto: Letras A, B, C", "Ontem 16:40", 25, "Alfabeto"));
        historyList.add(new ActivityHistoryItem("Jogo: Complete a Palavra", "Ontem 15:20", 20, "Jogos"));
        historyList.add(new ActivityHistoryItem("Palavras: Animais e Cores", "07/10 10:10", 15, "Palavras"));
    }

    private void initCurriculumData() {
        // Alphabet A-Z with multiple educational words per letter
        alphabetList.add(new LetterItem("A", "Letra A", Arrays.asList("Avião", "Abelha", "Árvore", "Abacaxi", "Anel", "Amigo", "Amor", "Azul"), R.drawable.ic_word_aviao, "Som aberto: /a/"));
        alphabetList.add(new LetterItem("B", "Letra B", Arrays.asList("Bola", "Boneca", "Barco", "Borboleta", "Balão", "Banana", "Bebê", "Biscoito"), R.drawable.ic_word_bola, "Som labial: /b/"));
        alphabetList.add(new LetterItem("C", "Letra C", Arrays.asList("Casa", "Carro", "Coelho", "Coração", "Cavalo", "Caneta", "Cama", "Cão"), R.drawable.ic_word_casa, "Som oclusivo: /k/"));
        alphabetList.add(new LetterItem("D", "Letra D", Arrays.asList("Dado", "Dente", "Dedo", "Dinossauro", "Doce", "Dia", "Dragão", "Dança"), R.drawable.ic_word_dado, "Som dental: /d/"));
        alphabetList.add(new LetterItem("E", "Letra E", Arrays.asList("Estrela", "Elefante", "Escada", "Escola", "Espelho", "Esquilo", "Escova", "Espada"), R.drawable.ic_word_estrela, "Som vogal: /e/"));
        alphabetList.add(new LetterItem("F", "Letra F", Arrays.asList("Flor", "Fada", "Fogo", "Folha", "Formiga", "Foca", "Foguete", "Festa"), R.drawable.ic_word_flor, "Som fricativo: /f/"));
        alphabetList.add(new LetterItem("G", "Letra G", Arrays.asList("Gato", "Girafa", "Galinha", "Golfinho", "Gorila", "Goiaba", "Gelo", "Garfo"), R.drawable.ic_word_gato, "Som velar: /g/"));
        alphabetList.add(new LetterItem("H", "Letra H", Arrays.asList("Helicóptero", "Hipopótamo", "Horta", "Harpa", "Herói", "Homem", "Hiena", "Hotel"), R.drawable.ic_alphabet, "Letra muda em início"));
        alphabetList.add(new LetterItem("I", "Letra I", Arrays.asList("Ilha", "Igreja", "Iglu", "Índio", "Ioiô", "Iguana", "Imã", "Inverno"), R.drawable.ic_word_sol, "Som vogal: /i/"));
        alphabetList.add(new LetterItem("J", "Letra J", Arrays.asList("Janela", "Jacaré", "Joaninha", "Jardim", "Jipe", "Jabuti", "Jornal", "Juiz"), R.drawable.ic_word_casa, "Som palatal: /ʒ/"));
        alphabetList.add(new LetterItem("K", "Letra K", Arrays.asList("Kiwi", "Kart", "Karate", "Koala", "Ketchup", "Kung-fu", "Kombi"), R.drawable.ic_word_maca, "Som consonantal: /k/"));
        alphabetList.add(new LetterItem("L", "Letra L", Arrays.asList("Livro", "Leão", "Lua", "Lápis", "Laranja", "Laço", "Lobo", "Limão"), R.drawable.ic_book, "Som alveolar: /l/"));
        alphabetList.add(new LetterItem("M", "Letra M", Arrays.asList("Maçã", "Macaco", "Melancia", "Menino", "Mala", "Mão", "Morango", "Milho"), R.drawable.ic_word_maca, "Som nasal: /m/"));
        alphabetList.add(new LetterItem("N", "Letra N", Arrays.asList("Nuvem", "Navio", "Ninho", "Neve", "Nariz", "Noite", "Natação", "Nectarina"), R.drawable.ic_word_sol, "Som nasal: /n/"));
        alphabetList.add(new LetterItem("O", "Letra O", Arrays.asList("Olho", "Ovelha", "Ovo", "Onça", "Orelha", "Óculos", "Ouro", "Ônibus"), R.drawable.ic_word_gato, "Som vogal: /o/"));
        alphabetList.add(new LetterItem("P", "Letra P", Arrays.asList("Peixe", "Pato", "Pipa", "Pão", "Pipoca", "Palhaço", "Picolé", "Porta"), R.drawable.ic_word_peixe, "Som bilabial: /p/"));
        alphabetList.add(new LetterItem("Q", "Letra Q", Arrays.asList("Queijo", "Quati", "Quadro", "Quebra-cabeça", "Quarto", "Quimono", "Quinze"), R.drawable.ic_word_sol, "Som gutural: /k/"));
        alphabetList.add(new LetterItem("R", "Letra R", Arrays.asList("Rua", "Rato", "Robô", "Rio", "Rosa", "Roda", "Relógio", "Raposa"), R.drawable.ic_word_casa, "Som vibrante: /ʁ/"));
        alphabetList.add(new LetterItem("S", "Letra S", Arrays.asList("Sol", "Sapo", "Sorvete", "Sino", "Semente", "Sapato", "Sereia", "Suco"), R.drawable.ic_word_sol, "Som sibilante: /s/"));
        alphabetList.add(new LetterItem("T", "Letra T", Arrays.asList("Tatu", "Tartaruga", "Trem", "Tubarão", "Tomate", "Tigre", "Telefone", "Tambor"), R.drawable.ic_word_urso, "Som oclusivo: /t/"));
        alphabetList.add(new LetterItem("U", "Letra U", Arrays.asList("Urso", "Uva", "Unicórnio", "Unha", "Urubu", "Universo", "Urutu"), R.drawable.ic_word_urso, "Som vogal: /u/"));
        alphabetList.add(new LetterItem("V", "Letra V", Arrays.asList("Vela", "Vaca", "Violão", "Vento", "Vaso", "Vulcão", "Vestido", "Vagalume"), R.drawable.ic_word_aviao, "Som labiodental: /v/"));
        alphabetList.add(new LetterItem("W", "Letra W", Arrays.asList("Waffle", "Walkie-talkie", "Web", "Wi-Fi", "Windsurf", "Webcam"), R.drawable.ic_alphabet, "Origem estrangeira"));
        alphabetList.add(new LetterItem("X", "Letra X", Arrays.asList("Xícara", "Xilofone", "Xadrez", "Xale", "Xerife", "Xarope"), R.drawable.ic_word_dado, "Som fricativo: /ʃ/"));
        alphabetList.add(new LetterItem("Y", "Letra Y", Arrays.asList("Yoga", "Yakisoba", "Youtuber", "Yuri", "Ypê", "Yakult"), R.drawable.ic_alphabet, "Vogal estrangeira"));
        alphabetList.add(new LetterItem("Z", "Letra Z", Arrays.asList("Zebra", "Zíper", "Zoológico", "Zero", "Zangão", "Zepelim"), R.drawable.ic_word_abelha, "Som sonoro: /z/"));

        // Words with strictly verified Palavra -> Imagem -> Áudio
        wordsList.add(new WordItem("AVIÃO", "A - VI - ÃO", "Transportes", R.drawable.ic_word_aviao, "O avião voa alto pelo céu azul."));
        wordsList.add(new WordItem("ABELHA", "A - BE - LHA", "Animais", R.drawable.ic_word_abelha, "A abelhinha faz mel bem docinho."));
        wordsList.add(new WordItem("BOLA", "BO - LA", "Brinquedos", R.drawable.ic_word_bola, "A bola redonda pula no chão."));
        wordsList.add(new WordItem("CASA", "CA - SA", "Lugares", R.drawable.ic_word_casa, "A casa acolhedora tem janela aberta."));
        wordsList.add(new WordItem("DADO", "DA - DO", "Jogos", R.drawable.ic_word_dado, "O dado colorido tem muitos pontos."));
        wordsList.add(new WordItem("FLOR", "FLOR", "Natureza", R.drawable.ic_word_flor, "A flor colorida perfuma o jardim."));
        wordsList.add(new WordItem("GATO", "GA - TO", "Animais", R.drawable.ic_word_gato, "O gato morde o novelo de lã."));
        wordsList.add(new WordItem("SOL", "SOL", "Natureza", R.drawable.ic_word_sol, "O sol brilha quentinho pela manhã."));
        wordsList.add(new WordItem("MAÇÃ", "MA - ÇÃ", "Frutas", R.drawable.ic_word_maca, "A maçã vermelha é muito saborosa."));
        wordsList.add(new WordItem("PEIXE", "PEI - XE", "Animais", R.drawable.ic_word_peixe, "O peixe nada veloz na água limpa."));
        wordsList.add(new WordItem("URSO", "UR - SO", "Animais", R.drawable.ic_word_urso, "O urso fofinho dorme na floresta."));
        wordsList.add(new WordItem("ESTRELA", "ES - TRE - LA", "Natureza", R.drawable.ic_word_estrela, "A estrela brilha na noite estrelada."));

        // Sentences
        sentencesList.add(new SentenceItem("s1", "O gato brinca no jardim com a bola.", R.drawable.ic_word_gato, "Animais"));
        sentencesList.add(new SentenceItem("s2", "O sol brilha e ilumina todas as flores.", R.drawable.ic_word_sol, "Natureza"));
        sentencesList.add(new SentenceItem("s3", "A abelha voa até a flor colorida.", R.drawable.ic_word_abelha, "Natureza"));
        sentencesList.add(new SentenceItem("s4", "O ursinho alegre come maçã vermelha.", R.drawable.ic_word_urso, "Alimentação"));
        sentencesList.add(new SentenceItem("s5", "O avião viaja rápido pelo céu bonito.", R.drawable.ic_word_aviao, "Aventura"));

        // Books with pages for synchronized reading
        List<BookPage> balaoPages = new ArrayList<>();
        balaoPages.add(new BookPage(1, "Era uma vez um balão colorido que adorava viajar pelo céu.", R.drawable.img_book_story));
        balaoPages.add(new BookPage(2, "Lá do alto, o balão via as montanhas verdes e os rios de água cristalina.", R.drawable.ic_word_sol));
        balaoPages.add(new BookPage(3, "Uma linda abelha e um pássaro vieram dar as boas-vindas ao balão.", R.drawable.ic_word_abelha));
        balaoPages.add(new BookPage(4, "Quando a tarde chegou, as estrelas começaram a brilhar e o balão pousou feliz.", R.drawable.ic_word_estrela));

        booksList.add(new BookStory(
                "book_balao",
                "O Balão Viajante",
                "Uma linda viagem pelas nuvens, conhecendo a natureza e novos amigos.",
                R.drawable.img_book_story,
                balaoPages
        ));

        List<BookPage> abelhaPages = new ArrayList<>();
        abelhaPages.add(new BookPage(1, "A pequena abelhinha Mel acordou cedo com os primeiros raios de sol.", R.drawable.ic_word_sol));
        abelhaPages.add(new BookPage(2, "Ela voou contente até encontrar uma linda flor vermelha no jardim.", R.drawable.ic_word_flor));
        abelhaPages.add(new BookPage(3, "Com amor e paciência, ela preparou o mel mais doce de toda a floresta.", R.drawable.ic_word_abelha));

        booksList.add(new BookStory(
                "book_abelha",
                "A Abelhinha e a Flor",
                "A história da abelhinha Mel e sua importante missão na natureza.",
                R.drawable.ic_word_abelha,
                abelhaPages
        ));

        // Book 3: O Cãozinho Pipoca
        List<BookPage> pipocaPages = new ArrayList<>();
        pipocaPages.add(new BookPage(1, "Pipoca é um cãozinho muito alegre que adora correr pelo quintal.", R.drawable.ic_word_gato));
        pipocaPages.add(new BookPage(2, "Ele encontrou uma bola vermelha brilhante no meio da grama verdinha.", R.drawable.ic_word_bola));
        pipocaPages.add(new BookPage(3, "Pipoca deu um salto bem alto e pegou a bola com a patinha.", R.drawable.ic_word_sol));
        pipocaPages.add(new BookPage(4, "No fim do dia, ele bebeu água fresca e dormiu na sua linda casinha.", R.drawable.ic_word_casa));

        booksList.add(new BookStory(
                "book_pipoca",
                "O Cãozinho Pipoca",
                "As travessuras do cachorrinho Pipoca e sua grande aventura no parque.",
                R.drawable.ic_word_gato,
                pipocaPages
        ));

        // Book 4: O Peixinho Dourado
        List<BookPage> peixePages = new ArrayList<>();
        peixePages.add(new BookPage(1, "No fundo do mar azul e cristalino vivia um peixinho de escamas douradas.", R.drawable.ic_word_peixe));
        peixePages.add(new BookPage(2, "Ele nadava ligeiro entre os corais coloridos e conchas brilhantes.", R.drawable.ic_word_peixe));
        peixePages.add(new BookPage(3, "O peixinho fez amizade com uma tartaruga marinha muito simpática.", R.drawable.ic_word_estrela));
        peixePages.add(new BookPage(4, "Juntos eles contemplavam os raios de sol que iluminavam as águas calmas.", R.drawable.ic_word_sol));

        booksList.add(new BookStory(
                "book_peixe",
                "O Peixinho Dourado",
                "Uma jornada mágica pelas águas do oceano com amizade e descobertas.",
                R.drawable.ic_word_peixe,
                peixePages
        ));

        // Book 5: A Estrelinha Curiosa
        List<BookPage> estrelaPages = new ArrayList<>();
        estrelaPages.add(new BookPage(1, "Lá no alto do céu da noite brilhava Sofia, uma estrelinha muito curiosa.", R.drawable.ic_word_estrela));
        estrelaPages.add(new BookPage(2, "Ela olhava as luzinhas das cidades e as casas onde as crianças dormiam.", R.drawable.ic_word_casa));
        estrelaPages.add(new BookPage(3, "Sofia piscava bem forte para desejar bons sonhos a todos os pequenos.", R.drawable.ic_word_estrela));
        estrelaPages.add(new BookPage(4, "Quando o sol começava a raiar, ela descansava feliz nas nuvens macias.", R.drawable.ic_word_sol));

        booksList.add(new BookStory(
                "book_estrela",
                "A Estrelinha Curiosa",
                "A linda história da estrelinha que iluminava os sonhos das crianças.",
                R.drawable.ic_word_estrela,
                estrelaPages
        ));
    }

    private void initFeedbackAndPaymentData() {
        // Sample feedback messages for admin
        feedbackList.add(new FeedbackMessage(
                "fb_1",
                "familia.souza@email.com",
                "Sugestão de Conteúdo",
                "Adicionar mais histórias de dinossauros",
                "Meu filho de 5 anos amou o app! Seria incrível se pudessem adicionar histórias e palavras sobre dinossauros.",
                "24/10 10:15",
                "Respondida",
                "Olá, Família Souza! Que alegria saber! Já estamos preparando um módulo especial sobre animais pré-históricos para a próxima atualização. Obrigado pelo carinho!",
                "24/10 14:30"
        ));

        feedbackList.add(new FeedbackMessage(
                "fb_2",
                "mariana.silva@email.com",
                "Cadastro",
                "Dúvida sobre cadastro de segundo filho",
                "Gostaria de saber se a assinatura mensal de R$ 9,99 cobre mais de um perfil de criança no mesmo celular.",
                "25/10 09:40",
                "Pendente",
                null,
                null
        ));

        feedbackList.add(new FeedbackMessage(
                "fb_3",
                "carlos.eduardo@email.com",
                "Dúvidas Gerais",
                "Velocidade da voz para fonoaudiologia",
                "A fonoaudióloga recomendou usar a velocidade 0.75x e funcionou perfeitamente para o aprendizado do Pedro!",
                "26/10 16:20",
                "Pendente",
                null,
                null
        ));

        // Financial & Subscription records for Admin dashboard
        paymentRecordsList.add(new PaymentRecord("pay_1", "familia.souza@email.com", "Roberto Souza", 5, 9.99, "ATIVO", "Pix", "01/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_2", "carlos.eduardo@email.com", "Carlos Eduardo", 6, 9.99, "ATIVO", "Cartão de Crédito", "02/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_3", "mariana.silva@email.com", "Mariana Silva", 4, 9.99, "PENDENTE", "Pix", "03/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_4", "lucas.oliveira@email.com", "Lucas Oliveira", 7, 9.99, "ATIVO", "Cartão de Crédito", "03/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_5", "fernanda.lima@email.com", "Fernanda Lima", 8, 9.99, "ATIVO", "Pix", "04/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_6", "juliana.costa@email.com", "Juliana Costa", 6, 9.99, "CANCELADO", "Cartão de Crédito", "04/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_7", "patricia.melo@email.com", "Patrícia Melo", 5, 9.99, "ATIVO", "Pix", "05/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_8", "andre.santos@email.com", "André Santos", 9, 9.99, "PENDENTE", "Boleto", "05/11/2024"));
        paymentRecordsList.add(new PaymentRecord("pay_9", "beatriz.alves@email.com", "Beatriz Alves", 4, 9.99, "ATIVO", "Cartão de Crédito", "06/11/2024"));
    }

    public List<FeedbackMessage> getFeedbackMessages() {
        return feedbackList;
    }

    public void addFeedbackMessage(FeedbackMessage message) {
        feedbackList.add(0, message);
    }

    public void replyToFeedbackMessage(String messageId, String replyText) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        String time = sdf.format(new Date());
        for (FeedbackMessage msg : feedbackList) {
            if (msg.getId().equals(messageId)) {
                msg.setReply(replyText);
                msg.setReplyTimestamp(time);
                msg.setStatus("Respondida");
                break;
            }
        }
    }

    public List<PaymentRecord> getPaymentRecords() {
        return paymentRecordsList;
    }

    public void addPaymentRecord(PaymentRecord record) {
        paymentRecordsList.add(0, record);
    }

    public void recordUserPayment(String method) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateStr = sdf.format(new Date());
        currentProfile.setSubscriptionStatus("ATIVO");
        currentProfile.setPaymentMethod(method);
        saveProfile(currentProfile);

        PaymentRecord record = new PaymentRecord(
                "pay_" + System.currentTimeMillis(),
                currentProfile.getEmail(),
                currentProfile.getChildName() + " (Responsável)",
                currentProfile.getChildAge(),
                9.99,
                "ATIVO",
                method,
                dateStr
        );
        paymentRecordsList.add(0, record);
    }

    public int getTotalUsersCount() {
        return 148;
    }

    public int[] getAgeDistribution() {
        // [3-5 anos, 6-7 anos, 8-10 anos]
        return new int[]{62, 56, 30};
    }
}
