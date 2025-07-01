package com.yourpackage.mysterymerchant.commands;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.ui.EditorGUI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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
                plugin.getMerchantManager().setSpawnLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Mystery Merchant spawn location has been set.");
                break;
            case "spawn":
                if (plugin.getMerchantManager().isMerchantActive()) {
                    player.sendMessage(ChatColor.YELLOW + "The Mystery Merchant is already spawned.");
                    return true;
                }
                if (plugin.getMerchantManager().spawnMerchant()) {
                    player.sendMessage(ChatColor.GREEN + "Mystery Merchant has been spawned!");
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to spawn merchant. Is the spawn location set?");
                }
                break;
            case "despawn":
                 if (!plugin.getMerchantManager().isMerchantActive()) {
                    player.sendMessage(ChatColor.YELLOW + "The Mystery Merchant is not currently spawned.");
                    return true;
                }
                plugin.getMerchantManager().despawnMerchant();
                player.sendMessage(ChatColor.GREEN + "Mystery Merchant has been despawned.");
                break;
            case "editor":
                new EditorGUI(plugin).open(player);
                break;
            case "additem":
                ItemStack itemInHand = player.getInventory().getItemInMainHand();
                if (itemInHand.getType().isAir()) {
                    player.sendMessage(ChatColor.RED + "You must be holding an item to add it.");
                    return true;
                }
                plugin.getMerchantManager().getItemManager().addItem(itemInHand);
                player.sendMessage(ChatColor.GREEN + "Item '" + itemInHand.getType().name() + "' added to stock.");
                player.sendMessage(ChatColor.YELLOW + "Use '/mm editor' to set its price and rarity.");
                break;
            case "autospawn":
                handleAutoSpawn(player, args);
                break;
            case "reload":
                plugin.reloadConfig();
                plugin.startAutoSpawnTask(); // Restart task with new config values
                player.sendMessage(ChatColor.GREEN + "MysteryMerchant config reloaded.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown command. Use '/mm' for help.");
                break;
        }
        return true;
    }
    
    private void handleAutoSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /mm autospawn <interval_hours|stop>");
            return;
        }

        if (args[1].equalsIgnoreCase("stop")) {
            plugin.getConfig().set("autospawn.enabled", false);
            plugin.saveConfig();
            plugin.startAutoSpawnTask(); // This will stop the current task
            player.sendMessage(ChatColor.GREEN + "Automatic spawning has been disabled.");
            return;
        }

        try {
            int hours = Integer.parseInt(args[1]);
            if (hours <= 0) {
                player.sendMessage(ChatColor.RED + "Interval must be a positive number of hours.");
                return;
            }
            plugin.getConfig().set("autospawn.enabled", true);
            plugin.getConfig().set("autospawn.interval-hours", hours);
            plugin.saveConfig();
            plugin.startAutoSpawnTask(); // Restart the task with the new interval
            player.sendMessage(ChatColor.GREEN + "Automatic spawning enabled! The merchant will now spawn every " + hours + " hours.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid number. Please provide the interval in hours.");
        }
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "--- Mystery Merchant Admin Commands ---");
        player.sendMessage(ChatColor.YELLOW + "/mm setspawn" + ChatColor.WHITE + " - Sets the merchant's spawn point.");
        player.sendMessage(ChatColor.YELLOW + "/mm spawn" + ChatColor.WHITE + " - Manually spawns the merchant.");
        player.sendMessage(ChatColor.YELLOW + "/mm despawn" + ChatColor.WHITE + " - Manually despawns the merchant.");
        player.sendMessage(ChatColor.YELLOW + "/mm editor" + ChatColor.WHITE + " - Opens the GUI to manage items.");
        player.sendMessage(ChatColor.YELLOW + "/mm additem" + ChatColor.WHITE + " - Adds your held item to the stock.");
        player.sendMessage(ChatColor.YELLOW + "/mm autospawn <hours>" + ChatColor.WHITE + " - Enables auto spawning.");
        player.sendMessage(ChatColor.YELLOW + "/mm autospawn stop" + ChatColor.WHITE + " - Disables auto spawning.");
        player.sendMessage(ChatColor.YELLOW + "/mm reload" + ChatColor.WHITE + " - Reloads the config file.");
        player.sendMessage(ChatColor.GOLD + "------------------------------------");
    }
}
