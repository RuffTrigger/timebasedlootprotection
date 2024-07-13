package org.rufftrigger.timebasedlootprotection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseManager {

    private static Connection connection;

    public static void setupDatabase() {
        try {
            File dataFolder = new File(Main.getInstance().getDataFolder(), "protected_items.db");
            if (!dataFolder.exists()) {
                dataFolder.getParentFile().mkdirs();
                Main.getInstance().saveResource("protected_items.db", false);
            }
            String url = "jdbc:sqlite:" + dataFolder.getPath();

            connection = DriverManager.getConnection(url);
            createTableIfNotExists();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS protected_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "owner_uuid TEXT NOT NULL," +
                "item_id TEXT NOT NULL," +
                "protection_expiration INTEGER NOT NULL" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void protectItem(UUID ownerUUID, String itemId, long protectionDurationMillis) {
        long expirationTime = System.currentTimeMillis() + protectionDurationMillis;
        String sql = "INSERT INTO protected_items (owner_uuid, item_id, protection_expiration) VALUES (?, ?, ?);";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            pstmt.setString(2, itemId);
            pstmt.setLong(3, expirationTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isLocationProtected(UUID ownerUUID, String itemId) {
        String sql = "SELECT COUNT(*) AS count FROM protected_items WHERE owner_uuid = ? AND item_id = ? AND protection_expiration >= ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUUID.toString());
            pstmt.setString(2, itemId);
            pstmt.setLong(3, System.currentTimeMillis());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void removeProtection(String itemId) {
        String sql = "DELETE FROM protected_items WHERE item_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
