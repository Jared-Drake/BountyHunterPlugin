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
    public static final String MY_BOUNTIES_TITLE = ChatColor.BLUE + "My Accepted Bounties";
    public static final String BOUNTY_CONFIRM_TITLE = ChatColor.GREEN + "Confirm Bounty Action";
    
    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, BOUNTY_MENU_TITLE);
        
        // Set bounty option
        ItemStack setBountyItem = createGuiItem(Material.PLAYER_HEAD, (byte) 0, 
            ChatColor.GREEN + "Set Bounty", 
            ChatColor.GRAY + "Click to place a bounty on a player",
            ChatColor.YELLOW + "Cost: Money from your account");
        inv.setItem(10, setBountyItem);
        
        // View bounties option
        ItemStack viewBountiesItem = createGuiItem(Material.BOOK, 
            ChatColor.GOLD + "View Active Bounties", 
            ChatColor.GRAY + "Click to see all active bounties",
            ChatColor.YELLOW + "Accept bounties to become a hunter!",
            ChatColor.AQUA + "⚡ NEW: Targets can claim bounties by killing hunters!");
        inv.setItem(13, viewBountiesItem);
        
        // My accepted bounties option
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        ItemStack myBountiesItem = createGuiItem(Material.COMPASS, 
            ChatColor.BLUE + "My Accepted Bounties", 
            ChatColor.GRAY + "View and manage your accepted bounties",
            acceptedBountyTarget != null ? 
                ChatColor.GREEN + "You have an active hunt!" : 
                ChatColor.YELLOW + "No active hunts");
        inv.setItem(16, myBountiesItem);
        
        // Player status option (NEW)
        PlayerModeManager.PlayerMode mode = PlayerModeManager.getPlayerMode(player);
        Material statusMaterial;
        String statusTitle;
        List<String> statusLore = new ArrayList<>();
        
        switch (mode) {
            case BOUNTY_HUNTER:
                statusMaterial = Material.IRON_SWORD;
                statusTitle = ChatColor.RED + "⚔ Hunter Mode Active ⚔";
                Player target = PlayerModeManager.getHunterTarget(player);
                statusLore.add(ChatColor.GRAY + "You are hunting: " + (target != null ? target.getName() : "Unknown"));
                statusLore.add(ChatColor.GREEN + "Effects: Night Vision, Enhanced Tracking");
                statusLore.add(ChatColor.YELLOW + "Kill your target to claim the bounty!");
                statusLore.add(ChatColor.RED + "⚠ If they kill you, they get the reward!");
                break;
            case TARGET:
                statusMaterial = Material.SHIELD;
                statusTitle = ChatColor.YELLOW + "⚠ Target Mode Active ⚠";
                Player hunter = PlayerModeManager.getTargetHunter(player);
                statusLore.add(ChatColor.GRAY + "You are being hunted by: " + (hunter != null ? hunter.getName() : "Unknown"));
                statusLore.add(ChatColor.GREEN + "Effects: None (stay alert!)");
                statusLore.add(ChatColor.YELLOW + "Survive or fight back!");
                statusLore.add(ChatColor.AQUA + "⚡ Kill your hunter to claim the bounty yourself!");
                break;
            default:
                statusMaterial = Material.EMERALD;
                statusTitle = ChatColor.GREEN + "Normal Mode";
                statusLore.add(ChatColor.GRAY + "You are not in any special mode");
                statusLore.add(ChatColor.YELLOW + "Accept a bounty to become a hunter!");
                if (BountyManager.hasBounty(player.getUniqueId())) {
                    BountyData bounty = BountyManager.getBounty(player.getUniqueId());
                    statusLore.add("");
                    statusLore.add(ChatColor.RED + "⚠ You have a bounty on you!");
                    statusLore.add(ChatColor.GOLD + "Reward: $" + String.format("%.2f", bounty.getAmount()));
                    if (bounty.isAccepted()) {
                        statusLore.add(ChatColor.RED + "Hunter: " + bounty.getHunterName());
                    }
                }
                break;
        }
        
        ItemStack statusItem = createGuiItem(statusMaterial, statusTitle, statusLore.toArray(new String[0]));
        inv.setItem(22, statusItem);
        
        player.openInventory(inv);
    }
    
    public static void openSetBountyMenu(Player player, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, SET_BOUNTY_TITLE + " - " + target.getName());
        
        // Target player head
        ItemStack targetHead = createGuiItem(Material.PLAYER_HEAD, (byte) 0, 
            ChatColor.RED + "Target: " + target.getName(),
            ChatColor.GRAY + "Select bounty amount below");
        inv.setItem(4, targetHead);
        
        // Money amount selection (common amounts)
        ItemStack smallBounty = createGuiItem(Material.GOLD_INGOT, 
            ChatColor.GREEN + "$100 Bounty",
            ChatColor.GRAY + "Click to set a $100 bounty");
        inv.setItem(18, smallBounty);
        
        ItemStack mediumBounty = createGuiItem(Material.DIAMOND, 
            ChatColor.AQUA + "$500 Bounty",
            ChatColor.GRAY + "Click to set a $500 bounty");
        inv.setItem(19, mediumBounty);
        
        ItemStack largeBounty = createGuiItem(Material.EMERALD, 
            ChatColor.GREEN + "$1000 Bounty",
            ChatColor.GRAY + "Click to set a $1000 bounty");
        inv.setItem(20, largeBounty);
        
        ItemStack customBounty = createGuiItem(Material.PAPER, 
            ChatColor.GOLD + "Custom Amount",
            ChatColor.GRAY + "Click to set a custom bounty amount");
        inv.setItem(21, customBounty);
        
        // Cancel button
        ItemStack cancelItem = createGuiItem(Material.BARRIER, 
            ChatColor.RED + "Cancel", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(26, cancelItem);
        
        player.openInventory(inv);
    }
    
    public static void openOfflineSetBountyMenu(Player player, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 54, SET_BOUNTY_TITLE + " - " + targetName + " (Offline)");
        
        // Target player head (skeleton skull for offline)
        ItemStack targetHead = createGuiItem(Material.SKELETON_SKULL, 
            ChatColor.GRAY + "Target: " + targetName + " (Offline)",
            ChatColor.GRAY + "Select bounty amount below",
            ChatColor.YELLOW + "Player is currently offline");
        inv.setItem(4, targetHead);
        
        // Money amount selection (common amounts)
        ItemStack smallBounty = createGuiItem(Material.GOLD_INGOT, 
            ChatColor.GREEN + "$100 Bounty",
            ChatColor.GRAY + "Click to set a $100 bounty");
        inv.setItem(18, smallBounty);
        
        ItemStack mediumBounty = createGuiItem(Material.DIAMOND, 
            ChatColor.AQUA + "$500 Bounty",
            ChatColor.GRAY + "Click to set a $500 bounty");
        inv.setItem(19, mediumBounty);
        
        ItemStack largeBounty = createGuiItem(Material.EMERALD, 
            ChatColor.GREEN + "$1000 Bounty",
            ChatColor.GRAY + "Click to set a $1000 bounty");
        inv.setItem(20, largeBounty);
        
        ItemStack customBounty = createGuiItem(Material.PAPER, 
            ChatColor.GOLD + "Custom Amount",
            ChatColor.GRAY + "Click to set a custom bounty amount");
        inv.setItem(21, customBounty);
        
        // Cancel button
        ItemStack cancelItem = createGuiItem(Material.BARRIER, 
            ChatColor.RED + "Cancel", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(26, cancelItem);
        
        player.openInventory(inv);
    }
    
    public static void openCustomAmountMenu(Player player, Player target) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Set Custom Bounty Amount");
        
        // Target info at top
        ItemStack targetInfo = createGuiItem(Material.PLAYER_HEAD, (byte) 0,
            ChatColor.RED + "Target: " + target.getName(),
            ChatColor.GRAY + "Select custom bounty amount");
        inv.setItem(4, targetInfo);
        
        // Amount options (common amounts)
        double[] amounts = {50, 100, 200, 300, 400, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000, 5000};
        
        for (int i = 0; i < amounts.length && i < 45; i++) {
            double amount = amounts[i];
            Material icon = getMoneyIcon(amount);
            ChatColor color = getMoneyColor(amount);
            
            ItemStack amountItem = createGuiItem(icon, 
                color + "$" + String.format("%.0f", amount) + " Bounty",
                ChatColor.GRAY + "Click to set bounty with $" + String.format("%.0f", amount));
            inv.setItem(i, amountItem);
        }
        
        // Back button
        ItemStack backItem = createGuiItem(Material.ARROW, 
            ChatColor.YELLOW + "Back", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(53, backItem);
        
        player.openInventory(inv);
    }
    
    public static void openOfflineCustomAmountMenu(Player player, String targetName) {
        Inventory inv = Bukkit.createInventory(null, 54, ChatColor.GOLD + "Set Custom Bounty Amount (Offline)");
        
        // Target info at top
        ItemStack targetInfo = createGuiItem(Material.SKELETON_SKULL,
            ChatColor.GRAY + "Target: " + targetName + " (Offline)",
            ChatColor.GRAY + "Select custom bounty amount",
            ChatColor.YELLOW + "Target is currently offline");
        inv.setItem(4, targetInfo);
        
        // Amount options (common amounts)
        double[] amounts = {50, 100, 200, 300, 400, 500, 750, 1000, 1500, 2000, 2500, 3000, 4000, 5000};
        
        for (int i = 0; i < amounts.length && i < 45; i++) {
            double amount = amounts[i];
            Material icon = getMoneyIcon(amount);
            ChatColor color = getMoneyColor(amount);
            
            ItemStack amountItem = createGuiItem(icon, 
                color + "$" + String.format("%.0f", amount) + " Bounty",
                ChatColor.GRAY + "Click to set bounty with $" + String.format("%.0f", amount),
                ChatColor.YELLOW + "Target is currently offline");
            inv.setItem(i, amountItem);
        }
        
        // Back button
        ItemStack backItem = createGuiItem(Material.ARROW, 
            ChatColor.YELLOW + "Back", 
            ChatColor.GRAY + "Click to go back");
        inv.setItem(53, backItem);
        
        player.openInventory(inv);
    }
    
    private static Material getMoneyIcon(double amount) {
        if (amount < 100) return Material.IRON_INGOT;
        if (amount < 500) return Material.GOLD_INGOT;
        if (amount < 1000) return Material.DIAMOND;
        if (amount < 3000) return Material.EMERALD;
        return Material.NETHERITE_INGOT;
    }
    
    private static ChatColor getMoneyColor(double amount) {
        if (amount < 100) return ChatColor.GRAY;
        if (amount < 500) return ChatColor.GREEN;
        if (amount < 1000) return ChatColor.AQUA;
        if (amount < 3000) return ChatColor.GREEN;
        return ChatColor.DARK_PURPLE;
    }
    
    public static void openBountyListMenu(Player player) {
        openBountyListMenu(player, 1);
    }
    
    public static void openBountyListMenu(Player player, int page) {
        HashMap<UUID, BountyData> bounties = BountyManager.getBounties();
        
        if (bounties.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounties.");
            return;
        }
        
        // Create list of bounties with target names (including offline players)
        List<BountyDisplayInfo> bountyList = new ArrayList<>();
        for (UUID targetUUID : bounties.keySet()) {
            BountyData bounty = bounties.get(targetUUID);
            String targetName = PlayerDataManager.getPlayerName(targetUUID);
            
            if (targetName != null) {
                boolean isOnline = Bukkit.getPlayer(targetUUID) != null;
                bountyList.add(new BountyDisplayInfo(targetUUID, targetName, bounty, isOnline));
            }
        }
        
        if (bountyList.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounties with valid targets.");
            return;
        }
        
        // Sort bounties: online targets first, then by bounty amount (highest first)
        bountyList.sort((a, b) -> {
            if (a.isOnline() != b.isOnline()) {
                return a.isOnline() ? -1 : 1; // Online first
            }
            return Double.compare(b.getBounty().getAmount(), a.getBounty().getAmount()); // Higher amount first
        });
        
        // Pagination settings
        int bountiesPerPage = 45; // 5 rows of 9 slots each (leaving room for navigation)
        int totalPages = (bountyList.size() - 1) / bountiesPerPage + 1;
        
        // Validate page number
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            VIEW_BOUNTIES_TITLE + " (Page " + page + "/" + totalPages + ")");
        
        // Calculate start and end indices for this page
        int startIndex = (page - 1) * bountiesPerPage;
        int endIndex = Math.min(startIndex + bountiesPerPage, bountyList.size());
        
        // Add bounties for this page
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            BountyDisplayInfo bountyInfo = bountyList.get(i);
            ItemStack bountyItem = createBountyItemFromInfo(bountyInfo);
            inv.setItem(slot, bountyItem);
            slot++;
        }
        
        // Navigation buttons
        if (page > 1) {
            ItemStack prevButton = createGuiItem(Material.ARROW,
                ChatColor.YELLOW + "← Previous Page",
                ChatColor.GRAY + "Go to page " + (page - 1));
            inv.setItem(45, prevButton);
        }
        
        if (page < totalPages) {
            ItemStack nextButton = createGuiItem(Material.ARROW,
                ChatColor.YELLOW + "Next Page →",
                ChatColor.GRAY + "Go to page " + (page + 1));
            inv.setItem(53, nextButton);
        }
        
        // Back button
        ItemStack backButton = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Back to Main Menu",
            ChatColor.GRAY + "Click to go back");
        inv.setItem(49, backButton);
        
        player.openInventory(inv);
    }
    
    public static void openPlayerSelectionMenu(Player player) {
        openPlayerSelectionMenu(player, 1);
    }
    
    public static void openPlayerSelectionMenu(Player player, int page) {
        // Get all known players (online and offline) except the current player
        List<PlayerDataManager.PlayerInfo> allPlayers = PlayerDataManager.getKnownPlayersSorted();
        List<PlayerDataManager.PlayerInfo> availablePlayers = new ArrayList<>();
        
        for (PlayerDataManager.PlayerInfo playerInfo : allPlayers) {
            if (!playerInfo.getUuid().equals(player.getUniqueId())) {
                availablePlayers.add(playerInfo);
            }
        }
        
        if (availablePlayers.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No other players have joined this server!");
            return;
        }
        
        // Pagination settings
        int playersPerPage = 45; // 5 rows of 9 slots each (leaving room for navigation)
        int totalPages = (availablePlayers.size() - 1) / playersPerPage + 1;
        
        // Validate page number
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        Inventory inv = Bukkit.createInventory(null, 54, 
            ChatColor.DARK_BLUE + "Select Target Player (Page " + page + "/" + totalPages + ")");
        
        // Calculate start and end indices for this page
        int startIndex = (page - 1) * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, availablePlayers.size());
        
        // Add players for this page
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            PlayerDataManager.PlayerInfo playerInfo = availablePlayers.get(i);
            
            // Check if player already has a bounty
            boolean hasBounty = BountyManager.hasBounty(playerInfo.getUuid());
            boolean isOnline = playerInfo.isOnline();
            boolean isOnCooldown = BountyCooldownManager.isOnCooldown(playerInfo.getUuid());
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Click to place bounty on " + playerInfo.getName());
            
            if (isOnline) {
                lore.add(ChatColor.GREEN + "✓ Currently online");
            } else {
                lore.add(ChatColor.YELLOW + "⚬ Currently offline");
                // Calculate time since last seen
                long timeSince = System.currentTimeMillis() - playerInfo.getLastSeen();
                String timeAgo = getTimeAgoString(timeSince);
                lore.add(ChatColor.GRAY + "Last seen: " + timeAgo + " ago");
            }
            
            if (hasBounty) {
                lore.add(ChatColor.RED + "Already has a bounty!");
            } else if (isOnCooldown) {
                long remainingTime = BountyCooldownManager.getRemainingCooldown(playerInfo.getUuid());
                String timeString = BountyCooldownManager.formatRemainingTime(remainingTime);
                lore.add(ChatColor.RED + "⏰ On bounty cooldown!");
                lore.add(ChatColor.GRAY + "Time remaining: " + timeString);
            } else {
                lore.add(ChatColor.YELLOW + "Available for bounty");
            }
            
            Material headMaterial = isOnline ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;
            ChatColor nameColor = isOnline ? ChatColor.GREEN : ChatColor.GRAY;
            
            ItemStack playerHead = createGuiItem(headMaterial,
                nameColor + playerInfo.getName(),
                lore.toArray(new String[0]));
            
            inv.setItem(slot, playerHead);
            slot++;
        }
        
        // Navigation buttons
        if (page > 1) {
            ItemStack prevButton = createGuiItem(Material.ARROW,
                ChatColor.YELLOW + "← Previous Page",
                ChatColor.GRAY + "Go to page " + (page - 1));
            inv.setItem(45, prevButton);
        }
        
        if (page < totalPages) {
            ItemStack nextButton = createGuiItem(Material.ARROW,
                ChatColor.YELLOW + "Next Page →",
                ChatColor.GRAY + "Go to page " + (page + 1));
            inv.setItem(53, nextButton);
        }
        
        // Cancel button
        ItemStack cancelButton = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Cancel",
            ChatColor.GRAY + "Click to go back");
        inv.setItem(49, cancelButton);
        
        player.openInventory(inv);
    }
    
    public static void openMyBountiesMenu(Player player) {
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        
        if (acceptedBountyTarget == null) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any accepted bounties.");
            return;
        }
        
        Inventory inv = Bukkit.createInventory(null, 36, MY_BOUNTIES_TITLE);
        
        // Target info
        String targetName = PlayerDataManager.getPlayerName(acceptedBountyTarget);
        if (targetName == null) targetName = "Unknown";
        
        BountyData bounty = BountyManager.getBounty(acceptedBountyTarget);
        if (bounty == null) {
            player.sendMessage(ChatColor.RED + "Bounty data not found!");
            return;
        }
        
        ItemStack targetInfo = createGuiItem(Material.PLAYER_HEAD, (byte) 0,
            ChatColor.RED + "Target: " + targetName,
            ChatColor.GRAY + "Bounty Amount: $" + String.format("%.2f", bounty.getAmount()),
            ChatColor.GRAY + "Placed by: " + bounty.getPlacedBy());
        inv.setItem(13, targetInfo);
        
        // Track target button
        ItemStack trackButton = createGuiItem(Material.COMPASS,
            ChatColor.GREEN + "Track Target",
            ChatColor.GRAY + "Click to track your target with compass",
            ChatColor.YELLOW + "Use compass to find your target!");
        inv.setItem(4, trackButton);
        
        // Abandon bounty button
        ItemStack abandonButton = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Abandon Bounty",
            ChatColor.GRAY + "Click to abandon this bounty",
            ChatColor.YELLOW + "This will make the bounty available again");
        inv.setItem(22, abandonButton);
        
        // Back button
        ItemStack backButton = createGuiItem(Material.ARROW,
            ChatColor.YELLOW + "Back to Main Menu",
            ChatColor.GRAY + "Click to go back");
        inv.setItem(31, backButton);
        
        player.openInventory(inv);
    }
    
    public static void openBountyConfirmMenu(Player player, Player target, String action) {
        Inventory inv = Bukkit.createInventory(null, 27, BOUNTY_CONFIRM_TITLE);
        
        // Target info in center
        ItemStack targetInfo = createGuiItem(Material.PLAYER_HEAD, (byte) 0,
            ChatColor.RED + "Target: " + target.getName(),
            ChatColor.GRAY + "Action: " + (action.equals("accept") ? "Accept Bounty" : "Abandon Bounty"));
        inv.setItem(13, targetInfo);
        
        if (action.equals("accept")) {
            // Confirm accept button
            ItemStack confirmButton = createGuiItem(Material.LIME_WOOL,
                ChatColor.GREEN + "Confirm Accept",
                ChatColor.GRAY + "Click to accept this bounty",
                ChatColor.YELLOW + "You will become the hunter!");
            inv.setItem(11, confirmButton);
        } else if (action.equals("abandon")) {
            // Confirm abandon button
            ItemStack confirmButton = createGuiItem(Material.RED_WOOL,
                ChatColor.RED + "Confirm Abandon",
                ChatColor.GRAY + "Click to abandon this bounty",
                ChatColor.YELLOW + "The bounty will be available again!");
            inv.setItem(11, confirmButton);
        }
        
        // Cancel button
        ItemStack cancelButton = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Cancel",
            ChatColor.GRAY + "Click to cancel");
        inv.setItem(15, cancelButton);
        
        player.openInventory(inv);
    }
    
    private static ItemStack createBountyItemFromInfo(BountyDisplayInfo bountyInfo) {
        BountyData bounty = bountyInfo.getBounty();
        String targetName = bountyInfo.getName();
        boolean isOnline = bountyInfo.isOnline();
        
        Material icon = isOnline ? Material.PLAYER_HEAD : Material.SKELETON_SKULL;
        ChatColor nameColor = isOnline ? ChatColor.GREEN : ChatColor.GRAY;
        ChatColor statusColor = bounty.isAccepted() ? ChatColor.RED : ChatColor.GOLD;
        
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Bounty Amount: " + ChatColor.GOLD + "$" + String.format("%.2f", bounty.getAmount()));
        lore.add(ChatColor.GRAY + "Placed by: " + bounty.getPlacedBy());
        
        if (bounty.isAccepted()) {
            lore.add(ChatColor.RED + "Status: Accepted by " + bounty.getHunterName());
            lore.add(ChatColor.YELLOW + "This bounty is being hunted!");
        } else {
            lore.add(ChatColor.GREEN + "Status: Available");
            lore.add(ChatColor.YELLOW + "Click to accept this bounty!");
        }
        
        if (!isOnline) {
            lore.add(ChatColor.YELLOW + "Target is currently offline");
        }
        
        return createGuiItem(icon, nameColor + "Bounty on " + targetName, lore.toArray(new String[0]));
    }
    
    private static String getTimeAgoString(long timeSince) {
        long seconds = timeSince / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "");
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        } else if (minutes > 0) {
            return minutes + " minute" + (minutes > 1 ? "s" : "");
        } else {
            return seconds + " second" + (seconds > 1 ? "s" : "");
        }
    }
    
    private static ItemStack createGuiItem(Material material, String name, String... lore) {
        return createGuiItem(material, (byte) 0, name, lore);
    }
    
    private static ItemStack createGuiItem(Material material, byte data, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1, data);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        
        if (lore.length > 0) {
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    public static class BountyDisplayInfo {
        private final UUID uuid;
        private final String name;
        private final BountyData bounty;
        private final boolean online;
        
        public BountyDisplayInfo(UUID uuid, String name, BountyData bounty, boolean online) {
            this.uuid = uuid;
            this.name = name;
            this.bounty = bounty;
            this.online = online;
        }
        
        public UUID getUuid() {
            return uuid;
        }
        
        public String getName() {
            return name;
        }
        
        public BountyData getBounty() {
            return bounty;
        }
        
        public boolean isOnline() {
            return online;
        }
    }
}
