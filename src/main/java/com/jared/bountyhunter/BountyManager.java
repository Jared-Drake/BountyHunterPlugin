package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.HashMap;
import java.util.UUID;

public class BountyManager {
    private static HashMap<UUID, BountyData> bounties = new HashMap<>();
    private static Economy economy;
    
    public static void setEconomy(Economy econ) {
        economy = econ;
    }
    
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
    
    public static boolean setBounty(Player target, Player placedBy, double amount) {
        return setBounty(target.getUniqueId(), target.getName(), placedBy, amount);
    }
    
    public static boolean setBounty(UUID targetUUID, String targetName, Player placedBy, double amount) {
        // Check if target is on cooldown
        if (BountyCooldownManager.isOnCooldown(targetUUID)) {
            long remainingTime = BountyCooldownManager.getRemainingCooldown(targetUUID);
            String timeString = BountyCooldownManager.formatRemainingTime(remainingTime);
            placedBy.sendMessage(ChatColor.RED + "Cannot place bounty on " + targetName + "!");
            placedBy.sendMessage(ChatColor.GRAY + "Player is on bounty cooldown for " + ChatColor.WHITE + timeString);
            placedBy.sendMessage(ChatColor.YELLOW + "💡 Cooldowns prevent bounty spam after a bounty is claimed.");
            return false;
        }
        
        // Check if player has enough money
        if (!hasEnoughMoney(placedBy, amount)) {
            placedBy.sendMessage(ChatColor.RED + "You don't have enough money! You need $" + String.format("%.2f", amount));
            return false;
        }
        
        // Remove money from player
        if (!removeMoney(placedBy, amount)) {
            placedBy.sendMessage(ChatColor.RED + "Failed to process payment! Please try again.");
            return false;
        }
        
        // Create and store bounty
        BountyData bounty = new BountyData(targetUUID, placedBy.getUniqueId(), 
            placedBy.getName(), amount);
        bounties.put(targetUUID, bounty);
        
        // Save bounty to file
        BountyDataManager.saveBounty(targetUUID, bounty);
        
        // Notify players
        placedBy.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", amount) + " placed on " + targetName + "!");
        
        // Check if target is online for notification
        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        String onlineStatus = onlineTarget != null ? "" : " (offline)";
        
        Bukkit.broadcastMessage(ChatColor.GOLD + placedBy.getName() + " placed a bounty of $" + 
            String.format("%.2f", amount) + " on " + targetName + onlineStatus + "!");
        
        return true;
    }
    
    public static void removeBounty(UUID targetUUID) {
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
    }
    
    /**
     * Removes a bounty and handles all cleanup including refunds and mode clearing
     */
    public static void removeBountyWithCleanup(UUID targetUUID, Player remover) {
        BountyData bounty = bounties.get(targetUUID);
        if (bounty == null) {
            return;
        }
        
        // Check if bounty is accepted and clear hunter mode if needed
        if (bounty.isAccepted()) {
            UUID hunterUUID = bounty.getHunterUUID();
            Player hunter = Bukkit.getPlayer(hunterUUID);
            if (hunter != null) {
                // Clear hunter mode and remove effects
                PlayerModeManager.clearHunterMode(hunter);
            }
        }
        
        // Refund money to the person who placed the bounty
        Player placedBy = Bukkit.getPlayer(bounty.getPlacedByUUID());
        if (placedBy != null) {
            if (giveMoney(placedBy, bounty.getAmount())) {
                placedBy.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", bounty.getAmount()) + " has been refunded to you!");
            } else {
                placedBy.sendMessage(ChatColor.RED + "Failed to refund bounty money! Contact an administrator.");
            }
        }
        
        // Remove the bounty
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
        
        // Notify the remover
        String targetName = PlayerDataManager.getPlayerName(targetUUID);
        if (targetName == null) targetName = "Unknown";
        remover.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", bounty.getAmount()) + " removed from " + targetName + "!");
        
        // Broadcast the removal
        Bukkit.broadcastMessage(ChatColor.YELLOW + remover.getName() + " removed the bounty on " + targetName + 
            " of $" + String.format("%.2f", bounty.getAmount()) + "!");
    }
    
