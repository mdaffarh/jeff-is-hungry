package model;

import java.awt.Color;
import java.awt.Point;

public class Food {
    // Menambahkan status untuk bola
    public enum FoodState {
        DEFAULT,          // Bergerak normal melintasi layar
        CAPTURED_BY_LASSO, // Ditarik oleh laso
        ANIMATING_TO_BASKET // Bergerak dari pemain ke keranjang skor
    }

    private Point position;
    private final int value;
    private int speed; // Dihapus final agar bisa diubah
    private final Color color;
    private FoodState state; // Variabel untuk menyimpan status

    public Food(int x, int y, int value, int speed, Color color) {
        this.position = new Point(x, y);
        this.value = value;
        this.speed = speed;
        this.color = color;
        this.state = FoodState.DEFAULT; // Status awal
    }

    public void move() {
        // Bola hanya bergerak normal jika statusnya DEFAULT
        if (state == FoodState.DEFAULT) {
            position.translate(speed, 0);
        }
    }

    // Getters
    public Point getPosition() { return position; }
    public int getValue() { return value; }
    public Color getColor() { return color; }
    public FoodState getState() { return state; }

    // Setters
    public void setState(FoodState state) { this.state = state; }
    public void setSpeed(int speed) { this.speed = speed; }
}