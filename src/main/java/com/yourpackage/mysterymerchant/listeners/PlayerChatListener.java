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

        event.setCancelled(true);
        String message = event.getMessage();
        
        Integer editingSlot = plugin.getPlayerEditingItemSlot(player);
        
        // Get the specific editor instance that the player was using
        EditorGUI activeEditor = plugin.getOpenEditorForPlayer(player);

        if (message.equalsIgnoreCase("cancel")) {
            plugin.removePlayerFromEditMode(player);
            player.sendMessage(ChatColor.RED + "Edit cancelled.");
            if (activeEditor != null && editingSlot != null) {
                plugin.getServer().getScheduler().runTask(plugin, () -> activeEditor.openItemEditor(player, editingSlot));
            }
            return;
        }

        String editMode = plugin.getPlayerEditMode(player);
        ItemManager itemManager = plugin.getMerchantManager().getItemManager();

        if (editingSlot == null) {
            plugin.removePlayerFromEditMode(player);
            return;
        }

        MerchantItem itemToEdit = itemManager.getMerchantItems().get(editingSlot);

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
        
        itemManager.updateItem(editingSlot, itemToEdit);
        plugin.removePlayerFromEditMode(player);
        
        if (activeEditor != null) {
            plugin.getServer().getScheduler().runTask(plugin, () -> activeEditor.openItemEditor(player, editingSlot));
        }
    }
}
