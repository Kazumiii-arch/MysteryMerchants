package com.yourpackage.mysterymerchant.merchant;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a single item sold by the merchant, including its properties
 * like price and rarity.
 */
public class MerchantItem {

    private ItemStack itemStack;
    private double price;
    private String rarity;

    public MerchantItem(ItemStack itemStack, double price, String rarity) {
        this.itemStack = itemStack;
        this.price = price;
        this.rarity = rarity;
    }

    // Getters
    public ItemStack getItemStack() {
        return itemStack;
    }

    public double getPrice() {
        return price;
    }

    public String getRarity() {
        return rarity;
    }

    // Setters
    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }
}
