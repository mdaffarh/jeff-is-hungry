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

    //-- 1. Definisikan data makanan dan skornya di sini
    private final String[] positiveFoodNames = {"steak", "hotdog", "hamburger", "cheesecake", "chocolate", "pudding", "sushi"};
    private final int[] positiveFoodScores = {20, 20, 20, 10, 10, 10, 10};

    private final String[] negativeFoodNames = {"carrot", "green", "corn", "eggplant", "cucumber"};
    private final int[] negativeFoodScores = {-10, -10, -10, -10, -10};

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

        lasso.update(player.getPosition(), foodItems);

        if (lasso.getState() == Lasso.LassoState.RETRACTING && lasso.getEndPoint().distance(player.getPosition()) < 20) {
            Food caughtFood = lasso.getCaughtFood();
            if (caughtFood != null) {
                caughtFood.setState(Food.FoodState.ANIMATING_TO_BASKET);
            }
            lasso.reset();
        }

        for (Food food : foodItems) {
            if (food.getState() == Food.FoodState.ANIMATING_TO_BASKET) {

                //-- SESUAIKAN KODE INI: Tentukan posisi target keranjang yang baru
                int basketWidth = 260;
                int basketHeight = 220;
                int basketX = panelWidth - basketWidth;
                int basketY = (panelHeight - basketHeight) / 2;
                // Targetkan bagian tengah keranjang untuk animasi yang lebih baik
                Point basketPosition = new Point(basketX + basketWidth / 2, basketY + basketHeight / 2);

                double distance = food.getPosition().distance(basketPosition);

                if (distance < 15) {
                    currentScore += food.getValue();
                    currentCount++;
                    foodItems.remove(food);
                } else {
                    double dx = basketPosition.x - food.getPosition().x;
                    double dy = basketPosition.y - food.getPosition().y;
                    double ratio = 15 / distance;
                    food.getPosition().x += (int) (dx * ratio);
                    food.getPosition().y += (int) (dy * ratio);
                }
            } else {
                food.move();
            }
        }

        //-- 2. Logika generateFood dipanggil dari sini
        if (random.nextInt(100) > 95 && foodItems.size() < 15) {
            generateFood(panelWidth, panelHeight);
        }

        foodItems.removeIf(food -> food.getState() == Food.FoodState.DEFAULT && (food.getPosition().x > panelWidth + 50 || food.getPosition().x < -50));
    }

    public void fireLasso(Point target) {
        if (lasso.isIdle()) {
            lasso.fire(target);
        }
    }

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

        //-- LOGIKA BARU UNTUK ZONA SPAWN --

        // 1. Tentukan batas zona atas dan bawah
        int topZoneLimit = panelHeight / 3;
        int bottomZoneStart = panelHeight * 2 / 3;
        int foodSize = 64; // Samakan dengan ukuran di GamePanel

        int yPos;
        boolean fromTop = random.nextBoolean();

        if (fromTop) {
            // Muncul secara acak di 1/3 bagian ATAS layar
            // Pastikan seluruh gambar berada di dalam zona
            yPos = random.nextInt(topZoneLimit - foodSize);
        } else {
            // Muncul secara acak di 1/3 bagian BAWAH layar
            yPos = bottomZoneStart + random.nextInt(panelHeight - bottomZoneStart - foodSize);
        }

        // Penggunaan random.nextInt() memastikan posisi Y tidak akan simetris (tidak selalu di baris yang sama).

        // Tentukan posisi X dan kecepatan seperti sebelumnya
        int xPos = fromTop ? panelWidth : -30;
        int speed = fromTop ? -(random.nextInt(3) + 1) : (random.nextInt(3) + 1);

        foodItems.add(new Food(name, value, type, xPos, yPos, speed));
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