package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class BountyListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // If there's no killer (environmental death), bounty stays active
        if (killer == null) {
            return; // No killer (environmental death)
        }

        // Check if the victim was hunting someone (reverse bounty scenario)
        if (BountyManager.hasBounty(killer.getUniqueId())) {
            BountyData killerBounty = BountyManager.getBounty(killer.getUniqueId());
            // Check if the victim (who died) was the hunter for the killer's bounty
            if (killerBounty.isAccepted() && killerBounty.getHunterUUID().equals(victim.getUniqueId())) {
                // Target killed their hunter! Award the bounty to the target
                BountyManager.claimBountyAsTarget(killer, victim);
                return;
            }
        }

        // Check normal bounty scenario: hunter kills target
        if (BountyManager.hasBounty(victim.getUniqueId())) {
            // Only award the bounty if the killer is the accepted hunter
            BountyData bounty = BountyManager.getBounty(victim.getUniqueId());
            if (bounty.isAccepted() && bounty.getHunterUUID().equals(killer.getUniqueId())) {
                // Hunter successfully killed their target - award the bounty
                BountyManager.claimBounty(killer, victim);
            } else if (!bounty.isAccepted()) {
                // Bounty not accepted yet - award to whoever killed them
                BountyManager.claimBounty(killer, victim);
            } else {
                // Someone else killed the target - bounty remains active
                Bukkit.broadcastMessage(ChatColor.YELLOW + victim.getName() + " was killed by " + killer.getName() + 
                    " but the bounty of $" + String.format("%.2f", bounty.getAmount()) + 
                    " remains active for " + bounty.getHunterName() + "!");
            }
            return;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Record this player in our player data system
        PlayerDataManager.recordPlayer(player);
        
        // Check if this player has a bounty
        if (BountyManager.hasBounty(player.getUniqueId())) {
            BountyData bounty = BountyManager.getBounty(player.getUniqueId());
            
            // Notify the player and server about their bounty
            String hunterInfo = bounty.isAccepted() ? 
                " and is being hunted by " + bounty.getHunterName() : "";
            player.sendMessage(ChatColor.RED + "⚠ WARNING: You have an active bounty of $" + 
                String.format("%.2f", bounty.getAmount()) + " placed by " + bounty.getPlacedBy() + hunterInfo + "!");
            
            String broadcastMessage = bounty.isAccepted() ?
                player.getName() + " has joined with an active bounty (being hunted by " + bounty.getHunterName() + ")!" :
                player.getName() + " has joined with an active bounty!";
            Bukkit.broadcastMessage(ChatColor.GOLD + broadcastMessage);
            
            // If bounty is accepted and hunter is online, activate modes
            if (bounty.isAccepted()) {
                Player hunter = Bukkit.getPlayer(bounty.getHunterUUID());
                if (hunter != null) {
                    PlayerModeManager.setHunterMode(hunter, player);
                    // Update target's compass to point to hunter
                    player.setCompassTarget(hunter.getLocation());
                    player.sendMessage(ChatColor.GREEN + "🧭 Your compass now points to " + hunter.getName() + "!");
                    player.sendMessage(ChatColor.YELLOW + "💡 Use '/bounty track' to get detailed tracking info on your hunter!");
                }
            }
        }
        
        // Check if this player is a hunter for any bounty
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        if (acceptedBountyTarget != null) {
            Player target = Bukkit.getPlayer(acceptedBountyTarget);
            if (target != null) {
                // Both hunter and target are online, activate modes
                PlayerModeManager.setHunterMode(player, target);
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Clean up any hunter/target modes when player disconnects
        PlayerModeManager.handlePlayerDisconnect(player);
    }
}
