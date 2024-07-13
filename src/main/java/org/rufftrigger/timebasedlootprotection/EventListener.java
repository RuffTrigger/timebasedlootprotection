package org.rufftrigger.timebasedlootprotection;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta()) {
            UUID ownerUUID = event.getPlayer().getUniqueId();
            String itemId = item.getType().name(); // Use item type as the identifier

            DatabaseManager.protectItem(ownerUUID, itemId, getProtectionDurationMillis());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String itemId = event.getBlock().getType().name(); // Use item type as the identifier

        if (DatabaseManager.isLocationProtected(playerUUID, itemId)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This item is protected and cannot be broken by you.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            UUID playerUUID = event.getPlayer().getUniqueId();
            String itemId = event.getClickedBlock().getType().name(); // Use item type as the identifier

            if (DatabaseManager.isLocationProtected(playerUUID, itemId)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("This item is protected and cannot be interacted with by you.");
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item != null && item.hasItemMeta()) {
                UUID ownerUUID = player.getUniqueId();
                String itemId = item.getType().name(); // Use item type as the identifier

                if (DatabaseManager.isLocationProtected(ownerUUID, itemId)) {
                    event.setCancelled(true);
                    player.sendMessage("You cannot damage this protected item.");
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (org.bukkit.block.Block block : event.blockList()) {
            String itemId = block.getType().name(); // Use item type as the identifier

            if (DatabaseManager.isLocationProtected(null, itemId)) { // Assuming no specific owner check for explosions
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().hasItemMeta()) {
            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().hasItemMeta()) {
            UUID ownerUUID = event.getPlayer().getUniqueId();
            String itemId = event.getItem().getItemStack().getType().name(); // Use item type as the identifier

            if (DatabaseManager.isLocationProtected(ownerUUID, itemId)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You cannot pick up this protected item.");
            }
        }
    }

    private long getProtectionDurationMillis() {
        FileConfiguration config = Main.getInstance().getConfig();
        int protectionTimeSeconds = config.getInt("protection_time_seconds", 300); // Default to 300 seconds (5 minutes)
        return protectionTimeSeconds * 1000; // Convert seconds to milliseconds
    }
}
