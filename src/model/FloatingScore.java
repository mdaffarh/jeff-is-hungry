package model;

import java.awt.Color;
import java.awt.Point;

public class FloatingScore {
    private final String text;
    private final Color color;
    private final Point.Float position;
    private int lifespan; // durasi dalam frame/tick
    private final float yVelocity = -0.5f; // Kecepatan gerakan ke atas

    private final int MAX_LIFESPAN = 70; // teks akan hilang setelah sekitar 70 frame

    // constructor
    public FloatingScore(String text, Point startPosition, Color color) {
        this.text = text;
        this.color = color;
        this.position = new Point.Float(startPosition.x, startPosition.y);
        this.lifespan = MAX_LIFESPAN;
    }

    // metode ini akan dipanggil di setiap frame game loop
    public void update() {
        if (isAlive()) {
            position.y += yVelocity; // Gerakkan ke atas
            lifespan--; // Kurangi sisa hidup
        }
    }

    public boolean isAlive() {
        return lifespan > 0;
    }

    // getters untuk digunakan oleh view
    public String getText() { return text; }
    public Color getColor() { return color; }
    public Point.Float getPosition() { return position; }
    public int getLifespan() { return lifespan; }
    public int getMaxLifespan() { return MAX_LIFESPAN; }
}