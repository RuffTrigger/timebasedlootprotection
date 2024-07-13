package org.rufftrigger.timebasedlootprotection;

import org.bukkit.Bukkit;
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

            // Run protection logic asynchronously
            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
                DatabaseManager.protectItem(ownerUUID, itemId, protectionDurationMillis);
            });
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Run protection check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            UUID playerUUID = event.getPlayer().getUniqueId();
            String itemId = event.getBlock().getType().name(); // Use item type as the identifier

            if (DatabaseManager.isLocationProtected(playerUUID, itemId)) {
                // Run cancellation on the main thread
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("This item is protected and cannot be broken by you.");
                });
            }
        });
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Run interaction check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            if (event.getClickedBlock() != null) {
                UUID playerUUID = event.getPlayer().getUniqueId();
                String itemId = event.getClickedBlock().getType().name(); // Use item type as the identifier

                if (DatabaseManager.isLocationProtected(playerUUID, itemId)) {
                    // Run cancellation on the main thread
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("This item is protected and cannot be interacted with by you.");
                    });
                }
            }
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Run damage check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item != null && item.hasItemMeta()) {
                    UUID ownerUUID = player.getUniqueId();
                    String itemId = item.getType().name(); // Use item type as the identifier

                    if (DatabaseManager.isLocationProtected(ownerUUID, itemId)) {
                        // Run cancellation on the main thread
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            event.setCancelled(true);
                            player.sendMessage("You cannot damage this protected item.");
                        });
                    }
                }
            }
        });
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        // Run explosion check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            Collection<Entity> affectedEntities = event.getEntity().getWorld().getNearbyEntities(event.getLocation(), 2, 2, 2);
            for (Entity entity : affectedEntities) {
                if (entity instanceof ItemFrame) {
                    ItemFrame itemFrame = (ItemFrame) entity;
                    ItemStack item = itemFrame.getItem();
                    if (item != null && item.hasItemMeta()) {
                        // You might want to adjust this logic based on how you define protection
                        // For example, check the owner of the item or the item type
                        // Run cancellation on the main thread
                        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                            event.setCancelled(true);
                        });
                        return; // No need to continue checking if one protected item is affected
                    }
                }
            }
        });
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        // Run item spawn check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            ItemStack item = event.getEntity().getItemStack();
            if (item != null && item.hasItemMeta()) {
                // You may adjust the protection logic based on your plugin's requirements
                // Run cancellation on the main thread
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                    event.setCancelled(true); // Prevent the item from spawning if it's protected
                });
            }
        });
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        // Run pickup check asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            ItemStack item = event.getItem().getItemStack();
            if (item != null && item.hasItemMeta()) {
                UUID ownerUUID = event.getPlayer().getUniqueId();
                String itemId = item.getType().name(); // Use item type as the identifier

                if (DatabaseManager.isLocationProtected(ownerUUID, itemId)) {
                    // Run cancellation on the main thread
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("You cannot pick up this protected item.");
                    });
                }
            }
        });
    }
}
