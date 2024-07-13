package org.rufftrigger.timebasedlootprotection;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize database
        try {
            DatabaseManager.setupDatabase();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error setting up database", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Save default config if not exists
        saveDefaultConfig();

        // Register events
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        // Start protection check task
        DatabaseManager.startProtectionCheckTask();

        getLogger().info("TimeBasedLootProtection has been enabled.");
    }

    @Override
    public void onDisable() {
        try {
            // Stop protection check task
            // (Note: This is not strictly necessary as Bukkit automatically stops tasks on server shutdown)

            // Close database connection
            DatabaseManager.closeConnection();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error disabling plugin", e);
        }

        getLogger().info("TimeBasedLootProtection has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }
}
