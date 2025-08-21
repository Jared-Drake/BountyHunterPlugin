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
        // Update compass for all active hunters and targets
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (PlayerModeManager.isHunter(player)) {
                updateCompassForHunter(player);
            }
            if (PlayerModeManager.isTarget(player)) {
                updateCompassForTarget(player);
            }
        }
    }
    
    private void updateCompassForHunter(Player hunter) {
        Player target = PlayerModeManager.getHunterTarget(hunter);
        
        if (target != null && target.isOnline()) {
            double distance = hunter.getLocation().distance(target.getLocation());
            
            // Only update compass if within 1000 blocks
            if (distance <= 1000) {
                // Update compass to point to target's current location
                hunter.setCompassTarget(target.getLocation());
                
                // Check if we should send a distance update notification
                UUID hunterUUID = hunter.getUniqueId();
                long currentTime = System.currentTimeMillis();
                Long lastNotified = lastNotificationTime.get(hunterUUID);
                
                if (lastNotified == null || currentTime - lastNotified > NOTIFICATION_COOLDOWN) {
                    // Send periodic distance updates with different messages based on distance
                    String message = getDistanceMessage(distance, target.getName());
                    if (message != null) {
                        hunter.sendMessage(message);
                        lastNotificationTime.put(hunterUUID, currentTime);
                    }
                }
            } else {
                // Target is beyond 1000 blocks - don't update compass but notify occasionally
                UUID hunterUUID = hunter.getUniqueId();
                long currentTime = System.currentTimeMillis();
                Long lastNotified = lastNotificationTime.get(hunterUUID);
                
                if (lastNotified == null || currentTime - lastNotified > NOTIFICATION_COOLDOWN) {
                    hunter.sendMessage(ChatColor.GRAY + "🗺️ " + target.getName() + " is beyond tracking range (" + String.format("%.0f", distance) + " blocks away)");
                    hunter.sendMessage(ChatColor.YELLOW + "💡 Move closer to " + target.getName() + " to activate compass tracking!");
                    lastNotificationTime.put(hunterUUID, currentTime);
                }
            }
        } else {
            // Target went offline, clear tracking
            lastNotificationTime.remove(hunter.getUniqueId());
        }
    }
    
    private void updateCompassForTarget(Player target) {
        Player hunter = PlayerModeManager.getTargetHunter(target);
        
        if (hunter != null && hunter.isOnline()) {
            double distance = target.getLocation().distance(hunter.getLocation());
            
            // Update compass to point to hunter's current location (unlimited range for targets)
            target.setCompassTarget(hunter.getLocation());
            
            // Check if we should send a distance update notification for the target
            UUID targetUUID = target.getUniqueId();
            long currentTime = System.currentTimeMillis();
            Long lastNotified = lastNotificationTime.get(targetUUID);
            
            if (lastNotified == null || currentTime - lastNotified > NOTIFICATION_COOLDOWN) {
                // Send periodic distance updates with different messages for targets
                String message = getTargetDistanceMessage(distance, hunter.getName());
                if (message != null) {
                    target.sendMessage(message);
                    lastNotificationTime.put(targetUUID, currentTime);
                }
                
                // Inform target about hunter's tracking limitations
                if (distance > 1000 && Math.random() < 0.3) { // 30% chance every notification cycle
                    target.sendMessage(ChatColor.GREEN + "💡 Your hunter's compass tracking is limited to 1000 blocks!");
                    target.sendMessage(ChatColor.GRAY + "You have unlimited tracking range to help you survive!");
                }
            }
        } else {
            // Hunter went offline, clear tracking
            lastNotificationTime.remove(target.getUniqueId());
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
            // Only notify occasionally for medium distances
            return Math.random() < 0.3 ? ChatColor.GRAY + "🗺️ " + targetName + " is " + String.format("%.0f", distance) + " blocks away" : null;
        } else if (distance <= 1000) {
            // Notify when within tracking range but still far
            return Math.random() < 0.2 ? ChatColor.GRAY + "🗺️ " + targetName + " is " + String.format("%.0f", distance) + " blocks away (within tracking range)" : null;
        }
        // Don't spam for very long distances
        return null;
    }
    
    private String getTargetDistanceMessage(double distance, String hunterName) {
        // Send messages for targets to track their hunter
        if (distance <= 20) {
            return ChatColor.RED + "⚠ " + hunterName + " is very close! (" + String.format("%.1f", distance) + " blocks)";
        } else if (distance <= 50) {
            return ChatColor.YELLOW + "⚠ " + hunterName + " is nearby (" + String.format("%.1f", distance) + " blocks)";
        } else if (distance <= 100) {
            return ChatColor.GRAY + "⚠ " + hunterName + " is approaching (" + String.format("%.1f", distance) + " blocks away)";
        } else if (distance <= 500) {
            // Only notify occasionally for medium distances
            return Math.random() < 0.2 ? ChatColor.GRAY + "⚠ " + hunterName + " is " + String.format("%.0f", distance) + " blocks away" : null;
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
                double distance = hunter.getLocation().distance(target.getLocation());
                if (distance <= 1000) {
                    hunter.setCompassTarget(target.getLocation());
                    hunter.sendMessage(ChatColor.GREEN + "🧭 Compass updated to point to " + target.getName() + "!");
                } else {
                    hunter.sendMessage(ChatColor.RED + "❌ " + target.getName() + " is too far away (" + String.format("%.0f", distance) + " blocks)");
                    hunter.sendMessage(ChatColor.YELLOW + "💡 Move within 1000 blocks to activate compass tracking!");
                }
            }
        }
    }
    
    /**
     * Manually trigger compass update for a specific target
     */
    public static void updateTargetCompass(Player target) {
        if (PlayerModeManager.isTarget(target)) {
            Player hunter = PlayerModeManager.getTargetHunter(target);
            if (hunter != null && hunter.isOnline()) {
                target.setCompassTarget(hunter.getLocation());
                double distance = target.getLocation().distance(hunter.getLocation());
                target.sendMessage(ChatColor.GREEN + "🧭 Compass updated to point to " + hunter.getName() + "!");
                target.sendMessage(ChatColor.GRAY + "Distance: " + ChatColor.WHITE + String.format("%.1f", distance) + " blocks");
                target.sendMessage(ChatColor.GREEN + "💡 You have unlimited tracking range to help you survive!");
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
