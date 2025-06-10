package model;

public class Hasil {
    private String username;
    private int skor;
    private int count;

    public Hasil(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public int getSkor() { return skor; }
    public int getCount() { return count; }
    public void setSkor(int skor) { this.skor = skor; }
    public void setCount(int count) { this.count = count; }
}