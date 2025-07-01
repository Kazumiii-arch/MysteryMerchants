package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
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
    private BossBar bossBar;

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
        this.activeMerchant.setCurrentStock(generateRandomizedStock());
        this.activeMerchant.spawn();
        
        startDespawnTimer();
        createBossBar();

        String spawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-spawn", "&5A Mysterious Merchant has appeared!"));
        Bukkit.broadcastMessage(spawnMessage);
        return true;
    }
    
    private List<MerchantItem> generateRandomizedStock() {
        FileConfiguration config = plugin.getConfig();
        if (!config.getBoolean("shop-randomization.enabled", true)) {
            return itemManager.getMerchantItems();
        }

        List<MerchantItem> allPossibleItems = itemManager.getMerchantItems();
        List<MerchantItem> randomizedStock = new ArrayList<>();
        Random random = new Random();
        Collections.shuffle(allPossibleItems);
        int maxItems = config.getInt("shop-randomization.max-items-per-spawn", 12);

        for (MerchantItem item : allPossibleItems) {
            if (randomizedStock.size() >= maxItems) break;

            double chance;
            switch (item.getRarity().toLowerCase()) {
                case "legendary": chance = config.getDouble("shop-randomization.chance-legendary", 0.10); break;
                case "epic": chance = config.getDouble("shop-randomization.chance-epic", 0.25); break;
                case "rare": chance = config.getDouble("shop-randomization.chance-rare", 0.50); break;
                default: chance = config.getDouble("shop-randomization.chance-common", 0.85); break;
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
        
        removeBossBar();
        activeMerchant.despawn(true);
        this.activeMerchant = null;

        String despawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-despawn", "&5The Mysterious Merchant has vanished..."));
        Bukkit.broadcastMessage(despawnMessage);
    }
    
    private void startDespawnTimer() {
        long durationSeconds = plugin.getConfig().getLong("merchant.duration-minutes", 5) * 60;
        this.despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isMerchantActive()) {
                    this.cancel();
                    return;
                }

                if (bossBar != null) {
                    double progress = (double) activeMerchant.getRemainingSeconds() / durationSeconds;
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                }

                if (activeMerchant.getRemainingSeconds() <= 0) {
                    plugin.getLogger().info("Mystery Merchant despawning automatically.");
                    despawnMerchant();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void createBossBar() {
        if (!plugin.getConfig().getBoolean("boss-bar.enabled", true)) return;

        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("boss-bar.title", "&5&lA Mysterious Merchant has arrived!"));
        BarColor color;
        try {
            color = BarColor.valueOf(plugin.getConfig().getString("boss-bar.color", "PURPLE").toUpperCase());
        } catch (IllegalArgumentException e) {
            color = BarColor.PURPLE;
            plugin.getLogger().warning("Invalid Boss Bar color in config.yml! Defaulting to PURPLE.");
        }

        bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        bossBar.setProgress(1.0);
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
    }

    private void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
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
