package org.rufftrigger.timebasedlootprotection;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private static Connection connection;

    public static void setupDatabase() throws SQLException {
        File dataFolder = new File(Main.getInstance().getDataFolder(), "protected_items.db");
        if (!dataFolder.exists()) {
            dataFolder.getParentFile().mkdirs();
            Main.getInstance().saveResource("protected_items.db", false);
        }
        String url = "jdbc:sqlite:" + dataFolder.getPath();

        connection = DriverManager.getConnection(url);
        createTableIfNotExists();
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
            Main.getInstance().getLogger().log(Level.SEVERE, "Error creating table", e);
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
            Main.getInstance().getLogger().log(Level.SEVERE, "Error protecting item", e);
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
            Main.getInstance().getLogger().log(Level.SEVERE, "Error checking protection status", e);
        }
        return false;
    }

    public static void removeProtection(String itemId) {
        String sql = "DELETE FROM protected_items WHERE item_id = ?;";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error removing protection", e);
        }
    }

    public static void startProtectionCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                String sql = "SELECT item_id FROM protected_items WHERE protection_expiration < ?;";

                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setLong(1, currentTimeMillis);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        String itemId = rs.getString("item_id");
                        removeProtection(itemId);
                    }
                } catch (SQLException e) {
                    Main.getInstance().getLogger().log(Level.SEVERE, "Error running protection check task", e);
                }
            }
        }.runTaskTimerAsynchronously(Main.getInstance(), 20L, 20L * 60L * 5L); // Run every 5 minutes
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Error closing database connection", e);
        }
    }
}
