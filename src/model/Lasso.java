package model;

import java.awt.Point;
import java.util.List;

public class Lasso {

    // enum untuk state lasso
    public enum LassoState {
        IDLE,       // tidak digunakan
        EXTENDING,  // memanjang ke arah target
        RETRACTING  // menarik kembali ke pemain
    }

    private LassoState state;
    private Point startPoint;
    private Point endPoint;
    private Point targetPoint; // titik tujuan (point mouse diklik)
    private Food caughtFood;
    private final int speed = 20; // kecepatan lasso

    // constructor
    public Lasso(Point startPoint) {
        this.startPoint = startPoint;
        this.endPoint = new Point(startPoint);
        this.state = LassoState.IDLE;
    }

    // menembakkan lasso
    public void fire(Point target) {
        // ubah state dari idle ke extending, set target
        if (state == LassoState.IDLE) {
            this.state = LassoState.EXTENDING;
            this.targetPoint = target;
        }
    }

    // update lasso sesuai state
    public void update(Point playerPosition, List<Food> foodItems) {
        // point awal/lokasi pemain
        this.startPoint = playerPosition;

        // lasso memanjang
        if (state == LassoState.EXTENDING) {
            moveTowards(targetPoint);

            // radius tabrakan
            int collisionRadius = 25; // nabrak jika kurang dari 25px
            int radiusSquared = collisionRadius * collisionRadius; // pembanding

            for (Food food : foodItems) {
                // cek hanya makanan yang masih default
                if (food.getState() == Food.FoodState.DEFAULT) {
                    // hitung kuadrat jarak
                    double dx = endPoint.x - food.getPosition().x;
                    double dy = endPoint.y - food.getPosition().y;
                    double distanceSquared = dx * dx + dy * dy;

                    // bandingkan kuadrat jaraknya
                    if (distanceSquared < radiusSquared) {
                        this.caughtFood = food;
                        this.caughtFood.setState(Food.FoodState.CAPTURED_BY_LASSO);
                        this.state = LassoState.RETRACTING;
                        return; // keluar dari loop setelah menemukan target
                    }
                }
            }

            // jika mencapai target tanpa kena bola, tarik kembali
            if (endPoint.distance(targetPoint) < speed) {
                this.state = LassoState.RETRACTING;
            }
            // menarik kembali
        } else if (state == LassoState.RETRACTING) {
            moveTowards(startPoint);
            if (caughtFood != null) {
                caughtFood.getPosition().setLocation(endPoint);
            }
        }
    }

    // logika pergerakan titik akhir laso
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

    // method untuk mereset lasso
    public void reset() {
        this.state = LassoState.IDLE;
        if (this.startPoint != null) {
            this.endPoint.setLocation(this.startPoint);
        }
        // lupakan referensi ke makanan tanpa mengubah state makanannya.
        this.caughtFood = null;
    }

    // getters
    public LassoState getState() { return state; }
    public Point getStartPoint() { return startPoint; }
    public Point getEndPoint() { return endPoint; }
    public Food getCaughtFood() { return caughtFood; }
}