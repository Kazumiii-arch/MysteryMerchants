package com.yourpackage.mysterymerchant.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.yourpackage.mysterymerchant.MysteryMerchant;

public class EconomyManager {

    private final MysteryMerchant plugin;
    private Economy economy = null;

    public EconomyManager(MysteryMerchant plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    private boolean setupEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found! Economy features will be disabled.");
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            plugin.getLogger().warning("No economy plugin found! Make sure you have an economy plugin (like EssentialsX) installed.");
            return false;
        }
        economy = rsp.getProvider();
        plugin.getLogger().info("Successfully hooked into Vault and found an economy provider!");
        return economy != null;
    }

    public boolean hasEconomy() {
        return economy != null;
    }

    public boolean hasEnough(Player player, double amount) {
        if (!hasEconomy()) return false;
        return economy.has(player, amount);
    }

    public boolean withdraw(Player player, double amount) {
        if (!hasEconomy()) return false;
        
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (response.transactionSuccess()) {
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "Transaction failed: " + response.errorMessage);
            return false;
        }
    }

    public String format(double amount) {
        if (!hasEconomy()) return String.valueOf(amount);
        return economy.format(amount);
    }
  }
