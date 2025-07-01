package com.yourpackage.mysterymerchant.merchant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor; // NEW
import org.bukkit.boss.BarStyle; // NEW
import org.bukkit.boss.BossBar; // NEW
import org.bukkit.entity.Player; // NEW
// ... (other imports)

public class MerchantManager {

    // ... (plugin, location, merchant, itemManager fields are unchanged) ...
    private BukkitTask despawnTask;
    private BossBar bossBar; // NEW

    // ... (constructor, loadSpawnLocation, setSpawnLocation are unchanged) ...

    public boolean spawnMerchant() {
        // ... (existing spawn logic is unchanged up to spawning the entity) ...
        
        this.activeMerchant = new Merchant(this, spawnLocation);
        this.activeMerchant.setCurrentStock(generateRandomizedStock());
        this.activeMerchant.spawn();
        
        startDespawnTimer();
        
        // NEW: Create and show the Boss Bar
        createBossBar();

        // ... (broadcast message is unchanged) ...
        return true;
    }

    public void despawnMerchant() {
        if (!isMerchantActive()) return;
        
        if (despawnTask != null && !despawnTask.isCancelled()) {
            despawnTask.cancel();
        }
        
        // NEW: Remove the Boss Bar
        removeBossBar();
        
        activeMerchant.despawn(true);
        this.activeMerchant = null;

        // ... (broadcast message is unchanged) ...
    }
    
    private void startDespawnTimer() {
        long duration = plugin.getConfig().getLong("merchant.duration-minutes", 5) * 60 * 20;
        this.despawnTask = new BukkitRunnable() {
            int ticksRemaining = (int) (duration / 20);
            @Override
            public void run() {
                // NEW: Update the Boss Bar progress
                if (bossBar != null) {
                    double progress = (double) activeMerchant.getRemainingSeconds() / (plugin.getConfig().getLong("merchant.duration-minutes", 5) * 60);
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                }
                
                if (--ticksRemaining <= 0) {
                    if (isMerchantActive()) {
                        plugin.getLogger().info("Mystery Merchant despawning automatically.");
                        despawnMerchant();
                    }
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }

    // --- NEW: Methods to manage the Boss Bar ---
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
        
        // Add all online players to the boss bar
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

    // ... (rest of the file is unchanged) ...
}
