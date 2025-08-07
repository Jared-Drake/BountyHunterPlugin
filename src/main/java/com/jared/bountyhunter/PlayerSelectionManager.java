package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class PlayerSelectionManager {
    private static HashMap<UUID, Boolean> playersInSelectionMode = new HashMap<>();
    
    public static void enterSelectionMode(Player player) {
        playersInSelectionMode.put(player.getUniqueId(), true);
        player.sendMessage(ChatColor.YELLOW + "Type the name of the player you want to place a bounty on:");
        player.sendMessage(ChatColor.GRAY + "Or type 'cancel' to cancel.");
    }
    
    public static void exitSelectionMode(Player player) {
        playersInSelectionMode.remove(player.getUniqueId());
    }
    
    public static boolean isInSelectionMode(Player player) {
        return playersInSelectionMode.containsKey(player.getUniqueId());
    }
    
    public static boolean handlePlayerSelection(Player player, String targetName) {
        if (targetName.equalsIgnoreCase("cancel")) {
            exitSelectionMode(player);
            player.sendMessage(ChatColor.GRAY + "Bounty placement cancelled.");
            return true;
        }
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return false;
        }
        
        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot place a bounty on yourself!");
            return false;
        }
        
        if (BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + target.getName() + "!");
            return false;
        }
        
        exitSelectionMode(player);
        BountyGUI.openSetBountyMenu(player, target);
        return true;
    }
}
