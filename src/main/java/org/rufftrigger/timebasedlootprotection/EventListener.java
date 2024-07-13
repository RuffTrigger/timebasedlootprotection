package org.rufftrigger.timebasedlootprotection;

import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
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

import java.util.Collection;
import java.util.UUID;

public class EventListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta()) {
            UUID ownerUUID = event.getPlayer().getUniqueId();
            String itemId = item.getType().name(); // Use item type as the identifier

            long protectionDurationMillis = 60000; // Example: 1 minute protection

            DatabaseManager.protectItem(ownerUUID, itemId, protectionDurationMillis);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Check if the block broken is protected and cancel the event if needed
        UUID playerUUID = event.getPlayer().getUniqueId();
        String itemId = event.getBlock().getType().name(); // Use item type as the identifier

        if (DatabaseManager.isLocationProtected(playerUUID, itemId)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("This item is protected and cannot be broken by you.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player interacts with a protected item and cancel the event if needed
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
        // Check if the entity damages a protected item and cancel the event if needed
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
        // Check if the explosion affects protected items and cancel the event if needed
        Collection<Entity> affectedEntities = event.getEntity().getWorld().getNearbyEntities(event.getLocation(), 2, 2, 2);
        for (Entity entity : affectedEntities) {
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                ItemStack item = itemFrame.getItem();
                if (item != null && item.hasItemMeta()) {
                    // You might want to adjust this logic based on how you define protection
                    // For example, check the owner of the item or the item type
                    event.setCancelled(true);
                    break; // No need to continue checking if one protected item is affected
                }
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Check if an item spawns and protect it if needed
        ItemStack item = event.getEntity().getItemStack();
        if (item != null && item.hasItemMeta()) {
            // You may adjust the protection logic based on your plugin's requirements
            event.setCancelled(true); // Prevent the item from spawning if it's protected
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        // Check if a player picks up a protected item and cancel the event if needed
        ItemStack item = event.getItem().getItemStack();
        if (item != null && item.hasItemMeta()) {
            UUID ownerUUID = event.getPlayer().getUniqueId();
            String itemId = item.getType().name(); // Use item type as the identifier

            if (DatabaseManager.isLocationProtected(ownerUUID, itemId)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You cannot pick up this protected item.");
            }
        }
    }
}
