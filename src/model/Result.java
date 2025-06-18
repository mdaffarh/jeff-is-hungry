package model;

public class Result {
    private String username;
    private int skor;
    private int count;

    // constructor
    public Result(String username, int skor, int count) {
        this.username = username;
        this.skor = skor;
        this.count = count;
    }

    // getters and setters
    public String getUsername() { return username; }
    public int getSkor() { return skor; }
    public int getCount() { return count; }
    public void setSkor(int skor) { this.skor = skor; }
    public void setCount(int count) { this.count = count; }
}