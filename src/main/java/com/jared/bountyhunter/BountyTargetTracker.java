package com.jared.bountyhunter;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BountyTargetTracker {
    private static HashMap<UUID, Player> playerTargets = new HashMap<>();
    
    public static void setTarget(Player player, Player target) {
        playerTargets.put(player.getUniqueId(), target);
    }
    
    public static Player getTarget(Player player) {
        return playerTargets.get(player.getUniqueId());
    }
    
    public static void clearTarget(Player player) {
        playerTargets.remove(player.getUniqueId());
    }
}
