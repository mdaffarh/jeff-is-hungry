package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HasilDAO {

    public List<Hasil> getAllHasil() {
        List<Hasil> results = new ArrayList<>();
        String sql = "SELECT * FROM thasil ORDER BY skor DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(new Hasil(
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

    // VVV TAMBAHKAN METODE BARU INI VVV
    public void createUserIfNotExist(String username) {
        if (!userExists(username)) {
            // Jika user belum ada, insert dengan skor 0
            insert(new Hasil(username, 0, 0));
        }
    }

    public void saveOrUpdate(Hasil hasil) {
        // Metode ini sekarang akan lebih sering melakukan UPDATE
        if (userExists(hasil.getUsername())) {
            update(hasil);
        } else {
            // Fallback jika karena suatu hal user belum ada
            insert(hasil);
        }
    }

    private void insert(Hasil hasil) {
        String sql = "INSERT INTO thasil(username, skor, count) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hasil.getUsername());
            pstmt.setInt(2, hasil.getSkor());
            pstmt.setInt(3, hasil.getCount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void update(Hasil hasil) {
        String sql = "UPDATE thasil SET skor = skor + ?, count = count + ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hasil.getSkor());
            pstmt.setInt(2, hasil.getCount());
            pstmt.setString(3, hasil.getUsername());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}