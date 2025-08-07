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
        if (!BountyManager.hasBounty(victim.getUniqueId())) {
            return; // No bounty, nothing to do
        }

        // If there's no killer (environmental death), remove the bounty
        if (killer == null) {
            BountyData bounty = BountyManager.getBounty(victim.getUniqueId());
            BountyManager.removeBounty(victim.getUniqueId());
            String currencyName = getCurrencyName(bounty.getCurrency());
            Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " died without a killer! " +
                "Bounty of " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + " has been lost!");
            return;
        }

        // Check if killer is the same as victim (suicide)
        if (killer.equals(victim)) {
            BountyData bounty = BountyManager.getBounty(victim.getUniqueId());
            BountyManager.removeBounty(victim.getUniqueId());
            String currencyName = getCurrencyName(bounty.getCurrency());
            Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " committed suicide! " +
                "Bounty of " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + " has been lost!");
            return;
        }

        // Award the bounty to the killer
        BountyManager.claimBounty(killer, victim);
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
