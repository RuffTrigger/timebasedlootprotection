package org.rufftrigger.timebasedlootprotection;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;
        databaseManager = new DatabaseManager();

        // Setup the database
        DatabaseManager.setupDatabase();

        // Register events
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // Start protection check task
        DatabaseManager.startProtectionCheckTask();

        getLogger().info("TimeBasedLootProtection has been enabled.");
    }

    @Override
    public void onDisable() {
        // Stop protection check task
        // (Note: This is not strictly necessary as Bukkit automatically stops tasks on server shutdown)
        // Bukkit.getScheduler().cancelTasks(this);

        // Close database connection
        DatabaseManager.closeConnection();

        getLogger().info("TimeBasedLootProtection has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }
}
