package com.jared.bountyhunter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BountyCooldownManager {
    private static Plugin plugin;
    private static File cooldownFile;
    private static FileConfiguration cooldownConfig;
    private static HashMap<UUID, Long> cooldowns = new HashMap<>(); // Player UUID -> Cooldown end time
    
    private static final long COOLDOWN_DURATION = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
    
    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
        cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");
        
        if (!cooldownFile.exists()) {
            try {
                cooldownFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create cooldowns.yml: " + e.getMessage());
            }
        }
        
        cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);
        loadCooldowns();
    }
    
    /**
     * Adds a cooldown for a player after their bounty is claimed
     */
    public static void addCooldown(UUID playerUUID) {
        long cooldownEndTime = System.currentTimeMillis() + COOLDOWN_DURATION;
        cooldowns.put(playerUUID, cooldownEndTime);
        
        // Save to file
        cooldownConfig.set("cooldowns." + playerUUID.toString(), cooldownEndTime);
        saveCooldownFile();
        
        plugin.getLogger().info("Added 24-hour bounty cooldown for player " + playerUUID);
    }
    
    /**
     * Checks if a player is on cooldown (cannot have bounties placed on them)
     */
    public static boolean isOnCooldown(UUID playerUUID) {
        Long cooldownEndTime = cooldowns.get(playerUUID);
        if (cooldownEndTime == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= cooldownEndTime) {
            // Cooldown has expired, remove it
            removeCooldown(playerUUID);
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the remaining cooldown time in milliseconds
     */
    public static long getRemainingCooldown(UUID playerUUID) {
        Long cooldownEndTime = cooldowns.get(playerUUID);
        if (cooldownEndTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= cooldownEndTime) {
            removeCooldown(playerUUID);
            return 0;
        }
        
        return cooldownEndTime - currentTime;
    }
    
    /**
     * Removes a cooldown (when it expires or manually cleared)
     */
    public static void removeCooldown(UUID playerUUID) {
        cooldowns.remove(playerUUID);
        cooldownConfig.set("cooldowns." + playerUUID.toString(), null);
        saveCooldownFile();
    }
    
    /**
     * Formats remaining time as a human-readable string
     */
    public static String formatRemainingTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        hours = hours % 24;
        minutes = minutes % 60;
        seconds = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    /**
     * Gets all players currently on cooldown
     */
    public static HashMap<UUID, Long> getAllCooldowns() {
        // Clean expired cooldowns first
        cleanExpiredCooldowns();
        return new HashMap<>(cooldowns);
    }
    
    /**
     * Cleans up expired cooldowns
     */
    public static void cleanExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                // Remove from config too
                cooldownConfig.set("cooldowns." + entry.getKey().toString(), null);
                return true;
            }
            return false;
        });
        saveCooldownFile();
    }
    
    /**
     * Admin command to clear a specific cooldown
     */
    public static boolean clearCooldown(UUID playerUUID) {
        if (cooldowns.containsKey(playerUUID)) {
            removeCooldown(playerUUID);
            return true;
        }
        return false;
    }
    
    /**
     * Admin command to clear all cooldowns
     */
    public static int clearAllCooldowns() {
        int count = cooldowns.size();
        cooldowns.clear();
        cooldownConfig.set("cooldowns", null);
        saveCooldownFile();
        return count;
    }
    
    private static void loadCooldowns() {
        cooldowns.clear();
        
        ConfigurationSection cooldownsSection = cooldownConfig.getConfigurationSection("cooldowns");
        if (cooldownsSection == null) {
            return;
        }
        
        for (String uuidString : cooldownsSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                long cooldownEndTime = cooldownsSection.getLong(uuidString);
                cooldowns.put(uuid, cooldownEndTime);
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid cooldown data for " + uuidString + ": " + e.getMessage());
            }
        }
        
        // Clean expired cooldowns on load
        cleanExpiredCooldowns();
        
        plugin.getLogger().info("Loaded " + cooldowns.size() + " active bounty cooldowns from file.");
    }
    
    private static void saveCooldownFile() {
        try {
            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save cooldowns file: " + e.getMessage());
        }
    }
}
