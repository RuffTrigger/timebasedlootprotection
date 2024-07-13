package org.rufftrigger.timebasedlootprotection;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize database
        DatabaseManager.setupDatabase();

        // Register events
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getLogger().info("TimeBasedLootProtection has been enabled.");
    }

    @Override
    public void onDisable() {
        // Ensure database connection is closed on shutdown
        DatabaseManager.closeConnection();

        getLogger().info("TimeBasedLootProtection has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }
}
