package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.UUID;

public class EnhancedTracker {
    
    private static BukkitTask trackingTask;
    private static HashMap<UUID, TrackingData> hunterData = new HashMap<>();
    
    public static void startEnhancedTracking(BountyHunter plugin) {
        if (trackingTask != null) {
            trackingTask.cancel();
        }
        
        // Enhanced tracking every 1 second for hunters
        trackingTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (PlayerModeManager.isHunter(player)) {
                    updateEnhancedTracking(player);
                }
            }
        }, 0L, 20L); // Every 1 second (20 ticks)
    }
    
    public static void stopEnhancedTracking() {
        if (trackingTask != null) {
            trackingTask.cancel();
            trackingTask = null;
        }
        hunterData.clear();
    }
    
    private static void updateEnhancedTracking(Player hunter) {
        Player target = PlayerModeManager.getHunterTarget(hunter);
        
        if (target == null || !target.isOnline()) {
            hunterData.remove(hunter.getUniqueId());
            return;
        }
        
        UUID hunterUUID = hunter.getUniqueId();
        TrackingData data = hunterData.computeIfAbsent(hunterUUID, k -> new TrackingData());
        
        Location hunterLoc = hunter.getLocation();
        Location targetLoc = target.getLocation();
        
        // Update compass
        hunter.setCompassTarget(targetLoc);
        
        // Calculate tracking data
        double distance = hunterLoc.distance(targetLoc);
        String direction = getDirection(hunterLoc, targetLoc);
        boolean sameDimension = hunterLoc.getWorld().equals(targetLoc.getWorld());
        
        // Update tracking data
        data.updateData(distance, direction, sameDimension, target.getName());
        
        // Send periodic updates
        if (data.shouldSendUpdate()) {
            sendTrackingUpdate(hunter, data);
        }
        
        // Give tracking items if hunter doesn't have them
        giveTrackingTools(hunter);
    }
    
    private static void sendTrackingUpdate(Player hunter, TrackingData data) {
        if (!data.sameDimension) {
            hunter.sendMessage(ChatColor.RED + "🌍 Target is in a different dimension!");
            return;
        }
        
        String message = getTrackingMessage(data.distance, data.direction, data.targetName);
        if (message != null) {
            hunter.sendMessage(message);
        }
        
        // Send title with distance instead of action bar
        String subtitle = ChatColor.YELLOW + "🎯 " + data.targetName + ": " + 
                         String.format("%.1f", data.distance) + "m " + data.direction;
        hunter.sendTitle("", subtitle, 0, 30, 0);
    }
    
    private static String getTrackingMessage(double distance, String direction, String targetName) {
        if (distance <= 10) {
            return ChatColor.DARK_RED + "⚡ " + targetName + " is VERY CLOSE! " + 
                   String.format("%.1f", distance) + "m " + direction;
        } else if (distance <= 25) {
            return ChatColor.RED + "🔥 " + targetName + " is close! " + 
                   String.format("%.1f", distance) + "m " + direction;
        } else if (distance <= 50) {
            return ChatColor.YELLOW + "🧭 " + targetName + " is nearby - " + 
                   String.format("%.1f", distance) + "m " + direction;
        } else if (distance <= 100) {
            return ChatColor.GRAY + "📍 Tracking " + targetName + " - " + 
                   String.format("%.0f", distance) + "m " + direction;
        }
        return null; // Don't spam for very long distances
    }
    
    private static void giveTrackingTools(Player hunter) {
        // Give compass if they don't have one
        if (!hunter.getInventory().contains(Material.COMPASS)) {
            ItemStack compass = new ItemStack(Material.COMPASS);
            var meta = compass.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.RED + "Hunter's Compass");
                compass.setItemMeta(meta);
            }
            hunter.getInventory().addItem(compass);
            hunter.sendMessage(ChatColor.GREEN + "🧭 You received a Hunter's Compass!");
        }
        
        // Give spyglass for enhanced tracking if they don't have one (if available)
        try {
            Material spyglass = Material.valueOf("SPYGLASS");
            if (!hunter.getInventory().contains(spyglass)) {
                ItemStack spyglassItem = new ItemStack(spyglass);
                var meta = spyglassItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.BLUE + "Hunter's Spyglass");
                    spyglassItem.setItemMeta(meta);
                }
                hunter.getInventory().addItem(spyglassItem);
                hunter.sendMessage(ChatColor.GREEN + "🔭 You received a Hunter's Spyglass for long-range tracking!");
            }
        } catch (IllegalArgumentException e) {
            // Spyglass not available in this version, skip it
        }
    }
    
    private static String getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360;
        
        if (angle >= 337.5 || angle < 22.5) return "East";
        else if (angle >= 22.5 && angle < 67.5) return "Southeast";
        else if (angle >= 67.5 && angle < 112.5) return "South";
        else if (angle >= 112.5 && angle < 157.5) return "Southwest";
        else if (angle >= 157.5 && angle < 202.5) return "West";
        else if (angle >= 202.5 && angle < 247.5) return "Northwest";
        else if (angle >= 247.5 && angle < 292.5) return "North";
        else return "Northeast";
    }
    
    public static void clearHunterData(Player hunter) {
        hunterData.remove(hunter.getUniqueId());
    }
    
    /**
     * Get detailed tracking info for a hunter
     */
    public static String getDetailedTrackingInfo(Player hunter) {
        Player target = PlayerModeManager.getHunterTarget(hunter);
        
        if (target == null || !target.isOnline()) {
            return ChatColor.RED + "❌ No target to track or target is offline.";
        }
        
        Location hunterLoc = hunter.getLocation();
        Location targetLoc = target.getLocation();
        
        if (!hunterLoc.getWorld().equals(targetLoc.getWorld())) {
            return ChatColor.RED + "🌍 Target is in a different dimension: " + targetLoc.getWorld().getName();
        }
        
        double distance = hunterLoc.distance(targetLoc);
        String direction = getDirection(hunterLoc, targetLoc);
        
        StringBuilder info = new StringBuilder();
        info.append(ChatColor.YELLOW).append("=== Enhanced Tracking Info ===\n");
        info.append(ChatColor.GRAY).append("Target: ").append(ChatColor.WHITE).append(target.getName()).append("\n");
        info.append(ChatColor.GRAY).append("Distance: ").append(ChatColor.WHITE).append(String.format("%.1f", distance)).append(" blocks\n");
        info.append(ChatColor.GRAY).append("Direction: ").append(ChatColor.WHITE).append(direction).append("\n");
        info.append(ChatColor.GRAY).append("World: ").append(ChatColor.WHITE).append(targetLoc.getWorld().getName()).append("\n");
        
        // Add proximity warning
        if (distance <= 25) {
            info.append(ChatColor.RED).append("⚠ Target is very close - prepare for combat!\n");
        } else if (distance <= 100) {
            info.append(ChatColor.YELLOW).append("🔍 Target is in tracking range\n");
        }
        
        info.append(ChatColor.GRAY).append("Use your compass and spyglass for navigation!");
        
        return info.toString();
    }
    
    private static class TrackingData {
        double distance;
        String direction;
        boolean sameDimension;
        String targetName;
        long lastUpdate;
        
        private static final long UPDATE_INTERVAL = 15000; // 15 seconds between chat messages
        
        void updateData(double distance, String direction, boolean sameDimension, String targetName) {
            this.distance = distance;
            this.direction = direction;
            this.sameDimension = sameDimension;
            this.targetName = targetName;
        }
        
        boolean shouldSendUpdate() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdate > UPDATE_INTERVAL) {
                lastUpdate = currentTime;
                return true;
            }
            return false;
        }
    }
}
