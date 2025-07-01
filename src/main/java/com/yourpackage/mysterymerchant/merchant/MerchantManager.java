package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MerchantManager {

    private final MysteryMerchant plugin;
    private Location spawnLocation;
    private Merchant activeMerchant;
    private final ItemManager itemManager;
    private BukkitTask despawnTask; // The manager now owns the despawn task

    public MerchantManager(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = new ItemManager(plugin);
        loadSpawnLocation();
    }

    private void loadSpawnLocation() {
        FileConfiguration config = plugin.getConfig();
        if (config.contains("merchant.spawn-location")) {
            this.spawnLocation = config.getLocation("merchant.spawn-location");
        } else {
            this.spawnLocation = null;
        }
    }

    public void setSpawnLocation(Location location) {
        this.spawnLocation = location;
        plugin.getConfig().set("merchant.spawn-location", location);
        plugin.saveConfig();
    }

    public boolean spawnMerchant() {
        if (spawnLocation == null) {
            plugin.getLogger().warning("Cannot spawn merchant: spawn location not set.");
            return false;
        }
        if (isMerchantActive()) {
            plugin.getLogger().warning("A merchant is already active.");
            return false;
        }

        this.activeMerchant = new Merchant(this, spawnLocation);
        this.activeMerchant.spawn();
        
        // Start the despawn timer here
        startDespawnTimer();

        String spawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-spawn", "&5A Mysterious Merchant has appeared!"));
        Bukkit.broadcastMessage(spawnMessage);
        return true;
    }

    public void despawnMerchant() {
        if (!isMerchantActive()) return;
        
        // Cancel the task if it's still running (for manual despawns)
        if (despawnTask != null && !despawnTask.isCancelled()) {
            despawnTask.cancel();
        }
        
        activeMerchant.despawn(true);
        this.activeMerchant = null;

        String despawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-despawn", "&5The Mysterious Merchant has vanished..."));
        Bukkit.broadcastMessage(despawnMessage);
    }
    
    // FIXED: The despawn timer logic is now owned by the manager
    private void startDespawnTimer() {
        long duration = plugin.getConfig().getLong("merchant.duration-minutes", 5) * 60 * 20;
        this.despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                // We check if a merchant is active before trying to despawn
                if (isMerchantActive()) {
                    plugin.getLogger().info("Mystery Merchant despawning automatically.");
                    despawnMerchant();
                }
            }
        }.runTaskLater(plugin, duration);
    }

    public boolean isMerchantActive() {
        return activeMerchant != null && activeMerchant.isSpawned();
    }
    
    public Merchant getActiveMerchant() {
        return activeMerchant;
    }
    
    public ItemManager getItemManager() {
        return itemManager;
    }
    
    public MysteryMerchant getPlugin() {
        return plugin;
    }
}
