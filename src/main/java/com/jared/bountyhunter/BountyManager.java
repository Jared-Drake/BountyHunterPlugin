package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class BountyManager {
    private static HashMap<UUID, BountyData> bounties = new HashMap<>();
    
    public static void loadBountiesFromFile() {
        bounties = BountyDataManager.loadBounties();
    }
    
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
        return setBounty(target.getUniqueId(), target.getName(), placedBy, currency, amount);
    }
    
    public static boolean setBounty(UUID targetUUID, String targetName, Player placedBy, BountyData.CurrencyType currency, int amount) {
        // Check if player has enough of the currency
        if (!hasEnoughCurrency(placedBy, currency, amount)) {
            placedBy.sendMessage(ChatColor.RED + "You don't have enough " + getCurrencyName(currency) + "s!");
            return false;
        }
        
        // Remove currency from player
        removeCurrency(placedBy, currency, amount);
        
        // Create and store bounty
        BountyData bounty = new BountyData(targetUUID, placedBy.getUniqueId(), 
            placedBy.getName(), currency, amount);
        bounties.put(targetUUID, bounty);
        
        // Save bounty to file
        BountyDataManager.saveBounty(targetUUID, bounty);
        
        // Notify players
        placedBy.sendMessage(ChatColor.GREEN + "Bounty of " + amount + " " + getCurrencyName(currency) + 
            (amount > 1 ? "s" : "") + " placed on " + targetName + "!");
        
        // Check if target is online for notification
        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        String onlineStatus = onlineTarget != null ? "" : " (offline)";
        
        Bukkit.broadcastMessage(ChatColor.GOLD + placedBy.getName() + " placed a bounty of " + 
            amount + " " + getCurrencyName(currency) + (amount > 1 ? "s" : "") + " on " + targetName + onlineStatus + "!");
        
        return true;
    }
    
    public static void removeBounty(UUID targetUUID) {
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
    }
    
    public static void claimBounty(Player killer, Player killed) {
        UUID killedUUID = killed.getUniqueId();
        if (!bounties.containsKey(killedUUID)) {
            return;
        }
        
        BountyData bounty = bounties.get(killedUUID);
        
        // Check if bounty is accepted and if killer is the hunter
        if (bounty.isAccepted()) {
            if (!bounty.getHunterUUID().equals(killer.getUniqueId())) {
                // Killer is not the accepted hunter
                killer.sendMessage(ChatColor.RED + "You cannot claim this bounty! It has been accepted by " + bounty.getHunterName() + ".");
                return;
            }
            
            // Clear hunter/target modes since bounty is being completed
            PlayerModeManager.clearHunterMode(killer);
        }
        
        // Give currency to killer
        giveCurrency(killer, bounty.getCurrency(), bounty.getAmount());
        
        // Notify players
        String claimMessage = bounty.isAccepted() ? 
            "You successfully completed your accepted bounty of " : 
            "You claimed an unaccepted bounty of ";
        killer.sendMessage(ChatColor.GREEN + claimMessage + bounty.getAmount() + " " + 
            getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + " for killing " + killed.getName() + "!");
        
        String broadcastMessage = bounty.isAccepted() ?
            killer.getName() + " completed their bounty hunt and claimed " :
            killer.getName() + " claimed a bounty of ";
        Bukkit.broadcastMessage(ChatColor.GOLD + broadcastMessage + 
            bounty.getAmount() + " " + getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + 
            " for killing " + killed.getName() + "!");
        
        // Remove bounty
        bounties.remove(killedUUID);
        BountyDataManager.removeBounty(killedUUID);
    }
    
    public static void claimBountyAsTarget(Player target, Player deadHunter) {
        UUID targetUUID = target.getUniqueId();
        if (!bounties.containsKey(targetUUID)) {
            return;
        }
        
        BountyData bounty = bounties.get(targetUUID);
        
        // Verify this was indeed a hunter-target scenario
        if (!bounty.isAccepted() || !bounty.getHunterUUID().equals(deadHunter.getUniqueId())) {
            return;
        }
        
        // Clear hunter/target modes since the hunt is over
        PlayerModeManager.clearTargetMode(target);
        
        // Give currency to target (they survived and killed their hunter!)
        giveCurrency(target, bounty.getCurrency(), bounty.getAmount());
        
        // Notify players with special messages for reverse bounty
        target.sendMessage(ChatColor.GREEN + "⚔ " + ChatColor.BOLD + "BOUNTY DEFENSE SUCCESSFUL!" + ChatColor.GREEN + 
            " You killed your hunter and claimed " + bounty.getAmount() + " " + 
            getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + "!");
        
        // Special title for target who survived
        target.sendTitle(ChatColor.GOLD + "⚔ BOUNTY DEFENDED ⚔", 
                        ChatColor.GREEN + "You killed your hunter!", 10, 70, 20);
        
        // Play victory sound
        target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        
        Bukkit.broadcastMessage(ChatColor.GOLD + "⚔ " + target.getName() + " defended against their bounty hunter " + 
            deadHunter.getName() + " and claimed the " + bounty.getAmount() + " " + 
            getCurrencyName(bounty.getCurrency()) + (bounty.getAmount() > 1 ? "s" : "") + " bounty!");
        
        // Remove bounty
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
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
    
    public static void acceptBounty(UUID targetUUID, Player hunter) {
        if (!bounties.containsKey(targetUUID)) {
            return;
        }
        
        BountyData bounty = bounties.get(targetUUID);
        bounty.setHunter(hunter.getUniqueId(), hunter.getName());
        
        // Save updated bounty to file
        BountyDataManager.saveBounty(targetUUID, bounty);
        
        // Notify players
        Player target = Bukkit.getPlayer(targetUUID);
        String targetName = target != null ? target.getName() : "Unknown";
        String currencyName = getCurrencyName(bounty.getCurrency());
        
        hunter.sendMessage(ChatColor.GREEN + "You have accepted the bounty on " + targetName + 
            " for " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + "!");
        Bukkit.broadcastMessage(ChatColor.GOLD + hunter.getName() + " has accepted the bounty on " + targetName + "!");
        
        if (target != null) {
            target.sendMessage(ChatColor.RED + "⚠ " + hunter.getName() + " is now hunting you for the bounty!");
            
            // Activate hunter/target modes if both players are online
            PlayerModeManager.setHunterMode(hunter, target);
        }
    }
    
    public static void abandonBounty(UUID targetUUID, Player hunter) {
        if (!bounties.containsKey(targetUUID)) {
            return;
        }
        
        BountyData bounty = bounties.get(targetUUID);
        if (!bounty.isAccepted() || !bounty.getHunterUUID().equals(hunter.getUniqueId())) {
            return;
        }
        
        // Clear hunter/target modes if both players are online
        Player target = Bukkit.getPlayer(targetUUID);
        if (target != null) {
            PlayerModeManager.clearHunterMode(hunter);
        }
        
        bounty.clearHunter();
        
        // Save updated bounty to file
        BountyDataManager.saveBounty(targetUUID, bounty);
        
        // Notify players
        String targetName = target != null ? target.getName() : "Unknown";
        String currencyName = getCurrencyName(bounty.getCurrency());
        
        hunter.sendMessage(ChatColor.YELLOW + "You have abandoned the bounty on " + targetName + ".");
        Bukkit.broadcastMessage(ChatColor.YELLOW + hunter.getName() + " has abandoned the bounty on " + targetName + 
            ". The bounty of " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + " is now available again!");
        
        if (target != null) {
            target.sendMessage(ChatColor.YELLOW + hunter.getName() + " is no longer hunting you. The bounty is available again.");
        }
    }
    
    public static UUID getAcceptedBountyTarget(UUID hunterUUID) {
        for (UUID targetUUID : bounties.keySet()) {
            BountyData bounty = bounties.get(targetUUID);
            if (bounty.isAccepted() && bounty.getHunterUUID().equals(hunterUUID)) {
                return targetUUID;
            }
        }
        return null;
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
