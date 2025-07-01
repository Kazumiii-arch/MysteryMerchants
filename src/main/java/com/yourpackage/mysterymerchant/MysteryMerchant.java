package com.yourpackage.mysterymerchant;

import org.bukkit.plugin.java.JavaPlugin;
import com.yourpackage.mysterymerchant.commands.CommandManager;
import com.yourpackage.mysterymerchant.listeners.PlayerInteractionListener;
import com.yourpackage.mysterymerchant.merchant.Merchant;
import com.yourpackage.mysterymerchant.merchant.MerchantManager;
import java.util.Objects;

public final class MysteryMerchant extends JavaPlugin {

    private static MysteryMerchant instance;
    private MerchantManager merchantManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // This will create the config.yml from your resources
        merchantManager = new MerchantManager(this);

        try {
            Objects.requireNonNull(getCommand("mysterymerchant")).setExecutor(new CommandManager(this));
        } catch (NullPointerException e) {
            getLogger().severe("Could not register the 'mysterymerchant' command! Please ensure it is defined in plugin.yml.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new PlayerInteractionListener(this), this);
        getLogger().info("MysteryMerchant has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        Merchant merchant = merchantManager.getActiveMerchant();
        if (merchant != null && merchant.isSpawned()) {
            merchant.despawn(false);
            getLogger().info("Mystery Merchant has been despawned due to server shutdown.");
        }
        saveConfig();
        getLogger().info("MysteryMerchant has been disabled.");
    }

    public static MysteryMerchant getInstance() {
        return instance;
    }

    public MerchantManager getMerchantManager() {
        return merchantManager;
    }
            }
