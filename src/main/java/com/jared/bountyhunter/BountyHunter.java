package com.jared.bountyhunter;

import org.bukkit.plugin.java.JavaPlugin;

public class BountyHunter extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("bounty").setExecutor(new BountyCommand());
        getServer().getPluginManager().registerEvents(new BountyListener(), this);
        getServer().getPluginManager().registerEvents(new BountyGUIListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);
        getLogger().info("BountyHunter enabled!");
    }
}
