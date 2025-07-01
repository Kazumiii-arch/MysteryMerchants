package com.yourpackage.mysterymerchant.ui;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.ItemManager;
import com.yourpackage.mysterymerchant.merchant.MerchantItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class EditorGUI implements Listener {

    private final MysteryMerchant plugin;
    private final ItemManager itemManager;
    private Inventory gui;
    private Inventory editGui;
    private int editingSlot = -1;

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        gui = Bukkit.createInventory(null, 54, "Mystery Merchant Editor");
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
                // Apply rarity color to the item name
                meta.setDisplayName(getRarityColor(merchantItem.getRarity()) + ChatColor.stripColor(meta.getDisplayName()));
                
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + "$" + merchantItem.getPrice());
                lore.add(getRarityColor(merchantItem.getRarity()) + "Rarity: " + ChatColor.WHITE + merchantItem.getRarity());
                lore.add("");
                lore.add(ChatColor.YELLOW + "▶ Left-click to edit this item.");
                lore.add(ChatColor.RED + "✖ Right-click to remove.");
                meta.setLore(lore);
                displayItem.setItemMeta(meta);
            }
            gui.setItem(i, displayItem);
        }
    }

    private void openItemEditor(Player player, int slot) {
        this.editingSlot = slot;
        MerchantItem merchantItem = itemManager.getMerchantItems().get(slot);
        
        editGui = Bukkit.createInventory(null, 27, "Editing Item...");
        
        editGui.setItem(4, merchantItem.getItemStack());

        editGui.setItem(11, createControlButton(Material.GOLD_INGOT, ChatColor.GOLD + "Price: $" + merchantItem.getPrice(), 
            Arrays.asList(ChatColor.GREEN + "Left-click to increase by 10", ChatColor.RED + "Right-click to decrease by 10"), true));
        
        editGui.setItem(15, createControlButton(Material.AMETHYST_SHARD, getRarityColor(merchantItem.getRarity()) + "Rarity: " + merchantItem.getRarity(), 
            Arrays.asList(ChatColor.YELLOW + "Click to cycle rarity."), true));

        editGui.setItem(26, createControlButton(Material.OAK_DOOR, ChatColor.RED + "Back to Main Editor", null, false));
        
        player.openInventory(editGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        
        if (event.getInventory().equals(gui)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            int slot = event.getSlot();
            if (event.isLeftClick()) {
                openItemEditor(player, slot);
            } else if (event.isRightClick()) {
                itemManager.removeItem(slot);
                player.sendMessage(ChatColor.GREEN + "Item removed.");
                populateMainItems();
            }
        }
        
        else if (event.getInventory().equals(editGui)) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            MerchantItem itemToEdit = itemManager.getMerchantItems().get(editingSlot);
            
            switch(event.getSlot()) {
                case 11:
                    double currentPrice = itemToEdit.getPrice();
                    if (event.isLeftClick()) itemToEdit.setPrice(currentPrice + 10);
                    else if (event.isRightClick()) itemToEdit.setPrice(Math.max(0, currentPrice - 10));
                    break;
                case 15:
                    itemToEdit.setRarity(getNextRarity(itemToEdit.getRarity()));
                    break;
                case 26:
                    itemManager.saveItems();
                    open(player);
                    return;
            }
            
            itemManager.updateItem(editingSlot, itemToEdit);
            openItemEditor(player, editingSlot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(gui) || event.getInventory().equals(editGui)) {
            HandlerList.unregisterAll(this);
        }
    }

    private ItemStack createControlButton(Material material, String name, List<String> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        if (enchanted) {
            meta.addEnchant(Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
        return item;
    }

    private ChatColor getRarityColor(String rarity) {
        switch (rarity.toLowerCase()) {
            case "legendary": return ChatColor.GOLD;
            case "epic": return ChatColor.LIGHT_PURPLE;
            case "rare": return ChatColor.AQUA;
            default: return ChatColor.GRAY; // Common
        }
    }

    private String getNextRarity(String currentRarity) {
        switch (currentRarity.toLowerCase()) {
            case "common": return "Rare";
            case "rare": return "Epic";
            case "epic": return "Legendary";
            default: return "Common";
        }
    }
                        }
