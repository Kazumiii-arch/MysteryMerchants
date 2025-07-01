package com.yourpackage.mysterymerchant.ui;

import com.yourpackage.mysterymerchant.MysteryMerchant;
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
import java.util.ArrayList;
import java.util.List;

public class ShopGUI implements Listener {

    private final MysteryMerchant plugin;
    private Inventory gui;

    public ShopGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&5Mysterious Wares"));
        gui = Bukkit.createInventory(null, 54, title);
        populateShopItems();
        fillBorders();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(gui);
    }

    private void populateShopItems() {
        List<ItemStack> items = plugin.getMerchantManager().getItemManager().getMerchantItems();
        for (ItemStack item : items) {
            ItemStack shopItem = item.clone();
            ItemMeta meta = shopItem.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GREEN + "Price: " + ChatColor.WHITE + "$100"); // Placeholder price
                lore.add(ChatColor.AQUA + "Click to purchase!");
                meta.setLore(lore);
                shopItem.setItemMeta(meta);
            }
            gui.addItem(shopItem);
        }
    }
    
    private void fillBorders() {
        ItemStack glassPane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glassPane.getItemMeta();
        if (meta != null) meta.setDisplayName(" ");
        glassPane.setItemMeta(meta);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null) {
                if (i < 9 || i > 44 || i % 9 == 0 || (i + 1) % 9 == 0) {
                     gui.setItem(i, glassPane);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != gui) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getType() == Material.BLACK_STAINED_GLASS_PANE) return;

        player.sendMessage(ChatColor.GREEN + "You purchased an item!");
        
        ItemStack purchasedItem = clickedItem.clone();
        ItemMeta meta = purchasedItem.getItemMeta();
        if (meta != null && meta.hasLore()) {
            List<String> newLore = new ArrayList<>(meta.getLore());
            newLore.removeIf(line -> line.contains("Price:") || line.contains("Click to purchase!"));
            if (newLore.isEmpty() || (newLore.size() == 1 && newLore.get(0).isEmpty())) {
                 meta.setLore(null);
            } else {
                 meta.setLore(newLore);
            }
            purchasedItem.setItemMeta(meta);
        }
        player.getInventory().addItem(purchasedItem);
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // This ensures the listener is always unregistered when the GUI is closed.
        // This is the fix.
        if (event.getInventory() == gui) {
            HandlerList.unregisterAll(this);
        }
    }
             }
          
