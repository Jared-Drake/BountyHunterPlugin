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

import java.util.HashMap;
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
        // Handle set bounty menu (online and offline)
        else if (title.startsWith(BountyGUI.SET_BOUNTY_TITLE)) {
            event.setCancelled(true);
            handleSetBountyMenuClick(player, event.getSlot(), title);
        }
        // Handle currency amount selection menu (online and offline)
        else if (title.contains("Select") && title.contains("Amount")) {
            event.setCancelled(true);
            handleCurrencyAmountMenuClick(player, event.getSlot(), title);
        }
        // Handle bounty list menu (with pagination)
        else if (title.startsWith(BountyGUI.VIEW_BOUNTIES_TITLE)) {
            event.setCancelled(true);
            handleBountyListMenuClick(player, event.getSlot(), event.getInventory(), title);
        }
        // Handle player selection menu (with pagination)
        else if (title.startsWith(ChatColor.DARK_BLUE + "Select Target Player")) {
            event.setCancelled(true);
            handlePlayerSelectionMenuClick(player, event.getSlot(), event.getInventory(), title);
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
        // Extract target player name from title (handle both online and offline formats)
        String fullTargetInfo = title.substring(BountyGUI.SET_BOUNTY_TITLE.length() + 3); // Remove " - " prefix
        String targetName = fullTargetInfo.replace(" (Offline)", "");
        boolean isOffline = fullTargetInfo.contains("(Offline)");
        
        if (slot == 26) { // Cancel button
            player.closeInventory();
            return;
        }
        
        // Handle currency type selection
        BountyData.CurrencyType selectedCurrency = null;
        if (slot == 18) { // Diamonds
            selectedCurrency = BountyData.CurrencyType.DIAMOND;
        } else if (slot == 19) { // Emeralds
            selectedCurrency = BountyData.CurrencyType.EMERALD;
        } else if (slot == 20) { // Netherite
            selectedCurrency = BountyData.CurrencyType.NETHERITE;
        }
        
        if (selectedCurrency != null) {
            player.closeInventory();
            
            if (isOffline) {
                // Handle offline player currency selection
                BountyGUI.openOfflineCurrencyAmountMenu(player, targetName, selectedCurrency);
            } else {
                // Handle online player currency selection
                Player target = Bukkit.getPlayer(targetName);
                if (target != null) {
                    BountyGUI.openCurrencyAmountMenu(player, target, selectedCurrency);
                } else {
                    player.sendMessage(ChatColor.RED + "Target player is no longer online!");
                }
            }
        }
    }
    
    private void handleBountyListMenuClick(Player player, int slot, Inventory inventory, String title) {
        // Handle navigation buttons
        if (slot == 45) { // Previous page
            int currentPage = extractPageFromTitle(title);
            if (currentPage > 1) {
                player.closeInventory();
                BountyGUI.openBountyListMenu(player, currentPage - 1);
            }
            return;
        }
        
        if (slot == 53) { // Next page
            int currentPage = extractPageFromTitle(title);
            player.closeInventory();
            BountyGUI.openBountyListMenu(player, currentPage + 1);
            return;
        }
        
        if (slot == 49) { // Back to main menu
            player.closeInventory();
            BountyGUI.openMainMenu(player);
            return;
        }
        
        // Handle bounty selection
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null) {
            return;
        }
        
        // Extract target name from the item display name
        String displayName = clickedItem.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName).replace("Bounty on ", "");
        
        // Find target UUID (could be offline player)
        UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "Target player not found!");
            player.closeInventory();
            return;
        }
        
        BountyData bounty = BountyManager.getBounty(targetUUID);
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
        if (targetUUID.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot accept a bounty on yourself!");
            return;
        }
        
        // Open confirmation menu (create a fake Player object for offline players)
        player.closeInventory();
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            BountyGUI.openBountyConfirmMenu(player, targetPlayer, "accept");
        } else {
            // Handle offline player acceptance directly
            BountyManager.acceptBounty(targetUUID, player);
        }
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
    
    private void handlePlayerSelectionMenuClick(Player player, int slot, Inventory inventory, String title) {
        // Handle navigation buttons
        if (slot == 45) { // Previous page
            int currentPage = extractPageFromTitle(title);
            if (currentPage > 1) {
                player.closeInventory();
                BountyGUI.openPlayerSelectionMenu(player, currentPage - 1);
            }
            return;
        }
        
        if (slot == 53) { // Next page
            int currentPage = extractPageFromTitle(title);
            player.closeInventory();
            BountyGUI.openPlayerSelectionMenu(player, currentPage + 1);
            return;
        }
        
        if (slot == 49) { // Cancel button
            player.closeInventory();
            BountyGUI.openMainMenu(player);
            return;
        }
        
        // Get the clicked item
        ItemStack clickedItem = inventory.getItem(slot);
        if (clickedItem == null || (clickedItem.getType() != Material.PLAYER_HEAD && clickedItem.getType() != Material.SKELETON_SKULL)) {
            return;
        }
        
        // Extract player name from the item's display name
        String displayName = clickedItem.getItemMeta().getDisplayName();
        String targetName = ChatColor.stripColor(displayName);
        
        // Find target UUID (could be offline player)
        UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "Player not found!");
            player.closeInventory();
            return;
        }
        
        // Check if player already has a bounty
        if (BountyManager.hasBounty(targetUUID)) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + targetName + "!");
            player.closeInventory();
            return;
        }
        
        // Check if target is online to determine how to proceed
        Player onlineTarget = Bukkit.getPlayer(targetUUID);
        if (onlineTarget != null) {
            // Online player - use normal flow
            BountyTargetTracker.setTarget(player, onlineTarget);
            player.closeInventory();
            BountyGUI.openSetBountyMenu(player, onlineTarget);
        } else {
            // Offline player - create a temporary tracker entry
            OfflinePlayerTracker.setTarget(player, targetUUID, targetName);
            player.closeInventory();
            BountyGUI.openOfflineSetBountyMenu(player, targetName);
        }
    }
    
    private void handleCurrencyAmountMenuClick(Player player, int slot, String title) {
        // Check if it's the back button (last slot)
        if (slot == 53) {
            boolean isOffline = title.contains("(Offline)");
            
            if (isOffline) {
                OfflinePlayerTracker.OfflinePlayerInfo offlineTarget = OfflinePlayerTracker.getTarget(player);
                if (offlineTarget != null) {
                    player.closeInventory();
                    BountyGUI.openOfflineSetBountyMenu(player, offlineTarget.getName());
                } else {
                    player.closeInventory();
                }
            } else {
                Player target = BountyTargetTracker.getTarget(player);
                if (target != null) {
                    player.closeInventory();
                    BountyGUI.openSetBountyMenu(player, target);
                } else {
                    player.closeInventory();
                }
            }
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
        
        boolean isOffline = title.contains("(Offline)");
        
        if (isOffline) {
            // Handle offline bounty setting
            OfflinePlayerTracker.OfflinePlayerInfo offlineTarget = OfflinePlayerTracker.getTarget(player);
            if (offlineTarget != null) {
                BountyManager.setBounty(offlineTarget.getUuid(), offlineTarget.getName(), player, currency, amount);
                OfflinePlayerTracker.clearTarget(player);
            } else {
                player.sendMessage(ChatColor.RED + "Target player is no longer available!");
            }
        } else {
            // Handle online bounty setting
            Player target = BountyTargetTracker.getTarget(player);
            if (target != null) {
                setBounty(player, target, currency, amount);
                BountyTargetTracker.clearTarget(player);
            } else {
                player.sendMessage(ChatColor.RED + "Target player is no longer available!");
            }
        }
        
        player.closeInventory();
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
                    String targetName = PlayerDataManager.getPlayerName(targetUUID);
                    if (targetName == null) targetName = "Unknown";
                    
                    if (target != null && target.isOnline()) {
                        // Calculate distance and check if within tracking range
                        double distance = player.getLocation().distance(target.getLocation());
                        String distanceStr = String.format("%.1f", distance);
                        
                        if (distance <= 1000) {
                            player.setCompassTarget(target.getLocation());
                            player.sendMessage(ChatColor.GREEN + "🧭 Compass now pointing to " + target.getName() + "!");
                            player.sendMessage(ChatColor.GRAY + "Distance: " + ChatColor.WHITE + distanceStr + " blocks");
                            
                            // Check if both are in hunter/target mode
                            if (PlayerModeManager.isHunter(player) && PlayerModeManager.isTarget(target)) {
                                player.sendMessage(ChatColor.YELLOW + "⚔ Hunt is active - both players in combat mode!");
                            }
                            
                            player.sendMessage(ChatColor.YELLOW + "💡 Use '/bounty track' for detailed tracking info!");
                            player.sendMessage(ChatColor.GRAY + "💡 Targets can also use '/bounty track' to track their hunter!");
                        } else {
                            player.sendMessage(ChatColor.RED + "❌ " + target.getName() + " is too far away (" + distanceStr + " blocks)");
                            player.sendMessage(ChatColor.YELLOW + "💡 Move within 1000 blocks to activate compass tracking!");
                            player.sendMessage(ChatColor.GRAY + "💡 Use '/bounty track' for distance information!");
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "❌ " + targetName + " is currently offline.");
                        player.sendMessage(ChatColor.GRAY + "Compass tracking unavailable while target is offline.");
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
    
    private int extractPageFromTitle(String title) {
        try {
            // Extract page number from title like "Menu (Page 2/5)"
            String[] parts = title.split("Page ");
            if (parts.length > 1) {
                String pageInfo = parts[1];
                String pageNumber = pageInfo.split("/")[0];
                return Integer.parseInt(pageNumber);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }
        return 1; // Default to page 1
    }
    
    /**
     * Simple tracker for offline player bounty setting
     */
    private static class OfflinePlayerTracker {
        private static HashMap<UUID, OfflinePlayerInfo> offlineTargets = new HashMap<>();
        
        public static void setTarget(Player player, UUID targetUUID, String targetName) {
            offlineTargets.put(player.getUniqueId(), new OfflinePlayerInfo(targetUUID, targetName));
        }
        
        public static OfflinePlayerInfo getTarget(Player player) {
            return offlineTargets.get(player.getUniqueId());
        }
        
        public static void clearTarget(Player player) {
            offlineTargets.remove(player.getUniqueId());
        }
        
        private static class OfflinePlayerInfo {
            private final UUID uuid;
            private final String name;
            
            public OfflinePlayerInfo(UUID uuid, String name) {
                this.uuid = uuid;
                this.name = name;
            }
            
            public UUID getUuid() {
                return uuid;
            }
            
            public String getName() {
                return name;
            }
        }
    }
}
