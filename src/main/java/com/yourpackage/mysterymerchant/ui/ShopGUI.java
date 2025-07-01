package com.yourpackage.mysterymerchant.ui;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.Merchant;
import com.yourpackage.mysterymerchant.merchant.MerchantItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShopGUI implements Listener {

    private final MysteryMerchant plugin;
    private Inventory gui;
    private BukkitRunnable updateTask;

    public ShopGUI(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String title = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&5&lMysterious Wares"));
        gui = Bukkit.createInventory(null, 54, title);

        fillBorders();
        populateShopItems();
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        startTimerUpdateTask();
        player.openInventory(gui);
    }

    private void populateShopItems() {
        List<MerchantItem> items = plugin.getMerchantManager().getItemManager().getMerchantItems();
        for (MerchantItem merchantItem : items) {
            ItemStack shopItem = merchantItem.getItemStack().clone();
            ItemMeta meta = shopItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getRarityColor(merchantItem.getRarity()) + ChatColor.stripColor(meta.getDisplayName()));
                
                // Use original lore if it exists, otherwise create a new list
                List<String> lore = meta.hasLore() ? meta.getLore().stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()) : new ArrayList<>();
                lore.add("");
                lore.add(ChatColor.GOLD + "Price: " + ChatColor.WHITE + "$" + merchantItem.getPrice());
                lore.add(getRarityColor(merchantItem.getRarity()) + "Rarity: " + ChatColor.WHITE + merchantItem.getRarity());
                
                if (merchantItem.getCommands() != null && !merchantItem.getCommands().isEmpty()) {
                    lore.add(ChatColor.LIGHT_PURPLE + "" + ChatColor.ITALIC + "This is a special perk!");
                }

                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to purchase!");
                meta.setLore(lore);
                shopItem.setItemMeta(meta);
            }
            gui.addItem(shopItem);
        }
    }
    
    private void fillBorders() {
        ItemStack darkPane = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta darkMeta = darkPane.getItemMeta();
        darkMeta.setDisplayName(" ");
        darkPane.setItemMeta(darkMeta);

        ItemStack lightPane = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE);
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
    }

    private void startTimerUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                Merchant merchant = plugin.getMerchantManager().getActiveMerchant();
                if (merchant == null || !merchant.isSpawned() || gui.getViewers().isEmpty()) {
                    this.cancel();
                    return;
                }
                long remaining = merchant.getRemainingSeconds();
                long minutes = remaining / 60;
                long seconds = remaining % 60;
                String time = String.format("%02d:%02d", minutes, seconds);

                ItemStack timerItem = new ItemStack(Material.CLOCK);
                ItemMeta meta = timerItem.getItemMeta();
                meta.setDisplayName(ChatColor.YELLOW + "Time Remaining");
                meta.setLore(Arrays.asList(ChatColor.WHITE + "The merchant will depart in:", ChatColor.GOLD + time));
                timerItem.setItemMeta(meta);
                gui.setItem(49, timerItem);
            }
        };
        updateTask.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() != gui) return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType().isAir() || clickedItem.getType().name().endsWith("_PANE") || clickedItem.getType() == Material.CLOCK) {
            return;
        }

        // Find the corresponding MerchantItem by comparing the base item without the shop lore
        MerchantItem clickedMerchantItem = null;
        for(MerchantItem item : plugin.getMerchantManager().getItemManager().getMerchantItems()){
            // We create a "clean" version of the clicked item to compare against the original
            ItemStack cleanClickedItem = clickedItem.clone();
            ItemMeta cleanMeta = cleanClickedItem.getItemMeta();
            if (cleanMeta != null && cleanMeta.hasLore()) {
                List<String> originalLore = item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta().hasLore() ? item.getItemStack().getItemMeta().getLore() : new ArrayList<>();
                cleanMeta.setLore(originalLore);
                cleanMeta.setDisplayName(item.getItemStack().getItemMeta().getDisplayName());
            }
            cleanClickedItem.setItemMeta(cleanMeta);

            if(item.getItemStack().isSimilar(cleanClickedItem)){
                clickedMerchantItem = item;
                break;
            }
        }

        if(clickedMerchantItem == null) {
            player.sendMessage(ChatColor.RED + "An error occurred trying to purchase this item.");
            return;
        }

        // In a real plugin, you would check if the player has enough money here
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1.0f, 1.0f);

        // Check if this is a command item or a regular item
        if (clickedMerchantItem.getCommands() != null && !clickedMerchantItem.getCommands().isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "You purchased a special perk!");
            for (String command : clickedMerchantItem.getCommands()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            }
        } else {
            player.sendMessage(ChatColor.GREEN + "You purchased an item!");
            player.getInventory().addItem(clickedMerchantItem.getItemStack().clone());
        }
        
        player.closeInventory();
    }

    private ChatColor getRarityColor(String rarity) {
        switch (rarity.toLowerCase()) {
            case "legendary": return ChatColor.GOLD;
            case "epic": return ChatColor.LIGHT_PURPLE;
            case "rare": return ChatColor.AQUA;
            default: return ChatColor.GRAY;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory() == gui) {
            if (updateTask != null) updateTask.cancel();
            HandlerList.unregisterAll(this);
        }
    }
                    }
                    
