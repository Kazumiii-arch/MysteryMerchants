package com.yourpackage.mysterymerchant;

import com.yourpackage.mysterymerchant.commands.CommandManager;
import com.yourpackage.mysterymerchant.economy.EconomyManager;
import com.yourpackage.mysterymerchant.listeners.PlayerChatListener;
import com.yourpackage.mysterymerchant.listeners.PlayerInteractionListener;
import com.yourpackage.mysterymerchant.merchant.MerchantManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MysteryMerchant extends JavaPlugin {

    private static MysteryMerchant instance;
    private MerchantManager merchantManager;
    private EconomyManager economyManager;
    
    // Maps to track what a player is currently editing via chat
    private final Map<UUID, String> playerEditModeMap = new HashMap<>();
    private final Map<UUID, Integer> playerEditingItemSlotMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
        // Initialize managers
        economyManager = new EconomyManager(this);
        merchantManager = new MerchantManager(this);

        try {
            Objects.requireNonNull(getCommand("mysterymerchant")).setExecutor(new CommandManager(this));
        } catch (NullPointerException e) {
            getLogger().severe("Could not register the 'mysterymerchant' command!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerInteractionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getLogger().info("MysteryMerchant has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (merchantManager != null && merchantManager.isMerchantActive()) {
            merchantManager.despawnMerchant();
            getLogger().info("Mystery Merchant has been despawned due to server shutdown.");
        }
        saveConfig();
        getLogger().info("MysteryMerchant has been disabled.");
    }

    // --- Methods to manage the player's editing state ---
    public void setPlayerInEditMode(Player player, String editType, int itemSlot) {
        playerEditModeMap.put(player.getUniqueId(), editType);
        playerEditingItemSlotMap.put(player.getUniqueId(), itemSlot);
    }

    public void removePlayerFromEditMode(Player player) {
        playerEditModeMap.remove(player.getUniqueId());
        playerEditingItemSlotMap.remove(player.getUniqueId());
    }

    public String getPlayerEditMode(Player player) {
        return playerEditModeMap.get(player.getUniqueId());
    }
    
    public Integer getPlayerEditingItemSlot(Player player) {
        return playerEditingItemSlotMap.get(player.getUniqueId());
    }
    
    public boolean isPlayerInEditMode(Player player) {
        return playerEditModeMap.containsKey(player.getUniqueId());
    }

    // --- Getters for main components ---
    public static MysteryMerchant getInstance() {
        return instance;
    }

    public MerchantManager getMerchantManager() {
        return merchantManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}
