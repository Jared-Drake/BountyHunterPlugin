package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CompassTracker extends BukkitRunnable {
    
    private static CompassTracker instance;
    private static HashMap<UUID, Long> lastNotificationTime = new HashMap<>();
    private static final long NOTIFICATION_COOLDOWN = 30000; // 30 seconds
    
    public static void startTracking(BountyHunter plugin) {
        if (instance != null) {
            instance.cancel();
        }
        instance = new CompassTracker();
        instance.runTaskTimer(plugin, 0L, 40L); // Run every 2 seconds (40 ticks) for enhanced tracking
    }
    
    public static void stopTracking() {
        if (instance != null) {
            instance.cancel();
            instance = null;
        }
    }
    
    @Override
    public void run() {
        // Update compass for all active hunters
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerModeManager.isHunter(player)) {
                updateCompassForHunter(player);
            }
        }
    }
    
    private void updateCompassForHunter(Player hunter) {
        Player target = PlayerModeManager.getHunterTarget(hunter);
        
        if (target != null && target.isOnline()) {
            // Update compass to point to target's current location
            hunter.setCompassTarget(target.getLocation());
            
            // Check if we should send a distance update notification
            UUID hunterUUID = hunter.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastNotified = lastNotificationTime.get(hunterUUID);
            
            if (lastNotified == null || currentTime - lastNotified > NOTIFICATION_COOLDOWN) {
                double distance = hunter.getLocation().distance(target.getLocation());
                
                // Send periodic distance updates with different messages based on distance
                String message = getDistanceMessage(distance, target.getName());
                if (message != null) {
                    hunter.sendMessage(message);
                    lastNotificationTime.put(hunterUUID, currentTime);
                }
            }
        } else {
            // Target went offline, clear tracking
            lastNotificationTime.remove(hunter.getUniqueId());
        }
    }
    
    private String getDistanceMessage(double distance, String targetName) {
        // Only send messages for certain distance ranges to avoid spam
        if (distance <= 20) {
            return ChatColor.RED + "🎯 " + targetName + " is very close! (" + String.format("%.1f", distance) + " blocks)";
        } else if (distance <= 50) {
            return ChatColor.YELLOW + "🧭 " + targetName + " is nearby (" + String.format("%.1f", distance) + " blocks)";
        } else if (distance <= 100) {
            return ChatColor.GRAY + "📍 Tracking " + targetName + " (" + String.format("%.1f", distance) + " blocks away)";
        } else if (distance <= 500) {
            // Only notify every other time for medium distances
            return Math.random() < 0.3 ? ChatColor.GRAY + "🗺️ " + targetName + " is " + String.format("%.0f", distance) + " blocks away" : null;
        }
        // Don't spam for very long distances
        return null;
    }
    
    /**
     * Manually trigger compass update for a specific hunter
     */
    public static void updateCompass(Player hunter) {
        if (PlayerModeManager.isHunter(hunter)) {
            Player target = PlayerModeManager.getHunterTarget(hunter);
            if (target != null && target.isOnline()) {
                hunter.setCompassTarget(target.getLocation());
            }
        }
    }
    
    /**
     * Clear tracking data for a player
     */
    public static void clearTracking(Player player) {
        lastNotificationTime.remove(player.getUniqueId());
    }
}
