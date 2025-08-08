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

        // If there's no killer (environmental death), check if victim had a bounty and remove it
        if (killer == null) {
            if (BountyManager.hasBounty(victim.getUniqueId())) {
                BountyData bounty = BountyManager.getBounty(victim.getUniqueId());
                BountyManager.removeBounty(victim.getUniqueId());
                String currencyName = getCurrencyName(bounty.getCurrency());
                Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " died without a killer! " +
                    "Bounty of " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + " has been lost!");
            }
            return;
        }

        // Check if killer is the same as victim (suicide)
        if (killer.equals(victim)) {
            if (BountyManager.hasBounty(victim.getUniqueId())) {
                BountyData bounty = BountyManager.getBounty(victim.getUniqueId());
                BountyManager.removeBounty(victim.getUniqueId());
                String currencyName = getCurrencyName(bounty.getCurrency());
                Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " committed suicide! " +
                    "Bounty of " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + " has been lost!");
            }
            return;
        }

        // Check for reverse bounty scenario: target kills hunter
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
            // Award the bounty to the killer (normal case)
            BountyManager.claimBounty(killer, victim);
            return;
        }
    }
    
    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Check if this player has a bounty
        if (BountyManager.hasBounty(player.getUniqueId())) {
            BountyData bounty = BountyManager.getBounty(player.getUniqueId());
            String currencyName = getCurrencyName(bounty.getCurrency());
            
            // Notify the player and server about their bounty
            String hunterInfo = bounty.isAccepted() ? 
                " and is being hunted by " + bounty.getHunterName() : "";
            player.sendMessage(ChatColor.RED + "⚠ WARNING: You have an active bounty of " + 
                bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + 
                " placed by " + bounty.getPlacedBy() + hunterInfo + "!");
            
            String broadcastMessage = bounty.isAccepted() ?
                player.getName() + " has joined with an active bounty (being hunted by " + bounty.getHunterName() + ")!" :
                player.getName() + " has joined with an active bounty!";
            Bukkit.broadcastMessage(ChatColor.GOLD + broadcastMessage);
            
            // If bounty is accepted and hunter is online, activate modes
            if (bounty.isAccepted()) {
                Player hunter = Bukkit.getPlayer(bounty.getHunterUUID());
                if (hunter != null) {
                    PlayerModeManager.setHunterMode(hunter, player);
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
    
    private String getCurrencyName(BountyData.CurrencyType currency) {
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
