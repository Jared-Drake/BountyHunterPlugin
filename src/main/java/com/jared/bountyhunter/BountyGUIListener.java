package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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
        // Handle bounty list menu
        else if (title.equals(BountyGUI.VIEW_BOUNTIES_TITLE)) {
            event.setCancelled(true);
            handleBountyListMenuClick(player, event.getSlot());
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
        
        if (slot == 49) { // Cancel button
            player.closeInventory();
            return;
        }
        
        // Handle currency selection
        if (slot >= 18 && slot <= 26) { // Diamond options
            int amount = slot - 17;
            setBounty(player, target, BountyData.CurrencyType.DIAMOND, amount);
            player.closeInventory();
        } else if (slot >= 27 && slot <= 35) { // Emerald options
            int amount = slot - 26;
            setBounty(player, target, BountyData.CurrencyType.EMERALD, amount);
            player.closeInventory();
        } else if (slot >= 36 && slot <= 44) { // Netherite options
            int amount = slot - 35;
            setBounty(player, target, BountyData.CurrencyType.NETHERITE, amount);
            player.closeInventory();
        }
    }
    
    private void handleBountyListMenuClick(Player player, int slot) {
        // This could be used for claiming bounties or getting more info
        // For now, just close the inventory
        player.closeInventory();
    }
    
    private void openPlayerSelectionMenu(Player player) {
        PlayerSelectionManager.enterSelectionMode(player);
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
}
