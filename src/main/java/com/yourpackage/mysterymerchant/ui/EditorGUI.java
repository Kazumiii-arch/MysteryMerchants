package com.yourpackage.mysterymerchant.ui;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

public class EditorGUI implements Listener {

    private final MysteryMerchant plugin;
    private final ItemManager itemManager;
    private Inventory gui;

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
    }
    
    public void open(Player player) {
        gui = Bukkit.createInventory(null, 54, "Mystery Merchant Editor");
        populateItems();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(gui);
    }

    private void populateItems() {
        gui.clear();
        List<ItemStack> items = itemManager.getMerchantItems();
        for (int i = 0; i < items.size() && i < 54; i++) {
            ItemStack item = items.get(i).clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.RED + "Right-click to remove!");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != gui) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

        if (event.isRightClick()) {
            itemManager.removeItem(event.getSlot());
            ((Player) event.getWhoClicked()).sendMessage(ChatColor.GREEN + "Item removed.");
            populateItems();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() != gui) return;
        
        List<ItemStack> finalItems = new ArrayList<>();
        for (ItemStack item : gui.getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemStack cleanItem = item.clone();
                ItemMeta meta = cleanItem.getItemMeta();
                if (meta != null && meta.hasLore()) {
                    List<String> newLore = new ArrayList<>(meta.getLore());
                    newLore.removeIf(line -> line.contains("Right-click to remove!"));
                    if (newLore.isEmpty() || (newLore.size() == 1 && newLore.get(0).isEmpty())) {
                         meta.setLore(null);
                    } else {
                         meta.setLore(newLore);
                    }
                    cleanItem.setItemMeta(meta);
                }
                finalItems.add(cleanItem);
            }
        }
        itemManager.setMerchantItems(finalItems);
        // Unregister the listener to prevent memory leaks. This is the fix.
        HandlerList.unregisterAll(this);
    }
    }

