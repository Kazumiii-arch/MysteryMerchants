package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    private final MysteryMerchant plugin;
    private final List<MerchantItem> merchantItems;

    public ItemManager(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.merchantItems = new ArrayList<>();
        loadItems();
    }

    public void loadItems() {
        merchantItems.clear();
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection itemsSection = config.getConfigurationSection("merchant.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                String path = "merchant.items." + key;
                ItemStack item = config.getItemStack(path + ".item");
                double price = config.getDouble(path + ".price", 100.0);
                String rarity = config.getString(path + ".rarity", "Common");
                if (item != null) {
                    merchantItems.add(new MerchantItem(item, price, rarity));
                }
            }
        }
    }

    public void saveItems() {
        FileConfiguration config = plugin.getConfig();
        config.set("merchant.items", null); 
        for (int i = 0; i < merchantItems.size(); i++) {
            MerchantItem merchantItem = merchantItems.get(i);
            String path = "merchant.items." + i;
            config.set(path + ".item", merchantItem.getItemStack());
            config.set(path + ".price", merchantItem.getPrice());
            config.set(path + ".rarity", merchantItem.getRarity());
        }
        plugin.saveConfig();
    }

    public void addItem(ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            // Add with default price and rarity
            merchantItems.add(new MerchantItem(item.clone(), 100.0, "Common"));
            saveItems();
        }
    }
    
    public void updateItem(int index, MerchantItem item) {
        if (index >= 0 && index < merchantItems.size()) {
            merchantItems.set(index, item);
            saveItems();
        }
    }

    public void removeItem(int index) {
        if (index >= 0 && index < merchantItems.size()) {
            merchantItems.remove(index);
            saveItems();
        }
    }

    public List<MerchantItem> getMerchantItems() {
        return merchantItems;
    }
}
