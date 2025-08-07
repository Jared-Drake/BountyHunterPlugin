package com.jared.bountyhunter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (PlayerSelectionManager.isInSelectionMode(player)) {
            event.setCancelled(true);
            String targetName = event.getMessage();
            
            // Handle the selection on the main thread
            player.getServer().getScheduler().runTask(player.getServer().getPluginManager().getPlugin("BountyHunter"), () -> {
                PlayerSelectionManager.handlePlayerSelection(player, targetName);
            });
        }
    }
}
