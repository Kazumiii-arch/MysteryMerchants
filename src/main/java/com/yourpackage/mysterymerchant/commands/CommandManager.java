package com.yourpackage.mysterymerchant.commands;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.ui.EditorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.bukkit.ChatColor;

public class CommandManager implements CommandExecutor {

    private final MysteryMerchant plugin;

    public CommandManager(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("mysterymerchant.admin")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "setspawn":
                handleSetSpawn(player);
                break;
            case "spawn":
                handleSpawn(player);
                break;
            case "despawn":
                handleDespawn(player);
                break;
            case "editor":
                handleEditor(player);
                break;
            case "additem":
                handleAddItem(player);
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use '/mm' for help.");
                break;
        }
        return true;
    }
    
    private void handleSetSpawn(Player player) {
        plugin.getMerchantManager().setSpawnLocation(player.getLocation());
        player.sendMessage(ChatColor.GREEN + "Mystery Merchant spawn location has been set.");
    }

    private void handleSpawn(Player player) {
        if (plugin.getMerchantManager().isMerchantActive()) {
            player.sendMessage(ChatColor.YELLOW + "The Mystery Merchant is already spawned.");
            return;
        }
        boolean success = plugin.getMerchantManager().spawnMerchant();
        if (success) {
            player.sendMessage(ChatColor.GREEN + "Mystery Merchant has been spawned!");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to spawn merchant. Is the spawn location set?");
        }
    }

    private void handleDespawn(Player player) {
        if (!plugin.getMerchantManager().isMerchantActive()) {
            player.sendMessage(ChatColor.YELLOW + "The Mystery Merchant is not currently spawned.");
            return;
        }
        plugin.getMerchantManager().despawnMerchant();
        player.sendMessage(ChatColor.GREEN + "Mystery Merchant has been despawned.");
    }

    private void handleEditor(Player player) {
        new EditorGUI(plugin).open(player);
    }

    private void handleAddItem(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType().isAir()) {
            player.sendMessage(ChatColor.RED + "You must be holding an item to add it.");
            return;
        }
        plugin.getMerchantManager().getItemManager().addItem(itemInHand);
        player.sendMessage(ChatColor.GREEN + "Item '" + itemInHand.getType().name() + "' added to stock.");
        player.sendMessage(ChatColor.YELLOW + "Use '/mm editor' to set its price and rarity.");
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Mystery Merchant Admin Commands ---");
        player.sendMessage(ChatColor.YELLOW + "/mm setspawn" + ChatColor.WHITE + " - Sets the merchant's spawn point.");
        player.sendMessage(ChatColor.YELLOW + "/mm spawn" + ChatColor.WHITE + " - Manually spawns the merchant.");
        player.sendMessage(ChatColor.YELLOW + "/mm despawn" + ChatColor.WHITE + " - Manually despawns the merchant.");
        player.sendMessage(ChatColor.YELLOW + "/mm editor" + ChatColor.WHITE + " - Opens the GUI to manage items.");
        player.sendMessage(ChatColor.YELLOW + "/mm additem" + ChatColor.WHITE + " - Adds your held item to the stock.");
        player.sendMessage(ChatColor.GOLD + "------------------------------------");
    }
    }
