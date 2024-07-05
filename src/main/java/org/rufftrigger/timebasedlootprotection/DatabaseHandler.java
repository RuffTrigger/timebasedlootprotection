package org.rufftrigger.timebasedlootprotection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseHandler {

    private static final String DB_URL = "jdbc:sqlite:plugins/LootProtectionPlugin/protection.db";

    public DatabaseHandler() {
        createTable();
    }

    private void createTable() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS item_protection (" +
                             "item_uuid TEXT PRIMARY KEY, " +
                             "owner_uuid TEXT, " +
                             "expiration_time LONG, " +
                             "drop_location_x DOUBLE, " +
                             "drop_location_y DOUBLE, " +
                             "drop_location_z DOUBLE, " +
                             "world_uuid TEXT)")) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addItemProtection(UUID itemUUID, UUID ownerUUID, long expirationTime, double dropLocationX, double dropLocationY, double dropLocationZ, UUID worldUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT OR REPLACE INTO item_protection " +
                             "(item_uuid, owner_uuid, expiration_time, drop_location_x, drop_location_y, drop_location_z, world_uuid) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            statement.setString(1, itemUUID.toString());
            statement.setString(2, ownerUUID != null ? ownerUUID.toString() : null);
            statement.setLong(3, expirationTime);
            statement.setDouble(4, dropLocationX);
            statement.setDouble(5, dropLocationY);
            statement.setDouble(6, dropLocationZ);
            statement.setString(7, worldUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeItemProtection(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UUID getOwnerUUID(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT owner_uuid FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("owner_uuid") != null ? UUID.fromString(resultSet.getString("owner_uuid")) : null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long getExpirationTime(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT expiration_time FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("expiration_time");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public double getDropLocationX(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT drop_location_x FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("drop_location_x");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getDropLocationY(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT drop_location_y FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("drop_location_y");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public double getDropLocationZ(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT drop_location_z FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("drop_location_z");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public UUID getWorldUUID(UUID itemUUID) {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT world_uuid FROM item_protection WHERE item_uuid = ?")) {
            statement.setString(1, itemUUID.toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString("world_uuid"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ItemData> getProtectedItems() {
        List<ItemData> items = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT * FROM item_protection")) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                UUID itemUUID = UUID.fromString(resultSet.getString("item_uuid"));
                double dropLocationX = resultSet.getDouble("drop_location_x");
                double dropLocationY = resultSet.getDouble("drop_location_y");
                double dropLocationZ = resultSet.getDouble("drop_location_z");
                UUID worldUUID = UUID.fromString(resultSet.getString("world_uuid"));
                long expirationTime = resultSet.getLong("expiration_time");

                ItemData itemData = new ItemData(itemUUID, dropLocationX, dropLocationY, dropLocationZ, worldUUID, expirationTime);
                items.add(itemData);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
}
