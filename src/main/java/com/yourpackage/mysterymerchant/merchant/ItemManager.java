package com.yourpackage.mysterymerchant.merchant;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemManager {

    private final MysteryMerchant plugin;
    private final List<ItemStack> merchantItems;

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
                ItemStack item = config.getItemStack("merchant.items." + key + ".item");
                if (item != null) {
                    merchantItems.add(item);
                }
            }
        }
    }

    public void saveItems() {
        FileConfiguration config = plugin.getConfig();
        config.set("merchant.items", null); 
        for (int i = 0; i < merchantItems.size(); i++) {
            config.set("merchant.items." + i + ".item", merchantItems.get(i));
        }
        plugin.saveConfig();
    }

    public void addItem(ItemStack item) {
        if (item != null && !item.getType().isAir()) {
            merchantItems.add(item.clone());
            saveItems();
        }
    }

    public void removeItem(int index) {
        if (index >= 0 && index < merchantItems.size()) {
            merchantItems.remove(index);
            saveItems();
        }
    }

    public List<ItemStack> getMerchantItems() {
        return new ArrayList<>(merchantItems);
    }
    
    public void setMerchantItems(List<ItemStack> items) {
        this.merchantItems.clear();
        this.merchantItems.addAll(items);
        saveItems();
    }
}

