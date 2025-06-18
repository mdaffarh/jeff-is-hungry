package model;

import java.awt.Point;

public class Player {
    // konstanta ukuran pemain
    public static final int WIDTH = 140;
    public static final int HEIGHT = 140;

    // enum untuk arah pemain
    public enum Direction {
        LEFT,
        RIGHT
    }

    // enum state pemain
    public enum PlayerState {
        IDLE,
        WALKING,
        EATING
    }

    private Point position;
    private Direction facing;
    private PlayerState state;

    // constructor
    public Player(int startX, int startY) {
        this.position = new Point(startX, startY);
        this.facing = Direction.RIGHT;
        this.state = PlayerState.IDLE;
    }

    // menggerakan player
    public void move(int dx, int dy) {
        position.translate(dx, dy);
    }

    // getter dan setter
    public Point getPosition() { return position; }
    public void setPosition(int x, int y) {
        this.position.setLocation(x, y);
    }
    public Direction getFacing() { return facing; }
    public void setFacing(Direction facing) { this.facing = facing; }
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
}