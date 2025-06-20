package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ResultDAO (Data Access Object) bertanggung jawab untuk semua interaksi
 * dengan tabel 'thasil' di database.
 * Versi ini menerapkan logika "Skor Tertinggi (Best Score)".
 */
public class ResultDAO {

    /**
     * Mengambil semua data hasil dari database, diurutkan dari skor tertinggi.
     * @return List dari objek Result.
     */
    public List<Result> getAllResult() {
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

    /**
     * KETERANGAN: Logika metode ini diubah total untuk mendukung sistem "Best Score".
     * Menyimpan skor baru HANYA JIKA skor tersebut lebih tinggi dari yang sudah ada,
     * atau jika pengguna belum ada di database.
     * @param sessionResult Objek Result dari sesi permainan yang baru saja selesai.
     */
    public void saveOrUpdate(Result sessionResult) {
        // Cek apakah pengguna sudah ada dan dapatkan data lamanya
        Result storedResult = getResultByUsername(sessionResult.getUsername());

        if (storedResult == null) {
            // Jika pengguna belum ada, langsung masukkan data baru.
            insertNewUser(sessionResult);
        } else {
            // Jika pengguna sudah ada, bandingkan skornya.
            if (sessionResult.getSkor() > storedResult.getSkor()) {
                // Jika skor baru lebih tinggi, update data di database.
                updateBestScore(sessionResult);
            }
            // Jika skor baru tidak lebih tinggi, tidak melakukan apa-apa.
        }
    }

    /**
     * KETERANGAN: Metode helper baru untuk mengambil data satu pengguna.
     * @param username Nama pengguna yang akan dicari.
     * @return Objek Result jika ditemukan, null jika tidak.
     */
    private Result getResultByUsername(String username) {
        String sql = "SELECT * FROM thasil WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Result(
                            rs.getString("username"),
                            rs.getInt("skor"),
                            rs.getInt("count")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null jika user tidak ditemukan
    }

    /**
     * KETERANGAN: Metode ini sekarang hanya untuk memasukkan pengguna baru.
     * @param hasil Data pengguna baru.
     */
    private void insertNewUser(Result hasil) {
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

    /**
     * KETERANGAN: Metode ini sekarang hanya untuk meng-update skor tertinggi (menimpa skor lama).
     * Query diubah dari "skor = skor + ?" menjadi "skor = ?".
     * @param hasil Data baru yang akan menimpa data lama.
     */
    private void updateBestScore(Result hasil) {
        String sql = "UPDATE thasil SET skor = ?, count = ? WHERE username = ?";
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

    /**
     * Memeriksa apakah seorang pengguna sudah ada di database.
     * @param username Nama pengguna yang akan diperiksa.
     * @return true jika ada, false jika tidak.
     */
    public void createUserIfNotExist(String username) {
        if (getResultByUsername(username) == null) {
            insertNewUser(new Result(username, 0, 0));
        }
    }
}