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

import java.util.UUID;

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
        // Handle my bounties menu
        else if (title.equals(BountyGUI.MY_BOUNTIES_TITLE)) {
            event.setCancelled(true);
            handleMyBountiesMenuClick(player, event.getSlot());
        }
        // Handle bounty confirmation menu
        else if (title.equals(BountyGUI.BOUNTY_CONFIRM_TITLE)) {
            event.setCancelled(true);
            handleBountyConfirmMenuClick(player, event.getSlot(), event.getInventory());
        }
    }
    
    private void handleMainMenuClick(Player player, int slot) {
        switch (slot) {
            case 10: // Set Bounty
                openPlayerSelectionMenu(player);
                break;
            case 13: // View Bounties
                BountyGUI.openBountyListMenu(player);
                break;
            case 16: // My Accepted Bounties
                BountyGUI.openMyBountiesMenu(player);
                break;
            case 22: // Player Status (just informational, no action needed)
                // Could add a refresh or detailed status view here if needed
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
        // Get the bounty from the clicked slot
        Inventory inventory = player.getOpenInventory().getTopInventory();
        ItemStack clickedItem = inventory.getItem(slot);
        
        if (clickedItem == null) {
            return;
        }
        
        // Extract target name from the item display name
        String displayName = clickedItem.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName).replace("Bounty on ", "");
        
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Target player is no longer online!");
            player.closeInventory();
            return;
        }
        
        BountyData bounty = BountyManager.getBounty(target.getUniqueId());
        if (bounty == null) {
            player.sendMessage(ChatColor.RED + "Bounty no longer exists!");
            player.closeInventory();
            return;
        }
        
        // Check if bounty is already accepted
        if (bounty.isAccepted()) {
            if (bounty.getHunterUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "You have already accepted this bounty!");
            } else {
                player.sendMessage(ChatColor.RED + "This bounty has already been accepted by " + bounty.getHunterName() + "!");
            }
            return;
        }
        
        // Check if player placed this bounty
        if (bounty.getPlacedByUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot accept a bounty that you placed!");
            return;
        }
        
        // Check if player is the target
        if (target.equals(player)) {
            player.sendMessage(ChatColor.RED + "You cannot accept a bounty on yourself!");
            return;
        }
        
        // Open confirmation menu
        player.closeInventory();
        BountyGUI.openBountyConfirmMenu(player, target, "accept");
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
    
    private void handleMyBountiesMenuClick(Player player, int slot) {
        switch (slot) {
            case 22: // Abandon bounty
                UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
                if (acceptedBountyTarget != null) {
                    Player target = Bukkit.getPlayer(acceptedBountyTarget);
                    if (target != null) {
                        player.closeInventory();
                        BountyGUI.openBountyConfirmMenu(player, target, "abandon");
                    } else {
                        player.sendMessage(ChatColor.RED + "Target is no longer online!");
                        player.closeInventory();
                    }
                }
                break;
            case 4: // Track target (compass functionality)
                UUID targetUUID = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
                if (targetUUID != null) {
                    Player target = Bukkit.getPlayer(targetUUID);
                    if (target != null && target.isOnline()) {
                        player.setCompassTarget(target.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Compass now pointing to " + target.getName() + "!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Target is not online!");
                    }
                }
                break;
        }
    }
    
    private void handleBountyConfirmMenuClick(Player player, int slot, Inventory inventory) {
        switch (slot) {
            case 11: // Confirm action
                ItemStack confirmItem = inventory.getItem(slot);
                if (confirmItem != null) {
                    String itemName = confirmItem.getItemMeta().getDisplayName();
                    
                    if (itemName.contains("Confirm Accept")) {
                        // Get target from the center item
                        ItemStack targetItem = inventory.getItem(13);
                        if (targetItem != null) {
                            String displayName = targetItem.getItemMeta().getDisplayName();
                            String targetName = ChatColor.stripColor(displayName).replace("Bounty on ", "");
                            
                            Player target = Bukkit.getPlayer(targetName);
                            if (target != null) {
                                // Perform acceptance validation and action
                                BountyData bounty = BountyManager.getBounty(target.getUniqueId());
                                if (bounty != null && !bounty.isAccepted()) {
                                    BountyManager.acceptBounty(target.getUniqueId(), player);
                                } else {
                                    player.sendMessage(ChatColor.RED + "This bounty is no longer available!");
                                }
                            } else {
                                player.sendMessage(ChatColor.RED + "Target player is no longer online!");
                            }
                        }
                    } else if (itemName.contains("Confirm Abandon")) {
                        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
                        if (acceptedBountyTarget != null) {
                            BountyManager.abandonBounty(acceptedBountyTarget, player);
                        } else {
                            player.sendMessage(ChatColor.RED + "You don't have any accepted bounties!");
                        }
                    }
                }
                player.closeInventory();
                break;
            case 15: // Cancel
                player.closeInventory();
                BountyGUI.openMainMenu(player);
                break;
        }
    }
}
