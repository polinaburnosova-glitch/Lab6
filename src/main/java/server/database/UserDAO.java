package server.database;

import common.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class UserDAO {
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        }
        catch (Exception e) {
            throw new RuntimeException("Ошибка хэширования пароля", e);
        }
    }

    public static boolean register(String username, String password) {
        String hash = hashPassword(password);
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            stmt.executeUpdate();
            return true;
        }

        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static User login(String username, String password) {
        String hash = hashPassword(password);
        String sql = "SELECT username, password_hash FROM users WHERE username = ? AND password_hash = ?";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, hash);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(rs.getString("username"), rs.getString("password_hash"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }

        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
