package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/shark_game_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * @return Connection object yang baru dan terbuka.
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // buat instance koneksi baru
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Gagal terhubung ke database: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}