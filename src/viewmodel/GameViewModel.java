package viewmodel;

import model.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Random;

public class GameViewModel {
    private final HasilDAO hasilDAO;
    private Player player;
    private Lasso lasso;
    private final List<Food> foodItems;
    private int currentScore;
    private int currentCount;
    private String username;
    private final Random random = new Random();
    public enum GameState { START_SCREEN, PLAYING, PAUSED }
    private GameState gameState;

    public GameViewModel() {
        this.hasilDAO = new HasilDAO();
        this.foodItems = new CopyOnWriteArrayList<>();
        this.gameState = GameState.START_SCREEN;
    }

    public void startGame(String username, int panelWidth, int panelHeight) {
        this.username = username;
        hasilDAO.createUserIfNotExist(this.username);
        this.currentScore = 0;
        this.currentCount = 0;
        this.player = new Player(panelWidth / 2, panelHeight / 2);
        this.lasso = new Lasso(player.getPosition());
        this.gameState = GameState.PLAYING;
        foodItems.clear();
    }

    public void stopGameAndSave() {
        if (this.username != null && !this.username.trim().isEmpty()) {
            if (currentScore > 0 || currentCount > 0) {
                Hasil sessionResult = new Hasil(username, currentScore, currentCount);
                hasilDAO.saveOrUpdate(sessionResult);
            }
        }
        this.gameState = GameState.START_SCREEN;
    }

    public void updateGame(int panelWidth, int panelHeight) {
        if (gameState != GameState.PLAYING) return;

        // Update posisi dan status laso
        lasso.update(player.getPosition(), foodItems);

        // --- LOGIKA BARU UNTUK ANIMASI 2 TAHAP ---

        // 1. Cek jika laso sudah kembali ke pemain
        if (lasso.getState() == Lasso.LassoState.RETRACTING && lasso.getEndPoint().distance(player.getPosition()) < 20) {
            Food caughtFood = lasso.getCaughtFood();
            if (caughtFood != null) {
                // Tahap 1 Selesai: Ubah status bola untuk memulai animasi ke keranjang
                caughtFood.setState(Food.FoodState.ANIMATING_TO_BASKET);
            }
            lasso.reset(); // Reset laso agar bisa digunakan lagi
        }

        // 2. Proses semua bola sesuai statusnya
        for (Food food : foodItems) {
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {
                // Tahap 2: Animasikan bola bergerak ke keranjang (kotak skor)
                Point basketPosition = new Point(panelWidth - 100, 40); // Koordinat keranjang
                double dx = basketPosition.x - food.getPosition().x;
                double dy = basketPosition.y - food.getPosition().y;
                double distance = food.getPosition().distance(basketPosition);

                if (distance < 15) {
                    // Animasi Selesai: Update skor dan hapus bola
                    currentScore += food.getValue(); // Skor bertambah
                    currentCount++; // Jumlah bola bertambah
                    foodItems.remove(food);
                } else {
                    // Masih bergerak ke keranjang
                    double ratio = 15 / distance; // kecepatan 15 pixel/frame
                    food.getPosition().x += (int) (dx * ratio);
                    food.getPosition().y += (int) (dy * ratio);
                }
            } else {
                // Jika status bola bukan animating, gerakkan seperti biasa
                food.move();
            }
        }

        // Generate makanan baru secara acak
        if (random.nextInt(100) > 95 && foodItems.size() < 15) {
            generateFood(panelWidth, panelHeight);
        }

        // Hapus makanan yang keluar layar (hanya yang statusnya default)
        foodItems.removeIf(food -> food.getState() == Food.FoodState.DEFAULT && (food.getPosition().x > panelWidth + 50 || food.getPosition().x < -50));
    }

    public void fireLasso(Point target) {
        if (lasso.isIdle()) {
            lasso.fire(target);
        }
    }

    private void generateFood(int panelWidth, int panelHeight) {
        int value = (random.nextInt(10) + 1) * 10;
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        boolean fromTop = random.nextBoolean();
        int yPos = fromTop ? random.nextInt(panelHeight / 2 - 50) : (panelHeight / 2) + random.nextInt(panelHeight / 2 - 50);
        int xPos = fromTop ? panelWidth : -30;
        int speed = fromTop ? -(random.nextInt(3) + 1) : (random.nextInt(3) + 1);
        foodItems.add(new Food(xPos, yPos, value, speed, color));
    }

    // Getters
    public Player getPlayer() { return player; }
    public Lasso getLasso() { return lasso; }
    public List<Food> getFoodItems() { return foodItems; }
    public int getCurrentScore() { return currentScore; }
    public int getCurrentCount() { return currentCount; }
    public GameState getGameState() { return gameState; }
    public List<Hasil> getAllScores() { return hasilDAO.getAllHasil(); }
}