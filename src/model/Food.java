package model;

import java.awt.Point;

public class Food {

    // enum untuk membedakan jenis makanan
    public enum FoodType {
        POSITIVE, // menambah skor
        NEGATIVE  // mengurangi skor
    }

    // enum untuk status animasi
    public enum FoodState {
        DEFAULT,
        CAPTURED_BY_LASSO,
        ANIMATING_TO_BASKET
    }

    private Point position;
    private final String name;      // nama aset
    private final int value;        // nilai skor
    private final FoodType type;    // jenis makanan
    private FoodState state;
    private int speed;

    // constructor
    public Food(String name, int value, FoodType type, int x, int y, int speed) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.position = new Point(x, y);
        this.speed = speed;
        this.state = FoodState.DEFAULT;
    }

    // method untuk menggerakan makanan
    public void move() {
        if (state == FoodState.DEFAULT) {
            position.translate(speed, 0);
        }
    }

    // getters
    public Point getPosition() { return position; }
    public String getName() { return name; }
    public int getValue() { return value; }
    public FoodType getType() { return type; }
    public FoodState getState() { return state; }

    // setters
    public void setState(FoodState state) { this.state = state; }
}