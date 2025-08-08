package com.jared.bountyhunter;

import org.bukkit.plugin.java.JavaPlugin;

public class BountyHunter extends JavaPlugin {

    @Override
    public void onEnable() {
        // Initialize data manager
        BountyDataManager.initialize(this);
        
        // Load existing bounties
        BountyManager.loadBountiesFromFile();
        
        getCommand("bounty").setExecutor(new BountyCommand());
        getServer().getPluginManager().registerEvents(new BountyListener(), this);
        getServer().getPluginManager().registerEvents(new BountyGUIListener(), this);
        getLogger().info("BountyHunter enabled!");
        getLogger().info("Loaded " + BountyManager.getBounties().size() + " bounties from file.");
    }
    
    @Override
    public void onDisable() {
        // Save all bounties on shutdown
        BountyDataManager.saveBounties(BountyManager.getBounties());
        getLogger().info("Saved " + BountyManager.getBounties().size() + " bounties to file.");
        getLogger().info("BountyHunter disabled!");
    }
}
