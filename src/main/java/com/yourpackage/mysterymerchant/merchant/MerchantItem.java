package com.yourpackage.mysterymerchant.merchant;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class MerchantItem {

    private ItemStack itemStack;
    private double price;
    private String rarity;
    private List<String> commands; // NEW: List of commands to run

    public MerchantItem(ItemStack itemStack, double price, String rarity, List<String> commands) {
        this.itemStack = itemStack;
        this.price = price;
        this.rarity = rarity;
        this.commands = (commands != null) ? commands : new ArrayList<>();
    }

    // Getters
    public ItemStack getItemStack() { return itemStack; }
    public double getPrice() { return price; }
    public String getRarity() { return rarity; }
    public List<String> getCommands() { return commands; } // NEW

    // Setters
    public void setItemStack(ItemStack itemStack) { this.itemStack = itemStack; }
    public void setPrice(double price) { this.price = price; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public void setCommands(List<String> commands) { this.commands = commands; } // NEW
}
