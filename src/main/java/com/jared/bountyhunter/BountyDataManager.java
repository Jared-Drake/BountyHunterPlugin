package com.jared.bountyhunter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class BountyDataManager {
    private static Plugin plugin;
    private static File bountyFile;
    private static FileConfiguration bountyConfig;
    
    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
        bountyFile = new File(plugin.getDataFolder(), "bounties.yml");
        
        if (!bountyFile.exists()) {
            plugin.saveResource("bounties.yml", false);
        }
        
        bountyConfig = YamlConfiguration.loadConfiguration(bountyFile);
    }
    
    public static void saveBounties(HashMap<UUID, BountyData> bounties) {
        // Clear existing data
        bountyConfig.set("bounties", null);
        
        // Save each bounty
        for (UUID targetUUID : bounties.keySet()) {
            BountyData bounty = bounties.get(targetUUID);
            String path = "bounties." + targetUUID.toString();
            
            bountyConfig.set(path + ".targetUUID", targetUUID.toString());
            bountyConfig.set(path + ".placedByUUID", bounty.getPlacedByUUID().toString());
            bountyConfig.set(path + ".placedByName", bounty.getPlacedBy());
            bountyConfig.set(path + ".currency", bounty.getCurrency().name());
            bountyConfig.set(path + ".amount", bounty.getAmount());
        }
        
        try {
            bountyConfig.save(bountyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save bounties: " + e.getMessage());
        }
    }
    
    public static HashMap<UUID, BountyData> loadBounties() {
        HashMap<UUID, BountyData> bounties = new HashMap<>();
        
        ConfigurationSection bountiesSection = bountyConfig.getConfigurationSection("bounties");
        if (bountiesSection == null) {
            return bounties;
        }
        
        for (String targetUUIDString : bountiesSection.getKeys(false)) {
            try {
                UUID targetUUID = UUID.fromString(targetUUIDString);
                String path = "bounties." + targetUUIDString;
                
                UUID placedByUUID = UUID.fromString(bountiesSection.getString(path + ".placedByUUID"));
                String placedByName = bountiesSection.getString(path + ".placedByName");
                BountyData.CurrencyType currency = BountyData.CurrencyType.valueOf(
                    bountiesSection.getString(path + ".currency"));
                int amount = bountiesSection.getInt(path + ".amount");
                
                BountyData bounty = new BountyData(targetUUID, placedByUUID, placedByName, currency, amount);
                bounties.put(targetUUID, bounty);
                
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load bounty for " + targetUUIDString + ": " + e.getMessage());
            }
        }
        
        return bounties;
    }
    
    public static void saveBounty(UUID targetUUID, BountyData bounty) {
        String path = "bounties." + targetUUID.toString();
        
        bountyConfig.set(path + ".targetUUID", targetUUID.toString());
        bountyConfig.set(path + ".placedByUUID", bounty.getPlacedByUUID().toString());
        bountyConfig.set(path + ".placedByName", bounty.getPlacedBy());
        bountyConfig.set(path + ".currency", bounty.getCurrency().name());
        bountyConfig.set(path + ".amount", bounty.getAmount());
        
        try {
            bountyConfig.save(bountyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save bounty: " + e.getMessage());
        }
    }
    
    public static void removeBounty(UUID targetUUID) {
        bountyConfig.set("bounties." + targetUUID.toString(), null);
        
        try {
            bountyConfig.save(bountyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to remove bounty: " + e.getMessage());
        }
    }
}
