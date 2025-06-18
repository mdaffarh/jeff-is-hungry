package viewmodel;

import audio.AudioManager;
import model.*;
import javax.swing.Timer;
import java.awt.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * GameViewModel adalah "otak" dari permainan.
 * Kelas ini memegang semua state (kondisi) permainan, mengelola logika utama game loop,
 * dan menyediakan antarmuka (API) untuk View (GamePanel) agar bisa menampilkan data
 * dan mengirimkan input dari pengguna.
 */
public class GameViewModel {

    // Dependensi & State Utama
    private final ResultDAO resultDAO;
    private GameState gameState;
    private Player player;
    private Lasso lasso;
    private final List<Food> foodItems;
    private final List<FloatingScore> floatingScores;

    // State Sesi Permainan
    private int currentScore;
    private int currentCount;
    private String username;

    // Konfigurasi Gameplay
    private final Random random = new Random();
    private Timer playerActionTimer;
    private long lastFootstepTime = 0;
    private static final long FOOTSTEP_DELAY = 250; // Jeda antar suara langkah (ms)

    // Data Makanan
    private final String[] positiveFoodNames = {"steak", "hotdog", "hamburger", "cheesecake", "chocolate", "pudding", "sushi"};
    private final int[] positiveFoodScores = {20, 20, 20, 10, 10, 10, 10};
    private final String[] negativeFoodNames = {"carrot", "green", "corn", "eggplant", "cucumber"};
    private final int[] negativeFoodScores = {-10, -10, -10, -10, -10};

    /**
     * Constructor untuk GameViewModel.
     * Menginisialisasi semua state awal dan data access object.
     */
    public GameViewModel() {
        this.resultDAO = new ResultDAO();
        this.foodItems = new CopyOnWriteArrayList<>();
        this.floatingScores = new CopyOnWriteArrayList<>();
        this.gameState = GameState.START_SCREEN;
    }

    // =========================================================================
    // METODE PUBLIK (API UNTUK VIEW)
    // =========================================================================

    public enum GameState { START_SCREEN, PLAYING, PAUSED }

    /** Memulai sesi permainan baru. */
    public void startGame(String username, int panelWidth, int panelHeight) {
        this.username = username;
        resultDAO.createUserIfNotExist(this.username);
        this.currentScore = 0;
        this.currentCount = 0;
        this.player = new Player(panelWidth / 2, panelHeight / 2);
        this.lasso = new Lasso(player.getPosition());
        this.gameState = GameState.PLAYING;
        foodItems.clear();
        floatingScores.clear();
    }

    /** Menghentikan permainan dan menyimpan skor ke database. */
    public void stopGameAndSave() {
        if (this.username != null && !this.username.trim().isEmpty()) {
            if (currentScore > 0 || currentCount > 0) {
                Result sessionResult = new Result(username, currentScore, currentCount);
                resultDAO.saveOrUpdate(sessionResult);
            }
        }
        this.gameState = GameState.START_SCREEN;
    }

    /** Menjeda permainan. */
    public void pauseGame() {
        if (this.gameState == GameState.PLAYING) {
            this.gameState = GameState.PAUSED;
        }
    }

    /** Melanjutkan permainan dari jeda. */
    public void resumeGame() {
        if (this.gameState == GameState.PAUSED) {
            this.gameState = GameState.PLAYING;
        }
    }

    /** Menembakkan laso dari posisi pemain. */
    public void fireLasso(Point target) {
        if (player == null) return;
        AudioManager.getInstance().playSound("eat_sound");

        Food previouslyCaughtFood = lasso.getCaughtFood();
        if (previouslyCaughtFood != null) {
            previouslyCaughtFood.setState(Food.FoodState.DEFAULT);
        }

        lasso.reset();
        lasso.fire(target);
        setPlayerActionState(Player.PlayerState.EATING, 500);
    }

