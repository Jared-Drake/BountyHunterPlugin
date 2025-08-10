package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty <gui|set|list|remove|accept|abandon|status|track|cooldown|adminremove> [player] [currency] [amount]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "gui":
                BountyGUI.openMainMenu(player);
                return true;
            case "set":
                return handleSetBounty(player, args);
            case "list":
                return handleListBounties(player);
            case "remove":
                return handleRemoveBounty(player, args);
            case "adminremove":
                return handleAdminRemoveBounty(player, args);
            case "accept":
                return handleAcceptBounty(player, args);
            case "abandon":
                return handleAbandonBounty(player);
            case "status":
                return handleStatusCommand(player);
            case "track":
                return handleTrackCommand(player);
            case "cooldown":
                return handleCooldownCommand(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: gui, set, list, remove, accept, abandon, status, track, cooldown, or adminremove");
                return true;
        }
    }

    private boolean handleSetBounty(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty set <player> <currency> <amount>");
            player.sendMessage(ChatColor.GRAY + "Currency options: diamond, emerald, netherite");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot set a bounty on yourself.");
            return true;
        }

        // Check if bounty already exists
        if (BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + target.getName() + "!");
            return true;
        }

        // Parse currency type
        BountyData.CurrencyType currency;
        try {
            currency = BountyData.CurrencyType.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Invalid currency: " + args[2]);
            player.sendMessage(ChatColor.GRAY + "Valid currencies: diamond, emerald, netherite");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[3]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }

            if (amount > 64) {
                player.sendMessage(ChatColor.RED + "Maximum bounty amount is 64.");
                return true;
            }

            BountyManager.setBounty(target, player, currency, amount);
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount: " + args[3]);
        }
        return true;
    }

    private boolean handleListBounties(Player player) {
        HashMap<UUID, BountyData> bounties = BountyManager.getBounties();
        if (bounties.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounties.");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "=== Active Bounties ===");
        for (UUID uuid : bounties.keySet()) {
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if (targetPlayer != null) {
                BountyData bounty = bounties.get(uuid);
                String currencyName = getCurrencyName(bounty.getCurrency());
                String status = bounty.isAccepted() ? 
                    ChatColor.RED + " [ACCEPTED by " + bounty.getHunterName() + "]" : 
                    ChatColor.GREEN + " [AVAILABLE]";
                player.sendMessage(ChatColor.GRAY + "- " + targetPlayer.getName() + 
                    ": " + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") +
                    " (set by " + bounty.getPlacedBy() + ")" + status);
            }
        }
        return true;
    }

    private boolean handleRemoveBounty(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty remove <player>");
            return true;
        }

        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        // Check if target is online or offline
        if (target == null) {
            // Target is offline, try to find by name
            UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
            if (targetUUID == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return true;
            }
            
            if (!BountyManager.hasBounty(targetUUID)) {
                player.sendMessage(ChatColor.RED + "No bounty exists on " + targetName + ".");
                return true;
            }
            
            BountyData bounty = BountyManager.getBounty(targetUUID);
            if (bounty.getPlacedByUUID().equals(player.getUniqueId())) {
                BountyManager.removeBountyOffline(targetUUID, player);
            } else if (targetUUID.equals(player.getUniqueId()) && player.hasPermission("bountyhunter.selfremove")) {
                // Allow targets to remove their own bounty if they have permission
                BountyManager.removeBountyOffline(targetUUID, player);
                player.sendMessage(ChatColor.YELLOW + "You have removed the bounty on yourself.");
            } else {
                player.sendMessage(ChatColor.RED + "You can only remove bounties that you placed!");
            }
            return true;
        }

        if (!BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No bounty exists on " + target.getName() + ".");
            return true;
        }

        BountyData bounty = BountyManager.getBounty(target.getUniqueId());
        if (bounty.getPlacedByUUID().equals(player.getUniqueId())) {
            BountyManager.removeBountyWithCleanup(target.getUniqueId(), player);
        } else if (target.getUniqueId().equals(player.getUniqueId()) && player.hasPermission("bountyhunter.selfremove")) {
            // Allow targets to remove their own bounty if they have permission
            BountyManager.removeBountyWithCleanup(target.getUniqueId(), player);
            player.sendMessage(ChatColor.YELLOW + "You have removed the bounty on yourself.");
        } else {
            player.sendMessage(ChatColor.RED + "You can only remove bounties that you placed!");
        }
        return true;
    }
    
    private boolean handleAdminRemoveBounty(Player player, String[] args) {
        if (!player.hasPermission("bountyhunter.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty adminremove <player>");
            return true;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        // Check if target is online or offline
        if (target == null) {
            // Target is offline, try to find by name
            UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
            if (targetUUID == null) {
                player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
                return true;
            }
            
            if (!BountyManager.hasBounty(targetUUID)) {
                player.sendMessage(ChatColor.RED + "No bounty exists on " + targetName + ".");
                return true;
            }
            
            BountyManager.removeBountyOffline(targetUUID, player);
            player.sendMessage(ChatColor.GREEN + "Admin removed bounty on " + targetName + ".");
            return true;
        }
        
        if (!BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No bounty exists on " + target.getName() + ".");
            return true;
        }
        
        BountyManager.removeBountyWithCleanup(target.getUniqueId(), player);
        player.sendMessage(ChatColor.GREEN + "Admin removed bounty on " + target.getName() + ".");
        return true;
    }
    
    private boolean handleAcceptBounty(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty accept <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        if (target == player) {
            player.sendMessage(ChatColor.RED + "You cannot accept a bounty on yourself.");
            return true;
        }

        if (!BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No bounty exists on " + target.getName() + ".");
            return true;
        }

        BountyData bounty = BountyManager.getBounty(target.getUniqueId());
        
        if (bounty.isAccepted()) {
            if (bounty.getHunterUUID().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "You have already accepted this bounty!");
            } else {
                player.sendMessage(ChatColor.RED + "This bounty has already been accepted by " + bounty.getHunterName() + "!");
            }
            return true;
        }

        // Check if the player who placed the bounty is trying to accept it
        if (bounty.getPlacedByUUID().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot accept a bounty that you placed!");
            return true;
        }

        // Accept the bounty
        BountyManager.acceptBounty(target.getUniqueId(), player);
        return true;
    }

    private boolean handleAbandonBounty(Player player) {
        // Find bounty that this player has accepted
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        
        if (acceptedBountyTarget == null) {
            player.sendMessage(ChatColor.RED + "You haven't accepted any bounties.");
            return true;
        }

        BountyManager.abandonBounty(acceptedBountyTarget, player);
        return true;
    }
    
    private boolean handleStatusCommand(Player player) {
        PlayerModeManager.PlayerMode mode = PlayerModeManager.getPlayerMode(player);
        
        player.sendMessage(ChatColor.YELLOW + "=== Your Bounty Status ===");
        player.sendMessage(ChatColor.GRAY + "Mode: " + ChatColor.WHITE + mode.name());
        
        if (mode == PlayerModeManager.PlayerMode.BOUNTY_HUNTER) {
            Player target = PlayerModeManager.getHunterTarget(player);
            String targetName = target != null ? target.getName() : "Unknown";
            player.sendMessage(ChatColor.GRAY + "Hunting: " + ChatColor.RED + targetName);
        } else if (mode == PlayerModeManager.PlayerMode.TARGET) {
            Player hunter = PlayerModeManager.getTargetHunter(player);
            String hunterName = hunter != null ? hunter.getName() : "Unknown";
            player.sendMessage(ChatColor.GRAY + "Being hunted by: " + ChatColor.RED + hunterName);
        }
        
        // Show if player has a bounty on them
        if (BountyManager.hasBounty(player.getUniqueId())) {
            BountyData bounty = BountyManager.getBounty(player.getUniqueId());
            String currencyName = getCurrencyName(bounty.getCurrency());
            player.sendMessage(ChatColor.GRAY + "Bounty on you: " + ChatColor.GOLD + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : ""));
        }
        
        // Show if player has accepted a bounty
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        if (acceptedBountyTarget != null) {
            Player target = Bukkit.getPlayer(acceptedBountyTarget);
            String targetName = target != null ? target.getName() : "Unknown";
            BountyData bounty = BountyManager.getBounty(acceptedBountyTarget);
            String currencyName = getCurrencyName(bounty.getCurrency());
            player.sendMessage(ChatColor.GRAY + "Accepted bounty on: " + ChatColor.GOLD + targetName + 
                " (" + bounty.getAmount() + " " + currencyName + (bounty.getAmount() > 1 ? "s" : "") + ")");
        }
        
        return true;
    }
    
    private boolean handleTrackCommand(Player player) {
        if (!PlayerModeManager.isHunter(player)) {
            player.sendMessage(ChatColor.RED + "❌ You must be in hunter mode to use enhanced tracking!");
            player.sendMessage(ChatColor.GRAY + "💡 Accept a bounty first to enter hunter mode.");
            return true;
        }
        
        // Use enhanced tracking info
        String trackingInfo = EnhancedTracker.getDetailedTrackingInfo(player);
        player.sendMessage(trackingInfo);
        
        return true;
    }
    
    private String getDirection(org.bukkit.Location from, org.bukkit.Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        // Calculate angle in degrees
        double angle = Math.toDegrees(Math.atan2(dz, dx));
        if (angle < 0) angle += 360;
        
        // Convert to cardinal directions
        if (angle >= 337.5 || angle < 22.5) return "East";
        else if (angle >= 22.5 && angle < 67.5) return "Southeast";
        else if (angle >= 67.5 && angle < 112.5) return "South";
        else if (angle >= 112.5 && angle < 157.5) return "Southwest";
        else if (angle >= 157.5 && angle < 202.5) return "West";
        else if (angle >= 202.5 && angle < 247.5) return "Northwest";
        else if (angle >= 247.5 && angle < 292.5) return "North";
        else return "Northeast";
    }
    
    private boolean handleCooldownCommand(Player player, String[] args) {
        if (args.length < 2) {
            // Show player's own cooldown or list all cooldowns
            return showCooldownInfo(player);
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "list":
                return listAllCooldowns(player);
            case "clear":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty cooldown clear <player>");
                    return true;
                }
                return clearPlayerCooldown(player, args[2]);
            case "clearall":
                return clearAllCooldowns(player);
            default:
                // Check specific player's cooldown
                return checkPlayerCooldown(player, args[1]);
        }
    }
    
    private boolean showCooldownInfo(Player player) {
        player.sendMessage(ChatColor.YELLOW + "=== Bounty Cooldown Info ===");
        
        // Check if player is on cooldown
        if (BountyCooldownManager.isOnCooldown(player.getUniqueId())) {
            long remainingTime = BountyCooldownManager.getRemainingCooldown(player.getUniqueId());
            String timeString = BountyCooldownManager.formatRemainingTime(remainingTime);
            player.sendMessage(ChatColor.RED + "⏰ You are on bounty cooldown!");
            player.sendMessage(ChatColor.GRAY + "Time remaining: " + ChatColor.WHITE + timeString);
            player.sendMessage(ChatColor.YELLOW + "💡 No bounties can be placed on you during this time.");
        } else {
            player.sendMessage(ChatColor.GREEN + "✓ You are not on bounty cooldown.");
            player.sendMessage(ChatColor.GRAY + "Bounties can be placed on you normally.");
        }
        
        player.sendMessage(ChatColor.GRAY + "Commands:");
        player.sendMessage(ChatColor.GRAY + "• /bounty cooldown <player> - Check specific player");
        player.sendMessage(ChatColor.GRAY + "• /bounty cooldown list - List all cooldowns");
        
        return true;
    }
    
    private boolean checkPlayerCooldown(Player player, String targetName) {
        UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }
        
        String actualName = PlayerDataManager.getPlayerName(targetUUID);
        if (actualName == null) actualName = targetName;
        
        if (BountyCooldownManager.isOnCooldown(targetUUID)) {
            long remainingTime = BountyCooldownManager.getRemainingCooldown(targetUUID);
            String timeString = BountyCooldownManager.formatRemainingTime(remainingTime);
            player.sendMessage(ChatColor.YELLOW + "⏰ " + actualName + " is on bounty cooldown.");
            player.sendMessage(ChatColor.GRAY + "Time remaining: " + ChatColor.WHITE + timeString);
        } else {
            player.sendMessage(ChatColor.GREEN + "✓ " + actualName + " is not on bounty cooldown.");
        }
        
        return true;
    }
    
    private boolean listAllCooldowns(Player player) {
        var cooldowns = BountyCooldownManager.getAllCooldowns();
        
        if (cooldowns.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounty cooldowns.");
            return true;
        }
        
        player.sendMessage(ChatColor.YELLOW + "=== Active Bounty Cooldowns ===");
        for (var entry : cooldowns.entrySet()) {
            String playerName = PlayerDataManager.getPlayerName(entry.getKey());
            if (playerName == null) playerName = "Unknown";
            
            long remainingTime = BountyCooldownManager.getRemainingCooldown(entry.getKey());
            String timeString = BountyCooldownManager.formatRemainingTime(remainingTime);
            
            player.sendMessage(ChatColor.GRAY + "• " + ChatColor.WHITE + playerName + ChatColor.GRAY + ": " + timeString);
        }
        
        return true;
    }
    
    private boolean clearPlayerCooldown(Player player, String targetName) {
        if (!player.hasPermission("bountyhunter.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to clear cooldowns.");
            return true;
        }
        
        UUID targetUUID = PlayerDataManager.findPlayerUUID(targetName);
        if (targetUUID == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
            return true;
        }
        
        String actualName = PlayerDataManager.getPlayerName(targetUUID);
        if (actualName == null) actualName = targetName;
        
        if (BountyCooldownManager.clearCooldown(targetUUID)) {
            player.sendMessage(ChatColor.GREEN + "✓ Cleared bounty cooldown for " + actualName + ".");
        } else {
            player.sendMessage(ChatColor.YELLOW + actualName + " was not on cooldown.");
        }
        
        return true;
    }
    
    private boolean clearAllCooldowns(Player player) {
        if (!player.hasPermission("bountyhunter.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to clear cooldowns.");
            return true;
        }
        
        int clearedCount = BountyCooldownManager.clearAllCooldowns();
        player.sendMessage(ChatColor.GREEN + "✓ Cleared " + clearedCount + " bounty cooldowns.");
        
        return true;
    }
    
    private String getCurrencyName(BountyData.CurrencyType currency) {
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
}
