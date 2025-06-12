package model;

import java.awt.Point;

public class Player {

    // Enum untuk arah hadap (masih kita gunakan)
    public enum Direction {
        LEFT,
        RIGHT
    }

    //-- 1. Enum baru untuk kondisi/state karakter
    public enum PlayerState {
        IDLE,    // Diam (animasi stand/jump)
        WALKING, // Sedang berjalan
        EATING   // Sedang "makan" (saat menembak laso)
    }

    private Point position;
    private Direction facing;
    //-- 2. Variabel baru untuk menyimpan state saat ini
    private PlayerState state;

    public Player(int startX, int startY) {
        this.position = new Point(startX, startY);
        this.facing = Direction.RIGHT; // Arah awal
        this.state = PlayerState.IDLE; // State awal
    }

    public Point getPosition() { return position; }
    public void move(int dx, int dy) { position.translate(dx, dy); }

    public Direction getFacing() { return facing; }
    public void setFacing(Direction facing) { this.facing = facing; }

    //-- 3. Getter dan Setter untuk state
    public PlayerState getState() { return state; }
    public void setState(PlayerState state) { this.state = state; }
}