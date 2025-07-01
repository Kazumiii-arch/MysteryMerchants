package com.yourpackage.mysterymerchant.ui;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.ItemManager;
import com.yourpackage.mysterymerchant.merchant.MerchantItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class EditorGUI implements Listener {

    private final MysteryMerchant plugin;
    private final ItemManager itemManager;
    private Inventory gui;
    private Inventory editGui; // The GUI for editing a single item
    private int editingSlot = -1; // The slot of the item being edited

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        gui = Bukkit.createInventory(null, 54, "Mystery Merchant Editor - Click to Edit");
        populateMainItems();
        player.openInventory(gui);
    }

    private void populateMainItems() {
        gui.clear();
        List<MerchantItem> items = itemManager.getMerchantItems();
        for (int i = 0; i < items.size() && i < 54; i++) {
            MerchantItem merchantItem = items.get(i);
            ItemStack displayItem = merchantItem.getItemStack().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + "$" + merchantItem.getPrice());
                lore.add(ChatColor.AQUA + "Rarity: " + ChatColor.WHITE + merchantItem.getRarity());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Left-click to edit this item.");
                lore.add(ChatColor.RED + "Right-click to remove.");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.setItem(i, displayItem);
        }
    }

    private void openItemEditor(Player player, int slot) {
        this.editingSlot = slot;
        MerchantItem merchantItem = itemManager.getMerchantItems().get(slot);
        
        editGui = Bukkit.createInventory(null, 27, "Editing: " + merchantItem.getItemStack().getType().name());
        
        // Add the item itself for reference
        editGui.setItem(4, merchantItem.getItemStack());

        // Price controls
        editGui.setItem(11, createControlButton(Material.GOLD_NUGGET, ChatColor.GOLD + "Price: $" + merchantItem.getPrice(), 
            Arrays.asList(ChatColor.GREEN + "Left-click: +$10", ChatColor.RED + "Right-click: -$10")));
        
        // Rarity controls
        editGui.setItem(15, createControlButton(Material.DIAMOND, ChatColor.AQUA + "Rarity: " + merchantItem.getRarity(), 
            Arrays.asList(ChatColor.YELLOW + "Click to cycle rarity.")));

        // Back button
        editGui.setItem(26, createControlButton(Material.BARRIER, ChatColor.RED + "Back to Main Editor", null));
        
        player.openInventory(editGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        
        // --- Main Editor GUI Logic ---
        if (event.getInventory().equals(gui)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

            int slot = event.getSlot();
            if (event.isLeftClick()) {
                // Open the specific item editor
                openItemEditor(player, slot);
            } else if (event.isRightClick()) {
                // Remove the item
                itemManager.removeItem(slot);
                player.sendMessage(ChatColor.GREEN + "Item removed.");
                populateMainItems(); // Refresh the view
            }
        }
        
        // --- Item Specific Editor GUI Logic ---
        else if (event.getInventory().equals(editGui)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

            MerchantItem itemToEdit = itemManager.getMerchantItems().get(editingSlot);
            
            switch(event.getSlot()) {
                case 11: // Price control
                    double currentPrice = itemToEdit.getPrice();
                    if (event.isLeftClick()) {
                        itemToEdit.setPrice(currentPrice + 10);
                    } else if (event.isRightClick()) {
                        itemToEdit.setPrice(Math.max(0, currentPrice - 10));
                    }
                    break;
                case 15: // Rarity control
                    itemToEdit.setRarity(getNextRarity(itemToEdit.getRarity()));
                    break;
                case 26: // Back button
                    itemManager.saveItems(); // Save changes before going back
                    open(player); // Re-open the main editor
                    return;
            }
            
            // Update the item and refresh the edit GUI
            itemManager.updateItem(editingSlot, itemToEdit);
            openItemEditor(player, editingSlot); // Re-open to show updated values
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) || event.getInventory().equals(editGui)) {
            HandlerList.unregisterAll(this);
        }
    }

    private ItemStack createControlButton(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) {
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
        return item;
    }

    private String getNextRarity(String currentRarity) {
        switch (currentRarity.toLowerCase()) {
            case "common": return "Rare";
            case "rare": return "Epic";
            case "epic": return "Legendary";
            case "legendary": return "Common";
            default: return "Common";
        }
    }
}
