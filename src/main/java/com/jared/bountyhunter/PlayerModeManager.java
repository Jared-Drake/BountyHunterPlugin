package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class PlayerModeManager {
    
    public enum PlayerMode {
        NORMAL,
        BOUNTY_HUNTER,
        TARGET
    }
    
    // Track player modes
    private static HashMap<UUID, PlayerMode> playerModes = new HashMap<>();
    // Track hunter-target relationships
    private static HashMap<UUID, UUID> hunterTargetMap = new HashMap<>(); // hunter -> target
    private static HashMap<UUID, UUID> targetHunterMap = new HashMap<>(); // target -> hunter
    
    /**
     * Sets a player into bounty hunter mode with their target
     */
    public static void setHunterMode(Player hunter, Player target) {
        UUID hunterUUID = hunter.getUniqueId();
        UUID targetUUID = target.getUniqueId();
        
        // Set modes
        playerModes.put(hunterUUID, PlayerMode.BOUNTY_HUNTER);
        playerModes.put(targetUUID, PlayerMode.TARGET);
        
        // Set relationships
        hunterTargetMap.put(hunterUUID, targetUUID);
        targetHunterMap.put(targetUUID, hunterUUID);
        
        // Apply hunter effects
        applyHunterEffects(hunter);
        
        // Apply target effects
        applyTargetEffects(target);
        
        // Send notifications
        notifyHunterModeActivated(hunter, target);
        notifyTargetModeActivated(target, hunter);
    }
    
    /**
     * Removes a player from hunter mode
     */
    public static void clearHunterMode(Player hunter) {
        UUID hunterUUID = hunter.getUniqueId();
        UUID targetUUID = hunterTargetMap.get(hunterUUID);
        
        if (targetUUID != null) {
            Player target = Bukkit.getPlayer(targetUUID);
            
            // Clear modes
            playerModes.put(hunterUUID, PlayerMode.NORMAL);
            if (target != null) {
                playerModes.put(targetUUID, PlayerMode.NORMAL);
            }
            
            // Clear relationships
            hunterTargetMap.remove(hunterUUID);
            targetHunterMap.remove(targetUUID);
            
            // Remove effects
            removeHunterEffects(hunter);
            if (target != null) {
                removeTargetEffects(target);
            }
            
            // Clear compass tracking
            CompassTracker.clearTracking(hunter);
            EnhancedTracker.clearHunterData(hunter);
            
            // Send notifications
            notifyHunterModeDeactivated(hunter, target);
            if (target != null) {
                notifyTargetModeDeactivated(target, hunter);
            }
        }
    }
    
    /**
     * Removes a player from target mode
     */
    public static void clearTargetMode(Player target) {
        UUID targetUUID = target.getUniqueId();
        UUID hunterUUID = targetHunterMap.get(targetUUID);
        
        if (hunterUUID != null) {
            Player hunter = Bukkit.getPlayer(hunterUUID);
            
            // Clear modes
            playerModes.put(targetUUID, PlayerMode.NORMAL);
            if (hunter != null) {
                playerModes.put(hunterUUID, PlayerMode.NORMAL);
            }
            
            // Clear relationships
            targetHunterMap.remove(targetUUID);
            hunterTargetMap.remove(hunterUUID);
            
            // Remove effects
            removeTargetEffects(target);
            // Clear target's compass tracking
            target.setCompassTarget(target.getWorld().getSpawnLocation());
            
            if (hunter != null) {
                removeHunterEffects(hunter);
                // Clear compass tracking
                CompassTracker.clearTracking(hunter);
                EnhancedTracker.clearHunterData(hunter);
            }
            
            // Send notifications
            notifyTargetModeDeactivated(target, hunter);
            if (hunter != null) {
                notifyHunterModeDeactivated(hunter, target);
            }
        }
    }
    
    /**
     * Gets the current mode of a player
     */
    public static PlayerMode getPlayerMode(Player player) {
        return playerModes.getOrDefault(player.getUniqueId(), PlayerMode.NORMAL);
    }
    
    /**
     * Gets the target of a hunter
     */
    public static Player getHunterTarget(Player hunter) {
        UUID targetUUID = hunterTargetMap.get(hunter.getUniqueId());
        return targetUUID != null ? Bukkit.getPlayer(targetUUID) : null;
    }
    
    /**
     * Gets the hunter of a target
     */
    public static Player getTargetHunter(Player target) {
        UUID hunterUUID = targetHunterMap.get(target.getUniqueId());
        return hunterUUID != null ? Bukkit.getPlayer(hunterUUID) : null;
    }
    
    /**
     * Checks if a player is in hunter mode
     */
    public static boolean isHunter(Player player) {
        return getPlayerMode(player) == PlayerMode.BOUNTY_HUNTER;
    }
    
    /**
     * Checks if a player is in target mode
     */
    public static boolean isTarget(Player player) {
        return getPlayerMode(player) == PlayerMode.TARGET;
    }
    
    /**
     * Cleans up mode when a player disconnects
     */
    public static void handlePlayerDisconnect(Player player) {
        PlayerMode mode = getPlayerMode(player);
        if (mode == PlayerMode.BOUNTY_HUNTER) {
            clearHunterMode(player);
        } else if (mode == PlayerMode.TARGET) {
            clearTargetMode(player);
        }
    }
    
    private static void applyHunterEffects(Player hunter) {
        // Give hunter night vision for enhanced tracking
        hunter.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        
        // Play hunter sound
        hunter.playSound(hunter.getLocation(), Sound.ENTITY_WOLF_HOWL, 1.0f, 1.0f);
    }
    
    private static void applyTargetEffects(Player target) {
        // Play target sound (no speed boost)
        target.playSound(target.getLocation(), Sound.ENTITY_VILLAGER_HURT, 1.0f, 0.8f);
    }
    
    private static void removeHunterEffects(Player hunter) {
        hunter.removePotionEffect(PotionEffectType.NIGHT_VISION);
        
        // Remove tracking tools (compass and spyglass)
        removeTrackingTools(hunter);
    }
    
    private static void removeTrackingTools(Player hunter) {
        // Remove compass
        hunter.getInventory().remove(Material.COMPASS);
        
        // Remove spyglass if available in this version
        try {
            Material spyglass = Material.valueOf("SPYGLASS");
            hunter.getInventory().remove(spyglass);
        } catch (IllegalArgumentException e) {
            // Spyglass not available in this version, skip it
        }
    }
    
    private static void removeTargetEffects(Player target) {
        // No effects to remove for targets now
    }
    
    private static void notifyHunterModeActivated(Player hunter, Player target) {
        hunter.sendMessage("");
        hunter.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════");
        hunter.sendMessage(ChatColor.RED + "⚔ " + ChatColor.BOLD + "BOUNTY HUNTER MODE ACTIVATED" + ChatColor.RED + " ⚔");
        hunter.sendMessage(ChatColor.GRAY + "Target: " + ChatColor.WHITE + target.getName());
        hunter.sendMessage(ChatColor.GRAY + "You have enhanced night vision and tracking!");
        hunter.sendMessage(ChatColor.YELLOW + "Hunt wisely, hunter...");
        hunter.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════");
        hunter.sendMessage("");
        
        // Title message
        hunter.sendTitle(ChatColor.RED + "⚔ HUNTER MODE ⚔", 
                        ChatColor.GRAY + "Target: " + target.getName(), 10, 70, 20);
    }
    
    private static void notifyTargetModeActivated(Player target, Player hunter) {
        target.sendMessage("");
        target.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════");
        target.sendMessage(ChatColor.RED + "⚠ " + ChatColor.BOLD + "TARGET MODE ACTIVATED" + ChatColor.RED + " ⚠");
        target.sendMessage(ChatColor.GRAY + "Hunter: " + ChatColor.WHITE + hunter.getName());
        target.sendMessage(ChatColor.GRAY + "You are being hunted!");
        target.sendMessage(ChatColor.YELLOW + "Stay alert and survive...");
        target.sendMessage(ChatColor.DARK_RED + "═══════════════════════════════");
        target.sendMessage("");
        
        // Title message
        target.sendTitle(ChatColor.RED + "⚠ TARGET MODE ⚠", 
                        ChatColor.GRAY + "Hunter: " + hunter.getName(), 10, 70, 20);
    }
    
    private static void notifyHunterModeDeactivated(Player hunter, Player target) {
        String targetName = target != null ? target.getName() : "Unknown";
        hunter.sendMessage(ChatColor.YELLOW + "⚔ Bounty hunter mode deactivated. Target: " + targetName);
        hunter.sendTitle(ChatColor.YELLOW + "Hunter Mode Ended", "", 10, 40, 10);
    }
    
    private static void notifyTargetModeDeactivated(Player target, Player hunter) {
        String hunterName = hunter != null ? hunter.getName() : "Unknown";
        target.sendMessage(ChatColor.YELLOW + "⚠ Target mode deactivated. Hunter: " + hunterName);
        target.sendTitle(ChatColor.YELLOW + "Target Mode Ended", "", 10, 40, 10);
    }
}
