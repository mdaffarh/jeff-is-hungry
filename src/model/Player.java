package model;

import java.awt.Point;

public class Player {
    private Point position;

    public Player(int startX, int startY) {
        this.position = new Point(startX, startY);
    }

    public Point getPosition() { return position; }
    public void move(int dx, int dy) { position.translate(dx, dy); }
}