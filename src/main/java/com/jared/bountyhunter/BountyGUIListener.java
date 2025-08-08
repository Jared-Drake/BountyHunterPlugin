package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class BountyGUIListener implements Listener {
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Handle main menu
        if (title.equals(BountyGUI.BOUNTY_MENU_TITLE)) {
            event.setCancelled(true);
            handleMainMenuClick(player, event.getSlot());
        }
        // Handle set bounty menu
        else if (title.startsWith(BountyGUI.SET_BOUNTY_TITLE)) {
            event.setCancelled(true);
            handleSetBountyMenuClick(player, event.getSlot(), title);
        }
        // Handle currency amount selection menu
        else if (title.contains("Select") && title.contains("Amount")) {
            event.setCancelled(true);
            handleCurrencyAmountMenuClick(player, event.getSlot(), title);
        }
        // Handle bounty list menu
        else if (title.equals(BountyGUI.VIEW_BOUNTIES_TITLE)) {
            event.setCancelled(true);
            handleBountyListMenuClick(player, event.getSlot());
        }
        // Handle player selection menu
        else if (title.equals(ChatColor.DARK_BLUE + "Select Target Player")) {
            event.setCancelled(true);
            handlePlayerSelectionMenuClick(player, event.getSlot(), event.getInventory());
        }
    }
    
    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 11: // Set Bounty
                openPlayerSelectionMenu(player);
                break;
            case 15: // View Bounties
                BountyGUI.openBountyListMenu(player);
                break;
        }
    }
    
    private void handleSetBountyMenuClick(Player player, int slot, String title) {
        // Extract target player name from title
        String targetName = title.substring(BountyGUI.SET_BOUNTY_TITLE.length() + 3); // Remove " - " prefix
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Target player is no longer online!");
            player.closeInventory();
            return;
        }
        
        if (slot == 26) { // Cancel button
            player.closeInventory();
            return;
        }
        
        // Handle currency type selection
        if (slot == 18) { // Diamonds
            player.closeInventory();
            BountyGUI.openCurrencyAmountMenu(player, target, BountyData.CurrencyType.DIAMOND);
        } else if (slot == 19) { // Emeralds
            player.closeInventory();
            BountyGUI.openCurrencyAmountMenu(player, target, BountyData.CurrencyType.EMERALD);
        } else if (slot == 20) { // Netherite
            player.closeInventory();
            BountyGUI.openCurrencyAmountMenu(player, target, BountyData.CurrencyType.NETHERITE);
        }
    }
    
    private void handleBountyListMenuClick(Player player, int slot) {
        // This could be used for claiming bounties or getting more info
        // For now, just close the inventory
        player.closeInventory();
    }
    
    private void openPlayerSelectionMenu(Player player) {
        BountyGUI.openPlayerSelectionMenu(player);
    }
    
    private void setBounty(Player player, Player target, BountyData.CurrencyType currency, int amount) {
        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot place a bounty on yourself!");
            return;
        }
        
        if (BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + target.getName() + "!");
            return;
        }
        
        BountyManager.setBounty(target, player, currency, amount);
    }
    
    private void handlePlayerSelectionMenuClick(Player player, int slot, Inventory inventory) {
        // Check if it's the cancel button (last slot)
        if (slot == inventory.getSize() - 1) {
            player.closeInventory();
            return;
        }
        
        // Get the clicked item
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) {
            return;
        }
        
        // Extract player name from the item's display name
        String displayName = clickedItem.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName);
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player is no longer online!");
            player.closeInventory();
            return;
        }
        
        // Check if player already has a bounty
        if (BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + target.getName() + "!");
            player.closeInventory();
            return;
        }
        
        // Set target in tracker and open the bounty setting menu
        BountyTargetTracker.setTarget(player, target);
        player.closeInventory();
        BountyGUI.openSetBountyMenu(player, target);
    }
    
    private void handleCurrencyAmountMenuClick(Player player, int slot, String title) {
        // Check if it's the back button (last slot)
        if (slot == 53) {
            Player target = BountyTargetTracker.getTarget(player);
            if (target != null) {
                player.closeInventory();
                BountyGUI.openSetBountyMenu(player, target);
            } else {
                player.closeInventory();
            }
            return;
        }
        
        // Get target from tracker
        Player target = BountyTargetTracker.getTarget(player);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Target player is no longer available!");
            player.closeInventory();
            return;
        }
        
        // Extract currency type from title
        String[] titleParts = title.split(" ");
        String currencyName = titleParts[1]; // "Diamond", "Emerald", or "Netherite"
        
        // Determine currency type
        BountyData.CurrencyType currency;
        if (currencyName.equals("Diamond")) {
            currency = BountyData.CurrencyType.DIAMOND;
        } else if (currencyName.equals("Emerald")) {
            currency = BountyData.CurrencyType.EMERALD;
        } else if (currencyName.equals("Netherite")) {
            currency = BountyData.CurrencyType.NETHERITE;
        } else {
            player.sendMessage(ChatColor.RED + "Invalid currency type!");
            return;
        }
        
        // Calculate amount (slot + 1, but skip the target info slot)
        int amount = slot + 1;
        if (amount > 64) {
            player.sendMessage(ChatColor.RED + "Invalid amount!");
            return;
        }
        
        setBounty(player, target, currency, amount);
        player.closeInventory();
        BountyTargetTracker.clearTarget(player);
    }
}
