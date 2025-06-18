package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResultDAO {
    // list seluruh hasil
    public List<Result> getAllHasil() {
        List<Result> results = new ArrayList<>();
        String sql = "SELECT * FROM thasil ORDER BY skor DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(new Result(
                        rs.getString("username"),
                        rs.getInt("skor"),
                        rs.getInt("count")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    // cek username
    public boolean userExists(String username) {
        String sql = "SELECT 1 FROM thasil WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // buat user jika belum ada (not-casesensitive)
    public void createUserIfNotExist(String username) {
        if (!userExists(username)) {
            // jika user belum ada, insert dengan skor 0
            insert(new Result(username, 0, 0));
        }
    }

    // wrapper untuk insert/update player
    public void saveOrUpdate(Result result) {
        // update kalau ada
        if (userExists(result.getUsername())) {
            update(result);
        } else {
            // insert jika belum ada
            insert(result);
        }
    }

    // insert data
    private void insert(Result result) {
        String sql = "INSERT INTO thasil(username, skor, count) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, result.getUsername());
            pstmt.setInt(2, result.getSkor());
            pstmt.setInt(3, result.getCount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // update data
    private void update(Result result) {
        String sql = "UPDATE thasil SET skor = skor + ?, count = count + ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, result.getSkor());
            pstmt.setInt(2, result.getCount());
            pstmt.setString(3, result.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}