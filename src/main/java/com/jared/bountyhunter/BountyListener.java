package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class BountyListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Check if victim has a bounty
        if (!BountyCommand.bounties.containsKey(victim.getUniqueId())) {
            return; // No bounty, nothing to do
        }

        // If there's no killer (environmental death), remove the bounty
        if (killer == null) {
            double lostBounty = BountyCommand.bounties.remove(victim.getUniqueId());
            Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " died without a killer! " +
                "Bounty of $" + String.format("%.2f", lostBounty) + " has been lost!");
            return;
        }

        // Check if killer is the same as victim (suicide)
        if (killer.equals(victim)) {
            double lostBounty = BountyCommand.bounties.remove(victim.getUniqueId());
            Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " committed suicide! " +
                "Bounty of $" + String.format("%.2f", lostBounty) + " has been lost!");
            return;
        }

        // Award the bounty to the killer
        double reward = BountyCommand.bounties.remove(victim.getUniqueId());
        
        try {
            BountyHunter.getEconomy().depositPlayer(killer, reward);
            killer.sendMessage(ChatColor.GREEN + "You claimed a bounty of $" + 
                String.format("%.2f", reward) + " for killing " + victim.getName() + "!");
            Bukkit.broadcastMessage(ChatColor.AQUA + killer.getName() + " claimed the bounty on " + 
                victim.getName() + " for $" + String.format("%.2f", reward) + "!");
        } catch (Exception e) {
            // If economy plugin fails, still remove the bounty but log the error
            Bukkit.getLogger().warning("Failed to deposit bounty reward to " + killer.getName() + 
                " for killing " + victim.getName() + ". Error: " + e.getMessage());
            killer.sendMessage(ChatColor.RED + "Error claiming bounty reward. Contact an administrator.");
        }
    }
}
