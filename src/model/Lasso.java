package model;

import java.awt.Point;
import java.util.List;

public class Lasso {

    // Status yang mungkin untuk laso
    public enum LassoState {
        IDLE,       // Diam, tidak digunakan
        EXTENDING,  // Memanjang ke arah target
        RETRACTING  // Menarik kembali ke pemain
    }

    private LassoState state;
    private Point startPoint;
    private Point endPoint;
    private Point targetPoint; // Titik tujuan (tempat mouse diklik)
    private Food caughtFood;
    private final int speed = 20; // Kecepatan gerakan laso

    public Lasso(Point startPoint) {
        this.startPoint = startPoint;
        this.endPoint = new Point(startPoint);
        this.state = LassoState.IDLE;
    }

    // Metode untuk "menembakkan" laso
    public void fire(Point target) {
        if (state == LassoState.IDLE) {
            this.state = LassoState.EXTENDING;
            this.targetPoint = target;
        }
    }

    public void update(Point playerPosition, List<Food> foodItems) {
        this.startPoint = playerPosition;

        if (state == LassoState.EXTENDING) {
            moveTowards(targetPoint);

            // Periksa tabrakan dengan bola yang statusnya masih DEFAULT
            for (Food food : foodItems) {
                if (food.getState() == Food.FoodState.DEFAULT && endPoint.distance(food.getPosition()) < 25) {
                    this.caughtFood = food;
                    this.caughtFood.setState(Food.FoodState.CAPTURED_BY_LASSO); // Ubah status bola!
                    this.state = LassoState.RETRACTING;
                    return;
                }
            }

            if (endPoint.distance(targetPoint) < speed) {
                this.state = LassoState.RETRACTING;
            }

        } else if (state == LassoState.RETRACTING) {
            moveTowards(startPoint);

            if (caughtFood != null) {
                caughtFood.getPosition().setLocation(endPoint);
            }

            if (endPoint.distance(startPoint) < speed) {
                // Cukup berhenti di sini. ViewModel yang akan proses selanjutnya.
                // Tidak perlu reset di sini lagi, biarkan ViewModel yang mengontrol.
            }
        }
    }

    // Logika pergerakan titik akhir laso
    private void moveTowards(Point target) {
        double dx = target.x - endPoint.x;
        double dy = target.y - endPoint.y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > speed) {
            double ratio = speed / distance;
            endPoint.x += (int) (dx * ratio);
            endPoint.y += (int) (dy * ratio);
        } else {
            endPoint.setLocation(target);
        }
    }

    public void reset() {
        this.state = LassoState.IDLE;
        this.endPoint.setLocation(this.startPoint);
        this.caughtFood = null;
    }

    // Getters
    public LassoState getState() { return state; }
    public Point getStartPoint() { return startPoint; }
    public Point getEndPoint() { return endPoint; }
    public Food getCaughtFood() { return caughtFood; }
    public boolean isIdle() { return state == LassoState.IDLE; }
}