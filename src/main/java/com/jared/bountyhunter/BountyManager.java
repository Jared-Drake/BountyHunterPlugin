package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class BountyManager {
    private static HashMap<UUID, BountyData> bounties = new HashMap<>();
    
    public static HashMap<UUID, BountyData> getBounties() {
        return bounties;
    }
    
    public static boolean hasBounty(UUID playerUUID) {
        return bounties.containsKey(playerUUID);
    }
    
    public static BountyData getBounty(UUID playerUUID) {
        return bounties.get(playerUUID);
    }
    
    public static boolean setBounty(Player target, Player placedBy, BountyData.CurrencyType currency, int amount) {
        // Check if player has enough of the currency
        if (!hasEnoughCurrency(placedBy, currency, amount)) {
            placedBy.sendMessage(ChatColor.RED + "You don't have enough " + getCurrencyName(currency) + "s!");
            return false;
        }
        
        // Remove currency from player
        removeCurrency(placedBy, currency, amount);
        
        // Create and store bounty
        BountyData bounty = new BountyData(target.getUniqueId(), placedBy.getUniqueId(), 
            placedBy.getName(), currency, amount);
        bounties.put(target.getUniqueId(), bounty);
        
        // Notify players
        placedBy.sendMessage(ChatColor.GREEN + "Bounty of " + amount + " " + getCurrencyName(currency) + 
            (amount > 1 ? "s" : "") + " placed on " + target.getName() + "!");
        Bukkit.broadcastMessage(ChatColor.GOLD + placedBy.getName() + " placed a bounty of " + 
            amount + " " + getCurrencyName(currency) + (amount > 1 ? "s" : "") + " on " + target.getName() + "!");
        
        return true;
    }
    
    public static void removeBounty(UUID targetUUID) {
        bounties.remove(targetUUID);
    }
    
    public static void claimBounty(Player killer, Player killed) {
        UUID killedUUID = killed.getUniqueId();
        if (!bounties.containsKey(killedUUID)) {
            return;
        }
        
        BountyData bounty = bounties.get(killedUUID);
        
        // Give currency to killer
        giveCurrency(killer, bounty.getCurrency(), bounty.getAmount());
        
        // Notify players
        killer.sendMessage(ChatColor.GREEN + "You claimed a bounty of " + bounty.getAmount() + " " + 
            getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + " for killing " + killed.getName() + "!");
        Bukkit.broadcastMessage(ChatColor.GOLD + killer.getName() + " claimed a bounty of " + 
            bounty.getAmount() + " " + getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + 
            " for killing " + killed.getName() + "!");
        
        // Remove bounty
        bounties.remove(killedUUID);
    }
    
    private static boolean hasEnoughCurrency(Player player, BountyData.CurrencyType currency, int amount) {
        Material material = getCurrencyMaterial(currency);
        int count = 0;
        
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        
        return count >= amount;
    }
    
    private static void removeCurrency(Player player, BountyData.CurrencyType currency, int amount) {
        Material material = getCurrencyMaterial(currency);
        int remaining = amount;
        
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == material) {
                int toRemove = Math.min(remaining, item.getAmount());
                if (toRemove == item.getAmount()) {
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - toRemove);
                }
                remaining -= toRemove;
            }
        }
    }
    
    private static void giveCurrency(Player player, BountyData.CurrencyType currency, int amount) {
        Material material = getCurrencyMaterial(currency);
        ItemStack currencyItem = new ItemStack(material, amount);
        player.getInventory().addItem(currencyItem);
    }
    
    private static Material getCurrencyMaterial(BountyData.CurrencyType currency) {
        switch (currency) {
            case DIAMOND:
                return Material.DIAMOND;
            case EMERALD:
                return Material.EMERALD;
            case NETHERITE:
                return Material.NETHERITE_INGOT;
            default:
                return Material.PAPER;
        }
    }
    
    private static String getCurrencyName(BountyData.CurrencyType currency) {
        switch (currency) {
            case DIAMOND:
                return "Diamond";
            case EMERALD:
                return "Emerald";
            case NETHERITE:
                return "Netherite Ingot";
            default:
                return "Unknown";
        }
    }
}
