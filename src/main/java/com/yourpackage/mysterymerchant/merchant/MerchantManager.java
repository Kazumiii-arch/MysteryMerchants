package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MerchantManager {

    private final MysteryMerchant plugin;
    private Location spawnLocation;
    private Merchant activeMerchant;
    private final ItemManager itemManager;
    private BukkitTask despawnTask;

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
        
        // Generate the randomized stock for this specific merchant
        this.activeMerchant.setCurrentStock(generateRandomizedStock());
        
        this.activeMerchant.spawn();
        startDespawnTimer();

        String spawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-spawn", "&5A Mysterious Merchant has appeared!"));
        Bukkit.broadcastMessage(spawnMessage);
        return true;
    }

    // --- Method to generate the random inventory ---
    private List<MerchantItem> generateRandomizedStock() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("shop-randomization.enabled", true)) {
            // If randomization is disabled, return all items
            return itemManager.getMerchantItems();
        }

        List<MerchantItem> allPossibleItems = itemManager.getMerchantItems();
        List<MerchantItem> randomizedStock = new ArrayList<>();
        Random random = new Random();

        // Shuffle the list to ensure different items are checked first each time
        Collections.shuffle(allPossibleItems);

        int maxItems = config.getInt("shop-randomization.max-items-per-spawn", 12);

        for (MerchantItem item : allPossibleItems) {
            if (randomizedStock.size() >= maxItems) {
                break; // Stop if we've reached the max number of items
            }

            double chance = 0.0;
            switch (item.getRarity().toLowerCase()) {
                case "legendary":
                    chance = config.getDouble("shop-randomization.chance-legendary", 0.10);
                    break;
                case "epic":
                    chance = config.getDouble("shop-randomization.chance-epic", 0.25);
                    break;
                case "rare":
                    chance = config.getDouble("shop-randomization.chance-rare", 0.50);
                    break;
                default: // Common
                    chance = config.getDouble("shop-randomization.chance-common", 0.85);
                    break;
            }

            if (random.nextDouble() <= chance) {
                randomizedStock.add(item);
            }
        }
        return randomizedStock;
    }

    public void despawnMerchant() {
        if (!isMerchantActive()) return;
        
        if (despawnTask != null && !despawnTask.isCancelled()) {
            despawnTask.cancel();
        }
        
        activeMerchant.despawn(true);
        this.activeMerchant = null;

        String despawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-despawn", "&5The Mysterious Merchant has vanished..."));
        Bukkit.broadcastMessage(despawnMessage);
    }
    
    private void startDespawnTimer() {
        long duration = plugin.getConfig().getLong("merchant.duration-minutes", 5) * 60 * 20;
        this.despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
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
