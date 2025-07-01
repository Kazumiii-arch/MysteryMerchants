package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class MerchantManager {

    private final MysteryMerchant plugin;
    private Location spawnLocation;
    private Merchant activeMerchant;
    private final ItemManager itemManager;

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
        
        String spawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-spawn", "&5A Mysterious Merchant has appeared!"));
        Bukkit.broadcastMessage(spawnMessage);
        return true;
    }

    public void despawnMerchant() {
        if (!isMerchantActive()) return;
        
        activeMerchant.despawn(true);
        this.activeMerchant = null;

        String despawnMessage = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.merchant-despawn", "&5The Mysterious Merchant has vanished..."));
        Bukkit.broadcastMessage(despawnMessage);
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