    /**
     * Removes a bounty when the target is offline (for admin commands or special cases)
     */
    public static void removeBountyOffline(UUID targetUUID, Player remover) {
        BountyData bounty = bounties.get(targetUUID);
        if (bounty == null) {
            return;
        }
        
        // Check if bounty is accepted and clear hunter mode if needed
        if (bounty.isAccepted()) {
            UUID hunterUUID = bounty.getHunterUUID();
            Player hunter = Bukkit.getPlayer(hunterUUID);
            if (hunter != null) {
                // Clear hunter mode and remove effects
                PlayerModeManager.clearHunterMode(hunter);
            }
        }
        
        // Refund money to the person who placed the bounty
        Player placedBy = Bukkit.getPlayer(bounty.getPlacedByUUID());
        if (placedBy != null) {
            if (giveMoney(placedBy, bounty.getAmount())) {
                placedBy.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", bounty.getAmount()) + " has been refunded to you!");
            } else {
                placedBy.sendMessage(ChatColor.RED + "Failed to refund bounty money! Contact an administrator.");
            }
        }
        
        // Remove the bounty
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
        
        // Notify the remover
        String targetName = PlayerDataManager.getPlayerName(targetUUID);
        if (targetName == null) targetName = "Unknown";
        remover.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", bounty.getAmount()) + " removed from " + targetName + "!");
        
        // Broadcast the removal
        Bukkit.broadcastMessage(ChatColor.YELLOW + remover.getName() + " removed the bounty on " + targetName + 
            " of $" + String.format("%.2f", bounty.getAmount()) + "!");
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
        
        // Give money to killer
        if (giveMoney(killer, bounty.getAmount())) {
            // Notify players
            String claimMessage = bounty.isAccepted() ? 
                "You successfully completed your accepted bounty of $" : 
                "You claimed an unaccepted bounty of $";
            killer.sendMessage(ChatColor.GREEN + claimMessage + String.format("%.2f", bounty.getAmount()) + " for killing " + killed.getName() + "!");
            
            String broadcastMessage = bounty.isAccepted() ?
                killer.getName() + " completed their bounty hunt and claimed $" :
                killer.getName() + " claimed a bounty of $";
            Bukkit.broadcastMessage(ChatColor.GOLD + broadcastMessage + 
                String.format("%.2f", bounty.getAmount()) + " for killing " + killed.getName() + "!");
        } else {
            killer.sendMessage(ChatColor.RED + "Failed to give bounty reward! Contact an administrator.");
        }
        
        // Add 24-hour cooldown for the killed player
        BountyCooldownManager.addCooldown(killedUUID);
        
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
        
        // Give money to target (they survived and killed their hunter!)
        if (giveMoney(target, bounty.getAmount())) {
            // Notify players with special messages for reverse bounty
            target.sendMessage(ChatColor.GREEN + "⚔ " + ChatColor.BOLD + "BOUNTY DEFENSE SUCCESSFUL!" + ChatColor.GREEN + 
                " You killed your hunter and claimed $" + String.format("%.2f", bounty.getAmount()) + "!");
            
            // Special title for target who survived
            target.sendTitle(ChatColor.GOLD + "⚔ BOUNTY DEFENDED ⚔", 
                            ChatColor.GREEN + "You killed your hunter!", 10, 70, 20);
            
            // Play victory sound
            target.playSound(target.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            
            Bukkit.broadcastMessage(ChatColor.GOLD + "⚔ " + target.getName() + " defended against their bounty hunter " + 
                deadHunter.getName() + " and claimed the $" + String.format("%.2f", bounty.getAmount()) + " bounty!");
        } else {
            target.sendMessage(ChatColor.RED + "Failed to give bounty reward! Contact an administrator.");
        }
        
        // Add 24-hour cooldown for the target (they successfully defended)
        BountyCooldownManager.addCooldown(targetUUID);
        
        // Remove bounty
        bounties.remove(targetUUID);
        BountyDataManager.removeBounty(targetUUID);
    }
    
    private static boolean hasEnoughMoney(Player player, double amount) {
        if (economy == null) {
            player.sendMessage(ChatColor.RED + "Economy system not available!");
            return false;
        }
        return economy.has(player, amount);
    }
    
    private static boolean removeMoney(Player player, double amount) {
        if (economy == null) {
            return false;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
    
    private static boolean giveMoney(Player player, double amount) {
        if (economy == null) {
            return false;
        }
        EconomyResponse response = economy.depositPlayer(player, amount);
        return response.transactionSuccess();
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
        
        hunter.sendMessage(ChatColor.GREEN + "You have accepted the bounty on " + targetName + 
            " for $" + String.format("%.2f", bounty.getAmount()) + "!");
        Bukkit.broadcastMessage(ChatColor.GOLD + hunter.getName() + " has accepted the bounty on " + targetName + "!");
        
        if (target != null) {
            target.sendMessage(ChatColor.RED + "⚠ " + hunter.getName() + " is now hunting you for the bounty!");
            
            // Give target a compass to track the bounty hunter
            target.setCompassTarget(hunter.getLocation());
            target.sendMessage(ChatColor.GREEN + "🧭 Your compass now points to " + hunter.getName() + "!");
            target.sendMessage(ChatColor.YELLOW + "💡 Use your compass to track the bounty hunter's location!");
            target.sendMessage(ChatColor.YELLOW + "💡 Use '/bounty track' for detailed tracking info on your hunter!");
            
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
        
        hunter.sendMessage(ChatColor.YELLOW + "You have abandoned the bounty on " + targetName + ".");
        Bukkit.broadcastMessage(ChatColor.YELLOW + hunter.getName() + " has abandoned the bounty on " + targetName + 
            ". The bounty of $" + String.format("%.2f", bounty.getAmount()) + " is now available again!");
        
        if (target != null) {
            // Clear the target's compass tracking
            target.setCompassTarget(target.getWorld().getSpawnLocation());
            target.sendMessage(ChatColor.YELLOW + hunter.getName() + " is no longer hunting you. The bounty is available again.");
            target.sendMessage(ChatColor.GRAY + "🧭 Your compass has been reset to world spawn.");
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
}
