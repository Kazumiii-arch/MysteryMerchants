package com.yourpackage.mysterymerchant;

import com.yourpackage.mysterymerchant.api.PAPIExpansion;
import com.yourpackage.mysterymerchant.commands.CommandManager;
import com.yourpackage.mysterymerchant.economy.EconomyManager;
import com.yourpackage.mysterymerchant.listeners.PlayerChatListener;
import com.yourpackage.mysterymerchant.listeners.PlayerInteractionListener;
import com.yourpackage.mysterymerchant.merchant.MerchantManager;
import com.yourpackage.mysterymerchant.ui.EditorGUI; // NEW IMPORT
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class MysteryMerchant extends JavaPlugin {

    private static MysteryMerchant instance;
    private MerchantManager merchantManager;
    private EconomyManager economyManager;
    private BukkitTask autoSpawnTask;
    
    // --- NEW: Maps to manage editor state correctly ---
    private final Map<UUID, String> playerEditModeMap = new HashMap<>();
    private final Map<UUID, Integer> playerEditingItemSlotMap = new HashMap<>();
    private final Map<UUID, EditorGUI> openEditors = new HashMap<>(); // Tracks the active editor instance for each player

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        
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
        
        startAutoSpawnTask();
        
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

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

    public void startAutoSpawnTask() {
        if (autoSpawnTask != null) autoSpawnTask.cancel();
        if (!getConfig().getBoolean("autospawn.enabled", false)) return;
        long intervalHours = getConfig().getLong("autospawn.interval-hours", 4);
        long intervalTicks = intervalHours * 60 * 60 * 20;
        autoSpawnTask = getServer().getScheduler().runTaskTimer(this, () -> {
            if (!merchantManager.isMerchantActive()) {
                getLogger().info("Attempting to automatically spawn the Mystery Merchant...");
                merchantManager.spawnMerchant();
            }
        }, intervalTicks, intervalTicks);
        getLogger().info("Automatic merchant spawning enabled. Interval: " + intervalHours + " hours.");
    }

    // --- NEW: Updated methods to manage editor state ---
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
    
    // --- NEW: Methods to track the active EditorGUI instance ---
    public void playerOpenedEditor(Player player, EditorGUI editor) {
        openEditors.put(player.getUniqueId(), editor);
    }

    public void playerClosedEditor(Player player) {
        openEditors.remove(player.getUniqueId());
    }

    public EditorGUI getOpenEditorForPlayer(Player player) {
        return openEditors.get(player.getUniqueId());
    }

    // --- Getters for main components ---
    public static MysteryMerchant getInstance() { return instance; }
    public MerchantManager getMerchantManager() { return merchantManager; }
    public EconomyManager getEconomyManager() { return economyManager; }
            }
