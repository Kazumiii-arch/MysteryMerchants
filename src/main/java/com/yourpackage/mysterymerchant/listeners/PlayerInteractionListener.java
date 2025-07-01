package com.yourpackage.mysterymerchant.listeners;

import com.yourpackage.mysterymerchant.MysteryMerchant;
import com.yourpackage.mysterymerchant.merchant.Merchant;
import com.yourpackage.mysterymerchant.ui.ShopGUI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractionListener implements Listener {

    private final MysteryMerchant plugin;

    public PlayerInteractionListener(MysteryMerchant plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Entity clickedEntity = event.getRightClicked();
        Merchant activeMerchant = plugin.getMerchantManager().getActiveMerchant();

        if (activeMerchant == null || !activeMerchant.isSpawned()) return;

        if (clickedEntity.getUniqueId().equals(activeMerchant.getEntityId())) {
            event.setCancelled(true);
            if (!player.hasPermission("mysterymerchant.use")) {
                player.sendMessage(org.bukkit.ChatColor.RED + "You don't have permission to trade here.");
                return;
            }
            new ShopGUI(plugin).open(player);
        }
    }
}

