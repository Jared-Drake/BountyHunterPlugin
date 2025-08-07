package com.jared.bountyhunter;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class BountyHunter extends JavaPlugin {

    private static Economy econ;

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault with an economy plugin not found! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("bounty").setExecutor(new BountyCommand());
        getServer().getPluginManager().registerEvents(new BountyListener(), this);
        getLogger().info("BountyHunter enabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }

    public static Economy getEconomy() {
        return econ;
    }
}
