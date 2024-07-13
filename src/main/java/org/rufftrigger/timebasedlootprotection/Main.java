package org.rufftrigger.timebasedlootprotection;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Main extends JavaPlugin {

    private static Main instance;
    private DatabaseHandler databaseHandler;

    @Override
    public void onEnable() {
        instance = this;
        databaseHandler = new DatabaseHandler();

        // Ensure the database is initialized
        DatabaseManager.createDatabase();

        // Register events
        getServer().getPluginManager().registerEvents(new EventListener(), this);

        getLogger().info("TimeBasedLootProtection has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("TimeBasedLootProtection has been disabled.");
    }

    public static Main getInstance() {
        return instance;
    }
}
