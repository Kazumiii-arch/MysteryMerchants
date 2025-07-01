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
import org.bukkit.event.inventory.ClickType;
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
    private int currentlyEditingSlot = -1;

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&5&lMystery Merchant &8- &dEditor");
        gui = Bukkit.createInventory(null, 54, title);
        
        fillBorders();
        populateMainItems();
        
        player.openInventory(gui);
    }

    private void populateMainItems() {
        List<MerchantItem> items = itemManager.getMerchantItems();
        int itemIndex = 0;

        for (int slot = 0; slot < gui.getSize(); slot++) {
            if (gui.getItem(slot) == null) {
                if (itemIndex < items.size()) {
                    MerchantItem merchantItem = items.get(itemIndex);
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
                    gui.setItem(slot, displayItem);
                    itemIndex++;
                } else {
                    break;
                }
            }
        }
    }
    
    private void fillBorders() {
        ItemStack darkPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta darkMeta = darkPane.getItemMeta();
        darkMeta.setDisplayName(" ");
        darkPane.setItemMeta(darkMeta);

        ItemStack lightPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta lightMeta = lightPane.getItemMeta();
        lightMeta.setDisplayName(" ");
        lightPane.setItemMeta(lightMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            if (i < 9 || i >= 45 || i % 9 == 0 || (i + 1) % 9 == 0) {
                 if (i % 2 == 0) {
                    gui.setItem(i, lightPane);
                 } else {
                    gui.setItem(i, darkPane);
                 }
            }
        }
        
        gui.setItem(48, createControlButton(Material.EMERALD, ChatColor.GREEN + "How to Add Items", Arrays.asList(ChatColor.GRAY + "Hold an item in your hand and type:", ChatColor.WHITE + "/mm additem"), false));
        gui.setItem(50, createControlButton(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Close Editor", null, false));
    }

    private void openItemEditor(Player player, int slot) {
        int itemIndex = 0;
        for (int i = 0; i < gui.getSize(); i++) {
            if(gui.getItem(i) != null && !gui.getItem(i).getType().name().endsWith("_PANE") && gui.getItem(i).getType() != Material.EMERALD && gui.getItem(i).getType() != Material.BARRIER) {
                if(i == slot) {
                    this.currentlyEditingSlot = itemIndex;
                    break;
                }
                itemIndex++;
            }
        }
        
        MerchantItem merchantItem = itemManager.getMerchantItems().get(currentlyEditingSlot);
        
        editGui = Bukkit.createInventory(null, 36, "Editing Item...");
        
        editGui.setItem(4, merchantItem.getItemStack());

        editGui.setItem(11, createControlButton(Material.NAME_TAG, ChatColor.GREEN + "Rename Item", Arrays.asList(ChatColor.GRAY + "Click to type a new name in chat."), false));
        editGui.setItem(13, createControlButton(Material.GOLD_INGOT, ChatColor.GOLD + "Price: $" + merchantItem.getPrice(), Arrays.asList(ChatColor.GREEN + "+$10", ChatColor.RED + "-$10"), true));
        editGui.setItem(15, createControlButton(Material.WRITABLE_BOOK, ChatColor.AQUA + "Add Lore Line", Arrays.asList(ChatColor.GRAY + "Click to type a lore line in chat."), false));

        editGui.setItem(20, createControlButton(Material.COMMAND_BLOCK, ChatColor.LIGHT_PURPLE + "Add Command", Arrays.asList(ChatColor.GRAY + "Click to type a command in chat.", ChatColor.GRAY + "Use %player% for player name."), false));
        editGui.setItem(22, createControlButton(Material.AMETHYST_SHARD, getRarityColor(merchantItem.getRarity()) + "Rarity: " + merchantItem.getRarity(), Arrays.asList(ChatColor.YELLOW + "Click to cycle rarity."), true));
        editGui.setItem(24, createControlButton(Material.LAVA_BUCKET, ChatColor.RED + "Clear Lore/Commands", Arrays.asList(ChatColor.DARK_RED + "Left-click to clear ALL commands.", ChatColor.DARK_RED + "Right-click to clear ALL lore."), false));
        
        editGui.setItem(35, createControlButton(Material.OAK_DOOR, ChatColor.RED + "Back to Main Editor", null, false));
        
        player.openInventory(editGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        
        // --- Main Editor GUI Logic ---
        if (event.getInventory().equals(gui)) {
            // FIXED: Prevent any interaction with the player's own inventory while the GUI is open
            if (clickedInventory != gui) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(true);
            
            if(event.getSlot() == 50) {
                player.closeInventory();
                return;
            }
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir() || event.getCurrentItem().getType().name().endsWith("_PANE")) return;

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            
            if(event.getClick() == ClickType.LEFT) {
                openItemEditor(player, event.getSlot());
            } else if (event.getClick() == ClickType.RIGHT) {
                 int itemIndex = 0;
                 for (int i = 0; i < gui.getSize(); i++) {
                    if(gui.getItem(i) != null && !gui.getItem(i).getType().name().endsWith("_PANE") && gui.getItem(i).getType() != Material.EMERALD && gui.getItem(i).getType() != Material.BARRIER) {
                        if(i == event.getSlot()) {
                            itemManager.removeItem(itemIndex);
                            player.sendMessage(ChatColor.GREEN + "Item removed.");
                            open(player); // Re-open to refresh
                            return;
                        }
                        itemIndex++;
                    }
                }
            }
        }
        
        // --- Item Specific Editor GUI Logic ---
        else if (event.getInventory().equals(editGui)) {
            // FIXED: Prevent any interaction with the player's own inventory
            if (clickedInventory != editGui) {
                event.setCancelled(true);
                return;
            }
            
            event.setCancelled(true);
            if (event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;

            // FIXED: Prevent clicking the display item in the center
            if (event.getSlot() == 4) {
                return;
            }

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            MerchantItem itemToEdit = itemManager.getMerchantItems().get(currentlyEditingSlot);
            
            switch(event.getSlot()) {
                case 11: // Rename
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the new item name in chat. Use '&' for colors. Type 'cancel' to abort.");
                    plugin.setPlayerInEditMode(player, "rename", currentlyEditingSlot);
                    return;
                case 13: // Price
                    double currentPrice = itemToEdit.getPrice();
                    if (event.isLeftClick()) itemToEdit.setPrice(currentPrice + 10);
                    else if (event.isRightClick()) itemToEdit.setPrice(Math.max(0, currentPrice - 10));
                    break;
                case 15: // Add Lore
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the new lore line in chat. Type 'cancel' to abort.");
                    plugin.setPlayerInEditMode(player, "addlore", currentlyEditingSlot);
                    return;
                case 20: // Add Command
                    player.closeInventory();
                    player.sendMessage(ChatColor.YELLOW + "Please type the command to add (without '/'). Use %player%. Type 'cancel' to abort.");
                    plugin.setPlayerInEditMode(player, "addcommand", currentlyEditingSlot);
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
            itemManager.updateItem(currentlyEditingSlot, itemToEdit);
            openItemEditor(player, currentlyEditingSlot);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!plugin.isPlayerInEditMode((Player) event.getPlayer())) {
            HandlerList.unregisterAll(this);
        }
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
