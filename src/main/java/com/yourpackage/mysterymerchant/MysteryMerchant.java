package com.yourpackage.mysterymerchant.api;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.Merchant;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPIExpansion extends PlaceholderExpansion {

    private final MysteryMerchant plugin;

    public PAPIExpansion(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mysterymerchant";
    }

    @Override
    public @NotNull String getAuthor() {
        return "YourName";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // This is required for the expansion to be registered
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("is_spawned")) {
            boolean isSpawned = plugin.getMerchantManager().isMerchantActive();
            return isSpawned ? "Yes" : "No";
        }

        if (params.equalsIgnoreCase("time_remaining")) {
            Merchant merchant = plugin.getMerchantManager().getActiveMerchant();
            if (merchant != null && merchant.isSpawned()) {
                long remaining = merchant.getRemainingSeconds();
                long minutes = remaining / 60;
                long seconds = remaining % 60;
                return String.format("%02d:%02d", minutes, seconds);
            } else {
                return "00:00";
            }
        }
        
        if (params.equalsIgnoreCase("status")) {
            boolean isSpawned = plugin.getMerchantManager().isMerchantActive();
            return isSpawned ? ChatColor.GREEN + "Spawned" : ChatColor.RED + "Not Spawned";
        }

        return null; // Placeholder not found
    }
}
