package server.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static Connection connection = null;

    public static String getUrl() {
        Dotenv dotenv = Dotenv.load();
        String host = dotenv.get("DB_HOST", "pg");
        String port = dotenv.get("DB_PORT", "5432");
        String dbName = dotenv.get("DB_NAME", "studs");
        return "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
    }

    private static String getUser() {
        return Dotenv.load().get("DB_USER", "studs");
    }

    private static String getPassword() {
        return Dotenv.load().get("DB_PASSWORD", "");
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(getUrl(), getUser(), getPassword());
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
