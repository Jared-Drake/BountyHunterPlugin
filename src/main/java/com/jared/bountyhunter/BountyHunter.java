package com.jared.bountyhunter;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class BountyHunter extends JavaPlugin {
    private static Economy econ = null;

    @Override
    public void onEnable() {
        // Check for Vault dependency
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling BountyHunter plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Set economy in BountyManager
        BountyManager.setEconomy(econ);
        
        // Initialize data managers
        BountyDataManager.initialize(this);
        PlayerDataManager.initialize(this);
        BountyCooldownManager.initialize(this);
        
        // Load existing bounties
        BountyManager.loadBountiesFromFile();
        
        getCommand("bounty").setExecutor(new BountyCommand());
        getServer().getPluginManager().registerEvents(new BountyListener(), this);
        getServer().getPluginManager().registerEvents(new BountyGUIListener(), this);
        
        // Start compass tracking for active hunters
        CompassTracker.startTracking(this);
        EnhancedTracker.startEnhancedTracking(this);
        
        getLogger().info("BountyHunter enabled!");
        getLogger().info("Economy system: " + econ.getName());
        getLogger().info("Loaded " + BountyManager.getBounties().size() + " bounties from file.");
    }
    
    @Override
    public void onDisable() {
        // Stop compass tracking
        CompassTracker.stopTracking();
        EnhancedTracker.stopEnhancedTracking();
        
        // Save all bounties on shutdown
        BountyDataManager.saveBounties(BountyManager.getBounties());
        getLogger().info("Saved " + BountyManager.getBounties().size() + " bounties to file.");
        getLogger().info("BountyHunter disabled!");
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
