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
    private Inventory mainGui;
    private Inventory editGui;
    private int currentlyEditingItemIndex = -1;

    public EditorGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
        this.itemManager = plugin.getMerchantManager().getItemManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', "&5&lMystery Merchant &8- &dItem Library");
        mainGui = Bukkit.createInventory(null, 54, title);
        
        fillBorders(mainGui);
        populateMainItems();
        
        player.openInventory(mainGui);
    }

    private void populateMainItems() {
        List<MerchantItem> items = itemManager.getMerchantItems();
        int itemIndex = 0;

        for (int slot = 0; slot < mainGui.getSize(); slot++) {
            if (mainGui.getItem(slot) == null) {
                if (itemIndex < items.size()) {
                    MerchantItem merchantItem = items.get(itemIndex);
                    ItemStack displayItem = merchantItem.getItemStack().clone();
                    ItemMeta meta = displayItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(getRarityColor(merchantItem.getRarity()) + ChatColor.stripColor(meta.getDisplayName()));
                        
                        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                        lore.add("");
                        lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + plugin.getEconomyManager().format(merchantItem.getPrice()));
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
                    mainGui.setItem(slot, displayItem);
                    itemIndex++;
                } else {
                    break;
                }
            }
        }
    }
    
    private void fillBorders(Inventory inv) {
        ItemStack darkPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta darkMeta = darkPane.getItemMeta();
        darkMeta.setDisplayName(" ");
        darkPane.setItemMeta(darkMeta);

        ItemStack lightPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta lightMeta = lightPane.getItemMeta();
        lightMeta.setDisplayName(" ");
        lightPane.setItemMeta(lightMeta);

        for (int i = 0; i < inv.getSize(); i++) {
            if (i < 9 || i >= inv.getSize() - 9 || i % 9 == 0 || (i + 1) % 9 == 0) {
                 if (i % 2 == 0) {
                    inv.setItem(i, lightPane);
                 } else {
                    inv.setItem(i, darkPane);
                 }
            }
        }
        
        if (inv.equals(mainGui)) {
            inv.setItem(48, createControlButton(Material.EMERALD, ChatColor.GREEN + "How to Add Items", Arrays.asList(ChatColor.GRAY + "Hold an item in your hand and type:", ChatColor.WHITE + "/mm additem"), false));
            inv.setItem(50, createControlButton(Material.BARRIER, ChatColor.RED + "" + ChatColor.BOLD + "Close Editor", null, false));
        }
    }

    public void openItemEditor(Player player, int itemIndex) {
        this.currentlyEditingItemIndex = itemIndex;
        MerchantItem merchantItem = itemManager.getMerchantItems().get(itemIndex);
        
        editGui = Bukkit.createInventory(null, 54, "Editing: " + ChatColor.stripColor(merchantItem.getItemStack().getItemMeta().getDisplayName()));
        fillBorders(editGui);
        
        editGui.setItem(13, merchantItem.getItemStack());

        editGui.setItem(20, createControlButton(Material.NAME_TAG, ChatColor.GREEN + "Rename Item", Arrays.asList(ChatColor.GRAY + "Click to type a new name in chat."), false));
        editGui.setItem(29, createControlButton(Material.WRITABLE_BOOK, ChatColor.AQUA + "Add Lore Line", Arrays.asList(ChatColor.GRAY + "Click to type a lore line in chat."), false));
        editGui.setItem(38, createControlButton(Material.COMMAND_BLOCK, ChatColor.LIGHT_PURPLE + "Add Command", Arrays.asList(ChatColor.GRAY + "Click to type a command in chat.", ChatColor.GRAY + "Use %player% for player name."), false));

        editGui.setItem(24, createControlButton(Material.GOLD_INGOT, ChatColor.GOLD + "Price: " + plugin.getEconomyManager().format(merchantItem.getPrice()), Arrays.asList(ChatColor.GREEN + "Left-click: +$10", ChatColor.RED + "Right-click: -$10"), true));
        editGui.setItem(33, createControlButton(Material.AMETHYST_SHARD, getRarityColor(merchantItem.getRarity()) + "Rarity: " + merchantItem.getRarity(), Arrays.asList(ChatColor.YELLOW + "Click to cycle rarity."), true));
        editGui.setItem(42, createControlButton(Material.LAVA_BUCKET, ChatColor.RED + "Clear Lore/Commands", Arrays.asList(ChatColor.DARK_RED + "Left-click to clear ALL commands.", ChatColor.DARK_RED + "Right-click to clear ALL lore."), false));
        
        editGui.setItem(49, createControlButton(Material.OAK_DOOR, ChatColor.RED + "Back to Item Library", null, false));
        
        player.openInventory(editGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        boolean isMainGui = topInventory.equals(mainGui);
        boolean isEditGui = topInventory.equals(editGui);

        if (!isMainGui && !isEditGui) return;
        event.setCancelled(true);

        if (!topInventory.equals(event.getClickedInventory())) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        Player player = (Player) event.getWhoClicked();

        if (isMainGui) {
            handleMainGuiClick(event, player);
        } else if (isEditGui) {
            handleEditGuiClick(event, player);
        }
    }

    private void handleMainGuiClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem().getType().name().endsWith("_PANE") || event.getSlot() >= 45) {
            if (event.getSlot() == 50) player.closeInventory();
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
        
        int itemIndex = 0;
        for (int i = 9; i < event.getSlot(); i++) {
            if (i % 9 != 0 && (i + 1) % 9 != 0 && mainGui.getItem(i) != null) {
                itemIndex++;
            }
        }

        if(event.getClick() == ClickType.LEFT) {
            openItemEditor(player, itemIndex);
        } else if (event.getClick() == ClickType.RIGHT) {
            itemManager.removeItem(itemIndex);
            player.sendMessage(ChatColor.GREEN + "Item removed.");
            open(player);
        }
    }
    
    private void handleEditGuiClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem().getType().name().endsWith("_PANE") || event.getSlot() == 13) {
            if (event.getSlot() == 49) {
                itemManager.saveItems();
                open(player);
            }
            return;
        }

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
        MerchantItem itemToEdit = itemManager.getMerchantItems().get(currentlyEditingItemIndex);
        
        switch(event.getSlot()) {
            case 20: // Rename
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please type the new item name in chat. Use '&' for colors. Type 'cancel' to abort.");
                plugin.setPlayerInEditMode(player, "rename", currentlyEditingItemIndex);
                return;
            case 29: // Add Lore
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please type the new lore line in chat. Type 'cancel' to abort.");
                plugin.setPlayerInEditMode(player, "addlore", currentlyEditingItemIndex);
                return;
            case 38: // Add Command
                player.closeInventory();
                player.sendMessage(ChatColor.YELLOW + "Please type the command to add (without '/'). Use %player%. Type 'cancel' to abort.");
                plugin.setPlayerInEditMode(player, "addcommand", currentlyEditingItemIndex);
                return;
            case 24: // Price
                double currentPrice = itemToEdit.getPrice();
                if (event.isLeftClick()) itemToEdit.setPrice(currentPrice + 10);
                else if (event.isRightClick()) itemToEdit.setPrice(Math.max(0, currentPrice - 10));
                break;
            case 33: // Rarity
                itemToEdit.setRarity(getNextRarity(itemToEdit.getRarity()));
                break;
            case 42: // Clear Lore/Commands
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
            default:
                return;
        }
        itemManager.updateItem(currentlyEditingItemIndex, itemToEdit);
        openItemEditor(player, currentlyEditingItemIndex);
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