    /** Menggerakkan pemain dan menangani batasan layar. */
    public void movePlayer(String direction, int panelWidth, int panelHeight) {
        if (player == null) return;
        player.setState(Player.PlayerState.WALKING);
        int speed = 4;

        switch (direction) {
            case "UP": player.move(0, -speed); break;
            case "DOWN": player.move(0, speed); break;
            case "LEFT": player.move(-speed, 0); player.setFacing(Player.Direction.LEFT); break;
            case "RIGHT": player.move(speed, 0); player.setFacing(Player.Direction.RIGHT); break;
        }

        // Logika "clamping" untuk menjaga pemain di dalam layar
        Point currentPos = player.getPosition();
        int newX = currentPos.x;
        int newY = currentPos.y;
        int halfWidth = Player.WIDTH / 2;
        int halfHeight = Player.HEIGHT / 2;
        newX = Math.max(halfWidth, Math.min(newX, panelWidth - halfWidth));
        newY = Math.max(halfHeight, Math.min(newY, panelHeight - halfHeight));
        player.setPosition(newX, newY);

        // Memutar suara langkah dengan jeda
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFootstepTime > FOOTSTEP_DELAY) {
            AudioManager.getInstance().playSound("footstep");
            lastFootstepTime = currentTime;
        }
    }

    /** Menghentikan animasi berjalan dan mengembalikan state pemain ke IDLE. */
    public void stopWalking() {
        if (player != null && player.getState() == Player.PlayerState.WALKING) {
            player.setState(Player.PlayerState.IDLE);
        }
    }

    // =========================================================================
    // METODE UTAMA GAME LOOP
    // =========================================================================

    /**
     * Metode utama yang dipanggil di setiap frame oleh GamePanel.
     * Metode ini berfungsi sebagai "sutradara" yang mendelegasikan tugas-tugas update.
     */
    public void updateGame(int panelWidth, int panelHeight) {
        if (gameState != GameState.PLAYING) return;

        updateFloatingScores();
        updateLasso(panelWidth, panelHeight);
        updateFoodItems(panelWidth, panelHeight);
        trySpawningNewFood(panelWidth, panelHeight);
    }

    // =========================================================================
    // METODE HELPER UNTUK LOGIKA GAME
    // =========================================================================

    /** Mengupdate posisi dan sisa hidup dari setiap skor mengambang. */
    private void updateFloatingScores() {
        for (FloatingScore fs : floatingScores) {
            fs.update();
        }
        floatingScores.removeIf(fs -> !fs.isAlive());
    }

    /** Mengupdate posisi laso dan menangani logika saat makanan berhasil ditarik. */
    private void updateLasso(int panelWidth, int panelHeight) {
        Point playerCenter = player.getPosition();
        Player.Direction facing = player.getFacing();
        int forwardOffset = 30;
        int verticalOffset = 35;
        Point tongueOrigin;

        if (facing == Player.Direction.RIGHT) {
            tongueOrigin = new Point(playerCenter.x + forwardOffset, playerCenter.y + verticalOffset);
        } else {
            tongueOrigin = new Point(playerCenter.x - forwardOffset, playerCenter.y + verticalOffset);
        }

        lasso.update(tongueOrigin, foodItems);

        if (lasso.getState() == Lasso.LassoState.RETRACTING && lasso.getEndPoint().distance(tongueOrigin) < 20) {
            Food caughtFood = lasso.getCaughtFood();
            if (caughtFood != null) {
                caughtFood.setState(Food.FoodState.ANIMATING_TO_BASKET);
            }
            lasso.reset();
        }
    }

    /** Mengupdate posisi makanan, menangani animasi ke keranjang, dan menghapus makanan di luar layar. */
    private void updateFoodItems(int panelWidth, int panelHeight) {
        for (Food food : foodItems) {
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {
                handleFoodAnimationToBasket(food, panelWidth, panelHeight);
            } else {
                food.move();
            }
        }
        foodItems.removeIf(food -> food.getState() == Food.FoodState.DEFAULT && (food.getPosition().x > panelWidth + 50 || food.getPosition().x < -50));
    }

    /** Menangani logika spesifik saat makanan beranimasi menuju keranjang skor. */
    private void handleFoodAnimationToBasket(Food food, int panelWidth, int panelHeight) {
        int basketWidth = 260;
        int basketHeight = 220;
        int basketX = panelWidth - basketWidth;
        int basketY = (panelHeight - basketHeight) / 2;
        Point basketPosition = new Point(basketX + basketWidth / 2, basketY + basketHeight / 2);

        if (food.getPosition().distance(basketPosition) < 15) {
            currentScore += food.getValue();
            currentCount++;
            createFloatingScore(food, basketPosition);
            foodItems.remove(food);
        } else {
            double dx = basketPosition.x - food.getPosition().x;
            double dy = basketPosition.y - food.getPosition().y;
            double ratio = 15 / (food.getPosition().distance(basketPosition));
            food.getPosition().x += (int) (dx * ratio);
            food.getPosition().y += (int) (dy * ratio);
        }
    }

    /** Mencoba memunculkan makanan baru secara acak. */
    private void trySpawningNewFood(int panelWidth, int panelHeight) {
        if (random.nextInt(100) > 95 && foodItems.size() < 15) {
            generateFood(panelWidth, panelHeight);
        }
    }

    /** Membuat objek makanan baru dengan tipe dan posisi acak. */
    private void generateFood(int panelWidth, int panelHeight) {
        String name;
        int value;
        Food.FoodType type;

        if (random.nextInt(10) < 7) {
            type = Food.FoodType.POSITIVE;
            int index = random.nextInt(positiveFoodNames.length);
            name = positiveFoodNames[index];
            value = positiveFoodScores[index];
        } else {
            type = Food.FoodType.NEGATIVE;
            int index = random.nextInt(negativeFoodNames.length);
            name = negativeFoodNames[index];
            value = negativeFoodScores[index];
        }

        int topZoneLimit = panelHeight / 3;
        int bottomZoneStart = panelHeight * 2 / 3;
        int foodSize = 64;
        int yPos = random.nextBoolean()
                ? random.nextInt(topZoneLimit - foodSize)
                : bottomZoneStart + random.nextInt(panelHeight - bottomZoneStart - foodSize);

        int xPos = yPos < topZoneLimit ? panelWidth : -30;
        int speed = yPos < topZoneLimit ? -(random.nextInt(3) + 1) : (random.nextInt(3) + 1);

        foodItems.add(new Food(name, value, type, xPos, yPos, speed));
    }

    /** Membuat objek skor mengambang. */
    private void createFloatingScore(Food food, Point position) {
        int value = food.getValue();
        String text = (value > 0 ? "+" : "") + value;
        Color color = (value > 0 ? new Color(34, 139, 34) : Color.RED);
        floatingScores.add(new FloatingScore(text, position, color));
    }

    /** Mengatur state pemain untuk aksi sesaat (seperti makan). */
    private void setPlayerActionState(Player.PlayerState actionState, int durationMs) {
        if (player.getState() == actionState && playerActionTimer != null && playerActionTimer.isRunning()) {
            playerActionTimer.restart();
            return;
        }
        if (playerActionTimer != null) {
            playerActionTimer.stop();
        }
        player.setState(actionState);
        playerActionTimer = new Timer(durationMs, e -> player.setState(Player.PlayerState.IDLE));
        playerActionTimer.setRepeats(false);
        playerActionTimer.start();
    }

    // =========================================================================
    // GETTERS (Untuk dibaca oleh View)
    // =========================================================================

    public Player getPlayer() { return player; }
    public Lasso getLasso() { return lasso; }
    public List<Food> getFoodItems() { return foodItems; }
    public int getCurrentScore() { return currentScore; }
    public int getCurrentCount() { return currentCount; }
    public GameState getGameState() { return gameState; }
    public List<Result> getAllScores() { return resultDAO.getAllHasil(); }
    public List<FloatingScore> getFloatingScores() { return floatingScores; }
}