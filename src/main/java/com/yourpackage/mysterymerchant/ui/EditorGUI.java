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
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class EditorGUI implements Listener {

    private final MysteryMerchant plugin;
    private final ItemManager itemManager;
    private Inventory gui;
    private Inventory editGui;
    private int editingSlot = -1;
    // Static reference to the item being edited so the chat listener can access it
    private static MerchantItem staticEditingItem;

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    // Static method for the chat listener to get the item
    public static MerchantItem getEditingItem() {
        return staticEditingItem;
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
                meta.setDisplayName(getRarityColor(merchantItem.getRarity()) + ChatColor.stripColor(meta.getDisplayName()));
                
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + "$" + merchantItem.getPrice());
                lore.add(getRarityColor(merchantItem.getRarity()) + "Rarity: " + ChatColor.WHITE + merchantItem.getRarity());
                
                if (merchantItem.getCommands() != null && !merchantItem.getCommands().isEmpty()) {
                    lore.add(ChatColor.LIGHT_PURPLE + "Commands: " + ChatColor.WHITE + merchantItem.getCommands().size());
                }

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
        staticEditingItem = merchantItem; // Update static reference
        
        editGui = Bukkit.createInventory(null, 36, "Editing Item...");
        
        editGui.setItem(4, merchantItem.getItemStack());

        // Row 2: Core editing
        editGui.setItem(11, createControlButton(Material.NAME_TAG, ChatColor.GREEN + "Rename Item", Arrays.asList(ChatColor.GRAY + "Click to type a new name in chat."), false));
        editGui.setItem(13, createControlButton(Material.GOLD_INGOT, ChatColor.GOLD + "Price: $" + merchantItem.getPrice(), Arrays.asList(ChatColor.GREEN + "+$10", ChatColor.RED + "-$10"), true));
        editGui.setItem(15, createControlButton(Material.WRITABLE_BOOK, ChatColor.AQUA + "Add Lore Line", Arrays.asList(ChatColor.GRAY + "Click to type a lore line in chat."), false));

        // Row 3: Advanced editing
        editGui.setItem(20, createControlButton(Material.COMMAND_BLOCK, ChatColor.LIGHT_PURPLE + "Add Command", Arrays.asList(ChatColor.GRAY + "Click to type a command in chat.", ChatColor.GRAY + "Use %player% for player name."), false));
        editGui.setItem(22, createControlButton(Material.AMETHYST_SHARD, getRarityColor(merchantItem.getRarity()) + "Rarity: " + merchantItem.getRarity(), Arrays.asList(ChatColor.YELLOW + "Click to cycle rarity."), true));
        editGui.setItem(24, createControlButton(Material.LAVA_BUCKET, ChatColor.RED + "Clear Lore/Commands", Arrays.asList(ChatColor.DARK_RED + "Left-click to clear ALL commands.", ChatColor.DARK_RED + "Right-click to clear ALL lore."), false));
        
        editGui.setItem(35, createControlButton(Material.OAK_DOOR, ChatColor.RED + "Back to Main Editor", null, false));
        
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
                case 11: // Rename
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the new item name in chat. Use '&' for colors. Type 'cancel' to abort.");
                    plugin.setPlayerEditing(player, "rename");
                    return;
                case 13: // Price
                    double currentPrice = itemToEdit.getPrice();
                    if (event.isLeftClick()) itemToEdit.setPrice(currentPrice + 10);
                    else if (event.isRightClick()) itemToEdit.setPrice(Math.max(0, currentPrice - 10));
                    break;
                case 15: // Add Lore
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the new lore line in chat. Type 'cancel' to abort.");
                    plugin.setPlayerEditing(player, "addlore");
                    return;
                case 20: // Add Command
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the command to add (without '/'). Use %player%. Type 'cancel' to abort.");
                    plugin.setPlayerEditing(player, "addcommand");
                    return;
                case 22: // Rarity
                    itemToEdit.setRarity(getNextRarity(itemToEdit.getRarity()));
                    break;
                case 24: // Clear Lore/Commands
                    if(event.isLeftClick()){
                        itemToEdit.getCommands().clear();
                        player.sendMessage(ChatColor.GREEN + "All commands for this item have been cleared.");
                    } else if (event.isRightClick()){
                        ItemMeta meta = itemToEdit.getItemStack().getItemMeta();
                        if(meta != null) {
                            meta.setLore(new ArrayList<>());
                            itemToEdit.getItemStack().setItemMeta(meta);
                        }
                        player.sendMessage(ChatColor.GREEN + "All lore for this item has been cleared.");
                    }
                    break;
                case 35: // Back button
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
        new BukkitRunnable() {
            @Override
            public void run() {
                Inventory currentOpen = event.getPlayer().getOpenInventory().getTopInventory();
                if (!currentOpen.equals(gui) && !currentOpen.equals(editGui)) {
                    HandlerList.unregisterAll(EditorGUI.this);
                    staticEditingItem = null; // Clear static reference
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    private ItemStack createControlButton(Material material, String name, List<String> lore, boolean enchanted) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        if (enchanted) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, false);
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
            default: return ChatColor.GRAY;
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
                                                
