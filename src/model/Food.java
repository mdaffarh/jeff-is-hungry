package model;

import java.awt.Point;

public class Food {

    // Enum untuk membedakan jenis makanan
    public enum FoodType {
        POSITIVE, // Menambah skor
        NEGATIVE  // Mengurangi skor
    }

    // Enum untuk status animasi (seperti sebelumnya)
    public enum FoodState {
        DEFAULT,
        CAPTURED_BY_LASSO,
        ANIMATING_TO_BASKET
    }

    private Point position;
    private final String name;      // Nama aset, misal: "steak"
    private final int value;        // Nilai skor (bisa positif atau negatif)
    private final FoodType type;    // Jenis makanan
    private FoodState state;
    private int speed;

    public Food(String name, int value, FoodType type, int x, int y, int speed) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.position = new Point(x, y);
        this.speed = speed;
        this.state = FoodState.DEFAULT;
    }

    public void move() {
        if (state == FoodState.DEFAULT) {
            position.translate(speed, 0);
        }
    }

    // Getters
    public Point getPosition() { return position; }
    public String getName() { return name; }
    public int getValue() { return value; }
    public FoodType getType() { return type; }
    public FoodState getState() { return state; }

    // Setters
    public void setState(FoodState state) { this.state = state; }
}