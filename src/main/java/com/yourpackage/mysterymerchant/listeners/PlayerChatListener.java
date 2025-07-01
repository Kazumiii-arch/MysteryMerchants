package com.yourpackage.mysterymerchant.listeners;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.ItemManager;
import com.yourpackage.mysterymerchant.merchant.MerchantItem;
import com.yourpackage.mysterymerchant.ui.EditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerChatListener implements Listener {

    private final MysteryMerchant plugin;

    public PlayerChatListener(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isPlayerInEditMode(player)) {
            return;
        }

        // Cancel the event so the message doesn't appear in public chat
        event.setCancelled(true);
        String message = event.getMessage();
        
        // Handle cancellation
        if (message.equalsIgnoreCase("cancel")) {
            plugin.removePlayerFromEditMode(player);
            player.sendMessage(ChatColor.RED + "Edit cancelled.");
            // Re-open the main editor GUI on the main server thread
            plugin.getServer().getScheduler().runTask(plugin, () -> new EditorGUI(plugin).open(player));
            return;
        }

        String editMode = plugin.getPlayerEditMode(player);
        Integer editingSlot = plugin.getPlayerEditingItemSlot(player);
        ItemManager itemManager = plugin.getMerchantManager().getItemManager();

        if (editingSlot == null) {
            plugin.removePlayerFromEditMode(player);
            return;
        }

        MerchantItem itemToEdit = itemManager.getMerchantItems().get(editingSlot);

        // Process the input based on what the player was editing
        switch (editMode.toLowerCase()) {
            case "rename":
                ItemStack itemStack = itemToEdit.getItemStack();
                ItemMeta meta = itemStack.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', message));
                    itemStack.setItemMeta(meta);
                }
                player.sendMessage(ChatColor.GREEN + "Item renamed successfully!");
                break;
            case "addlore":
                ItemStack loreItemStack = itemToEdit.getItemStack();
                ItemMeta loreMeta = loreItemStack.getItemMeta();
                if (loreMeta != null) {
                    List<String> lore = loreMeta.hasLore() ? loreMeta.getLore() : new ArrayList<>();
                    lore.add(ChatColor.translateAlternateColorCodes('&', message));
                    loreMeta.setLore(lore);
                    loreItemStack.setItemMeta(loreMeta);
                }
                player.sendMessage(ChatColor.GREEN + "Lore line added!");
                break;
            case "addcommand":
                itemToEdit.getCommands().add(message);
                player.sendMessage(ChatColor.GREEN + "Command added successfully!");
                break;
        }
        
        // Save the changes, remove the player from edit mode, and re-open the GUI
        itemManager.updateItem(editingSlot, itemToEdit);
        plugin.removePlayerFromEditMode(player);
        plugin.getServer().getScheduler().runTask(plugin, () -> new EditorGUI(plugin).open(player));
    }
}
