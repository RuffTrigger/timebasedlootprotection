package org.rufftrigger.timebasedlootprotection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.Action;

import java.util.Collection;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private DatabaseHandler databaseHandler;
    private int protectionTimeMinutes;

    @Override
    public void onEnable() {
        this.databaseHandler = new DatabaseHandler();
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);

        // Load protected items from database and schedule protection removal
        Collection<ItemData> protectedItems = databaseHandler.getProtectedItems();
        for (ItemData itemData : protectedItems) {
            if (itemData.getExpirationTime() > System.currentTimeMillis()) {
                Location location = new Location(Bukkit.getWorld(itemData.getWorldUUID()), itemData.getDropLocationX(), itemData.getDropLocationY(), itemData.getDropLocationZ());
                ItemStack itemStack = new ItemStack(Material.DIRT); // Placeholder, you'll need to store and retrieve actual item data
                Item item = location.getWorld().dropItem(location, itemStack);
                item.setInvulnerable(true); // Set item as invulnerable initially
                long delay = ((itemData.getExpirationTime() - System.currentTimeMillis()) / 50); // Convert milliseconds to ticks
                scheduleProtectionRemoval(item.getUniqueId(), delay);
            } else {
                databaseHandler.removeItemProtection(itemData.getItemUUID());
            }
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        config.addDefault("protection_time_minutes", 5);
        config.options().copyDefaults(true);
        saveConfig();
        protectionTimeMinutes = config.getInt("protection_time_minutes");
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof Item) {
            Item item = (Item) event.getEntity();
            UUID itemUUID = item.getUniqueId();
            long expirationTime = System.currentTimeMillis() + (protectionTimeMinutes * 60 * 1000); // Convert minutes to milliseconds
            double dropLocationX = item.getLocation().getX();
            double dropLocationY = item.getLocation().getY();
            double dropLocationZ = item.getLocation().getZ();
            UUID worldUUID = item.getWorld().getUID();
            databaseHandler.addItemProtection(itemUUID, null, expirationTime, dropLocationX, dropLocationY, dropLocationZ, worldUUID);
            scheduleProtectionRemoval(itemUUID, protectionTimeMinutes * 60 * 20); // Convert minutes to ticks
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        Player player = event.getPlayer();
        UUID itemUUID = item.getUniqueId();
        UUID ownerUUID = player.getUniqueId();
        long expirationTime = System.currentTimeMillis() + (protectionTimeMinutes * 60 * 1000); // Convert minutes to milliseconds
        double dropLocationX = item.getLocation().getX();
        double dropLocationY = item.getLocation().getY();
        double dropLocationZ = item.getLocation().getZ();
        UUID worldUUID = item.getWorld().getUID();
        databaseHandler.addItemProtection(itemUUID, ownerUUID, expirationTime, dropLocationX, dropLocationY, dropLocationZ, worldUUID);
        scheduleProtectionRemoval(itemUUID, protectionTimeMinutes * 60 * 20); // Convert minutes to ticks
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        Item item = event.getItem();
        UUID itemUUID = item.getUniqueId();
        UUID ownerUUID = databaseHandler.getOwnerUUID(itemUUID);
        if (ownerUUID != null && entity instanceof Player && !entity.getUniqueId().equals(ownerUUID)) {
            Long expirationTime = databaseHandler.getExpirationTime(itemUUID);
            if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Item) {
            Item item = (Item) event.getEntity();
            UUID itemUUID = item.getUniqueId();
            UUID ownerUUID = databaseHandler.getOwnerUUID(itemUUID);
            if (ownerUUID != null && event.getDamager() instanceof Player) {
                Player damager = (Player) event.getDamager();
                if (!damager.getUniqueId().equals(ownerUUID)) {
                    Long expirationTime = databaseHandler.getExpirationTime(itemUUID);
                    if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Collection<Entity> nearbyEntities = event.getLocation().getWorld().getNearbyEntities(event.getLocation(), 10, 10, 10);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                UUID itemUUID = item.getUniqueId();
                Long expirationTime = databaseHandler.getExpirationTime(itemUUID);
                if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Location location = event.getBlock().getLocation();
        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 10, 10, 10);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                UUID itemUUID = item.getUniqueId();
                Long expirationTime = databaseHandler.getExpirationTime(itemUUID);
                if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))) {
            Material material = event.getClickedBlock().getType();
            if (material == Material.TNT || material == Material.FIRE || material == Material.LAVA) {
                Location location = event.getClickedBlock().getLocation();
                Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, 10, 10, 10);
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        UUID itemUUID = item.getUniqueId();
                        Long expirationTime = databaseHandler.getExpirationTime(itemUUID);
                        if (expirationTime != null && expirationTime > System.currentTimeMillis()) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void scheduleProtectionRemoval(UUID itemUUID, long delay) {
        new BukkitRunnable() {
            @Override
            public void run() {
                databaseHandler.removeItemProtection(itemUUID);
            }
        }.runTaskLater(this, delay);
    }
}
