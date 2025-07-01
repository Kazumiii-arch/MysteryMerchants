package com.yourpackage.mysterymerchant;

import com.yourpackage.mysterymerchant.commands.CommandManager;
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
    
    private final Map<UUID, String> playerEditorMap = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
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

    public void setPlayerEditing(Player player, String editType) {
        playerEditorMap.put(player.getUniqueId(), editType);
    }

    public void removePlayerEditing(Player player) {
        playerEditorMap.remove(player.getUniqueId());
    }

    public String getPlayerEditMode(Player player) {
        return playerEditorMap.get(player.getUniqueId());
    }
    
    public boolean isPlayerEditing(Player player) {
        return playerEditorMap.containsKey(player.getUniqueId());
    }

    public static MysteryMerchant getInstance() {
        return instance;
    }

    public MerchantManager getMerchantManager() {
        return merchantManager;
    }
}
