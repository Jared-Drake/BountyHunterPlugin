package com.jared.bountyhunter;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PlayerDataManager {
    private static Plugin plugin;
    private static File playersFile;
    private static FileConfiguration playersConfig;
    private static HashMap<UUID, String> knownPlayers = new HashMap<>(); // UUID -> Last known name
    
    public static void initialize(Plugin pluginInstance) {
        plugin = pluginInstance;
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create players.yml: " + e.getMessage());
            }
        }
        
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
        loadKnownPlayers();
    }
    
    /**
     * Records a player when they join the server
     */
    public static void recordPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        
        // Update in memory
        knownPlayers.put(uuid, name);
        
        // Save to file
        playersConfig.set("players." + uuid.toString() + ".name", name);
        playersConfig.set("players." + uuid.toString() + ".lastSeen", System.currentTimeMillis());
        
        savePlayersFile();
    }
    
    /**
     * Gets all known players (UUID -> Name mapping)
     */
    public static HashMap<UUID, String> getKnownPlayers() {
        return new HashMap<>(knownPlayers);
    }
    
    /**
     * Gets a player's last known name by UUID
     */
    public static String getPlayerName(UUID uuid) {
        return knownPlayers.get(uuid);
    }
    
    /**
     * Finds a player UUID by name (case insensitive)
     */
    public static UUID findPlayerUUID(String name) {
        for (Map.Entry<UUID, String> entry : knownPlayers.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(name)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Gets all known players sorted by last seen (most recent first)
     */
    public static List<PlayerInfo> getKnownPlayersSorted() {
        List<PlayerInfo> players = new ArrayList<>();
        
        ConfigurationSection playersSection = playersConfig.getConfigurationSection("players");
        if (playersSection == null) {
            return players;
        }
        
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String name = playersSection.getString(uuidString + ".name");
                long lastSeen = playersSection.getLong(uuidString + ".lastSeen", 0);
                
                if (name != null) {
                    players.add(new PlayerInfo(uuid, name, lastSeen));
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid player data for " + uuidString + ": " + e.getMessage());
            }
        }
        
        // Sort by last seen (most recent first)
        players.sort((a, b) -> Long.compare(b.getLastSeen(), a.getLastSeen()));
        
        return players;
    }
    
    /**
     * Checks if a player is known to the server
     */
    public static boolean isKnownPlayer(UUID uuid) {
        return knownPlayers.containsKey(uuid);
    }
    
    /**
     * Checks if a player is known to the server by name
     */
    public static boolean isKnownPlayer(String name) {
        return findPlayerUUID(name) != null;
    }
    
    private static void loadKnownPlayers() {
        knownPlayers.clear();
        
        ConfigurationSection playersSection = playersConfig.getConfigurationSection("players");
        if (playersSection == null) {
            return;
        }
        
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                String name = playersSection.getString(uuidString + ".name");
                
                if (name != null) {
                    knownPlayers.put(uuid, name);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid player data for " + uuidString + ": " + e.getMessage());
            }
        }
        
        plugin.getLogger().info("Loaded " + knownPlayers.size() + " known players from file.");
    }
    
    private static void savePlayersFile() {
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save players file: " + e.getMessage());
        }
    }
    
    /**
     * Player information class
     */
    public static class PlayerInfo {
        private final UUID uuid;
        private final String name;
        private final long lastSeen;
        
        public PlayerInfo(UUID uuid, String name, long lastSeen) {
            this.uuid = uuid;
            this.name = name;
            this.lastSeen = lastSeen;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getName() {
            return name;
        }
        
        public long getLastSeen() {
            return lastSeen;
        }
        
        public boolean isOnline() {
            return org.bukkit.Bukkit.getPlayer(uuid) != null;
        }
    }
}
