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
        
        // Diamond options
        for (int i = 0; i < 9; i++) {
            int amount = i + 1;
            ItemStack diamondItem = createGuiItem(DIAMOND_ICON, 
                ChatColor.AQUA + String.valueOf(amount) + " Diamond" + (amount > 1 ? "s" : ""),
                ChatColor.GRAY + "Click to set bounty with " + String.valueOf(amount) + " diamond" + (amount > 1 ? "s" : ""));
            inv.setItem(18 + i, diamondItem);
        }
        
        // Emerald options
        for (int i = 0; i < 9; i++) {
            int amount = i + 1;
            ItemStack emeraldItem = createGuiItem(EMERALD_ICON, 
                ChatColor.GREEN + String.valueOf(amount) + " Emerald" + (amount > 1 ? "s" : ""),
                ChatColor.GRAY + "Click to set bounty with " + String.valueOf(amount) + " emerald" + (amount > 1 ? "s" : ""));
            inv.setItem(27 + i, emeraldItem);
        }
        
        // Netherite options
        for (int i = 0; i < 9; i++) {
            int amount = i + 1;
            ItemStack netheriteItem = createGuiItem(NETHERITE_ICON, 
                ChatColor.DARK_PURPLE + String.valueOf(amount) + " Netherite Ingot" + (amount > 1 ? "s" : ""),
                ChatColor.GRAY + "Click to set bounty with " + String.valueOf(amount) + " netherite ingot" + (amount > 1 ? "s" : ""));
            inv.setItem(36 + i, netheriteItem);
        }
        
        // Cancel button
        ItemStack cancelItem = createGuiItem(Material.BARRIER, 
            ChatColor.RED + "Cancel", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(49, cancelItem);
        
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
}
