package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BountyGUI {
    
    public static final String BOUNTY_MENU_TITLE = ChatColor.DARK_RED + "Bounty Hunter Menu";
    public static final String SET_BOUNTY_TITLE = ChatColor.RED + "Set Bounty";
    public static final String VIEW_BOUNTIES_TITLE = ChatColor.GOLD + "Active Bounties";
    
    private static final Material DIAMOND_ICON = Material.DIAMOND;
    private static final Material EMERALD_ICON = Material.EMERALD;
    private static final Material NETHERITE_ICON = Material.NETHERITE_INGOT;
    
    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, BOUNTY_MENU_TITLE);
        
        // Set bounty option
        ItemStack setBountyItem = createGuiItem(Material.PLAYER_HEAD, (byte) 0, 
            ChatColor.GREEN + "Set Bounty", 
            ChatColor.GRAY + "Click to place a bounty on a player",
            ChatColor.YELLOW + "Cost: Diamonds, Emeralds, or Netherite");
        inv.setItem(11, setBountyItem);
        
        // View bounties option
        ItemStack viewBountiesItem = createGuiItem(Material.BOOK, 
            ChatColor.BLUE + "View Active Bounties", 
            ChatColor.GRAY + "Click to see all active bounties");
        inv.setItem(15, viewBountiesItem);
        
        player.openInventory(inv);
    }
    
    public static void openSetBountyMenu(Player player, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, SET_BOUNTY_TITLE + " - " + target.getName());
        
        // Target player head
        ItemStack targetHead = createGuiItem(Material.PLAYER_HEAD, (byte) 0, 
            ChatColor.RED + "Target: " + target.getName(),
            ChatColor.GRAY + "Select currency and amount below");
        inv.setItem(4, targetHead);
        
        // Currency type selection at the top
        ItemStack diamondCurrency = createGuiItem(DIAMOND_ICON, 
            ChatColor.AQUA + "Diamonds",
            ChatColor.GRAY + "Click to select diamond amounts");
        inv.setItem(18, diamondCurrency);
        
        ItemStack emeraldCurrency = createGuiItem(EMERALD_ICON, 
            ChatColor.GREEN + "Emeralds",
            ChatColor.GRAY + "Click to select emerald amounts");
        inv.setItem(19, emeraldCurrency);
        
        ItemStack netheriteCurrency = createGuiItem(NETHERITE_ICON, 
            ChatColor.DARK_PURPLE + "Netherite Ingots",
            ChatColor.GRAY + "Click to select netherite amounts");
        inv.setItem(20, netheriteCurrency);
        
        // Cancel button
        ItemStack cancelItem = createGuiItem(Material.BARRIER, 
            ChatColor.RED + "Cancel", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(26, cancelItem);
        
        player.openInventory(inv);
    }
    
    public static void openCurrencyAmountMenu(Player player, Player target, BountyData.CurrencyType currency) {
        String currencyName = getCurrencyName(currency);
        Material currencyMaterial = getCurrencyMaterial(currency);
        ChatColor currencyColor = getCurrencyColor(currency);
        
        Inventory inv = Bukkit.createInventory(null, 54, currencyColor + "Select " + currencyName + " Amount");
        
        // Target info at top
        ItemStack targetInfo = createGuiItem(Material.PLAYER_HEAD, (byte) 0,
            ChatColor.RED + "Target: " + target.getName(),
            ChatColor.GRAY + "Currency: " + currencyName);
        inv.setItem(4, targetInfo);
        
        // Amount options (1-64)
        for (int i = 0; i < 54; i++) {
            int amount = i + 1;
            if (amount > 64) break;
            
            ItemStack amountItem = createGuiItem(currencyMaterial, 
                currencyColor + String.valueOf(amount) + " " + currencyName + (amount > 1 ? "s" : ""),
                ChatColor.GRAY + "Click to set bounty with " + String.valueOf(amount) + " " + currencyName + (amount > 1 ? "s" : ""));
            inv.setItem(i, amountItem);
        }
        
        // Back button
        ItemStack backItem = createGuiItem(Material.ARROW, 
            ChatColor.YELLOW + "Back", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(53, backItem);
        
        player.openInventory(inv);
    }
    
    public static void openBountyListMenu(Player player) {
        HashMap<UUID, BountyData> bounties = BountyManager.getBounties();
        
        if (bounties.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounties.");
            return;
        }
        
        int size = Math.min(54, ((bounties.size() - 1) / 9 + 1) * 9);
        Inventory inv = Bukkit.createInventory(null, size, VIEW_BOUNTIES_TITLE);
        
        int slot = 0;
        for (UUID targetUUID : bounties.keySet()) {
            Player target = Bukkit.getPlayer(targetUUID);
            if (target != null) {
                BountyData bounty = bounties.get(targetUUID);
                ItemStack bountyItem = createBountyItem(target, bounty);
                inv.setItem(slot, bountyItem);
                slot++;
            }
        }
        
        player.openInventory(inv);
    }
    
    public static void openPlayerSelectionMenu(Player player) {
        // Get all online players except the current player
        List<Player> onlinePlayers = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.equals(player)) {
                onlinePlayers.add(p);
            }
        }
        
        if (onlinePlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No other players are online!");
            return;
        }
        
        // Calculate inventory size based on number of players
        int playerCount = onlinePlayers.size();
        int rows = (playerCount - 1) / 9 + 1;
        int size = Math.min(54, rows * 9); // Max 6 rows (54 slots)
        
        Inventory inv = Bukkit.createInventory(null, size, ChatColor.DARK_BLUE + "Select Target Player");
        
        int slot = 0;
        for (Player target : onlinePlayers) {
            if (slot >= size) break; // Safety check
            
            // Check if player already has a bounty
            boolean hasBounty = BountyManager.hasBounty(target.getUniqueId());
            
            ItemStack playerHead = createGuiItem(Material.PLAYER_HEAD, (byte) 0,
                ChatColor.GREEN + target.getName(),
                ChatColor.GRAY + "Click to place bounty on " + target.getName(),
                hasBounty ? ChatColor.RED + "Already has a bounty!" : ChatColor.YELLOW + "Available for bounty");
            
            inv.setItem(slot, playerHead);
            slot++;
        }
        
        // Add cancel button at the bottom
        if (size >= 9) {
            ItemStack cancelItem = createGuiItem(Material.BARRIER,
                ChatColor.RED + "Cancel",
                ChatColor.GRAY + "Click to go back");
            inv.setItem(size - 1, cancelItem);
        }
        
        player.openInventory(inv);
    }
    
    private static ItemStack createBountyItem(Player target, BountyData bounty) {
        Material material;
        String currencyName;
        
        switch (bounty.getCurrency()) {
            case DIAMOND:
                material = Material.DIAMOND;
                currencyName = "Diamond";
                break;
            case EMERALD:
                material = Material.EMERALD;
                currencyName = "Emerald";
                break;
            case NETHERITE:
                material = Material.NETHERITE_INGOT;
                currencyName = "Netherite Ingot";
                break;
            default:
                material = Material.PAPER;
                currencyName = "Unknown";
        }
        
        return createGuiItem(material, 
            ChatColor.GOLD + "Bounty on " + target.getName(),
            ChatColor.GREEN + "Reward: " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : ""),
            ChatColor.GRAY + "Set by: " + bounty.getPlacedBy(),
            ChatColor.YELLOW + "Click to claim (if you kill them)");
    }
    
    private static ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, (byte) 0, name, lore);
    }
    
    private static ItemStack createGuiItem(Material material, byte data, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        
        item.setItemMeta(meta);
        return item;
    }
    
    private static String getCurrencyName(BountyData.CurrencyType currency) {
        switch (currency) {
            case DIAMOND:
                return "Diamond";
            case EMERALD:
                return "Emerald";
            case NETHERITE:
                return "Netherite Ingot";
            default:
                return "Unknown";
        }
    }
    
    private static Material getCurrencyMaterial(BountyData.CurrencyType currency) {
        switch (currency) {
            case DIAMOND:
                return Material.DIAMOND;
            case EMERALD:
                return Material.EMERALD;
            case NETHERITE:
                return Material.NETHERITE_INGOT;
            default:
                return Material.PAPER;
        }
    }
    
    private static ChatColor getCurrencyColor(BountyData.CurrencyType currency) {
        switch (currency) {
            case DIAMOND:
                return ChatColor.AQUA;
            case EMERALD:
                return ChatColor.GREEN;
            case NETHERITE:
                return ChatColor.DARK_PURPLE;
            default:
                return ChatColor.WHITE;
        }
    }
}
