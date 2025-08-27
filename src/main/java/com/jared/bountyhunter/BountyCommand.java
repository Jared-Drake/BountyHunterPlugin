package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BountyCommand implements org.bukkit.command.CommandExecutor {
    
    @Override
    public boolean onCommand(org.bukkit.command.CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open GUI
            BountyGUI.openMainMenu(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "gui":
                BountyGUI.openMainMenu(player);
                break;
            case "set":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty set <player> <amount>");
                    return true;
                }
                handleSetBounty(player, args);
                break;
            case "list":
                handleListBounties(player);
                break;
            case "remove":
                handleRemoveBounty(player, args);
                break;
            case "cooldown":
                handleCooldownCommand(player, args);
                break;
            case "track":
                handleTrackCommand(player);
                break;
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private boolean handleSetBounty(Player player, String[] args) {
        if (!player.hasPermission("bountyhunter.set")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to set bounties.");
            return true;
        }
        
        String targetName = args[1];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + targetName);
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

        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }

            if (amount > 1000000) {
                player.sendMessage(ChatColor.RED + "Maximum bounty amount is $1,000,000.");
                return true;
            }

            BountyManager.setBounty(target, player, amount);
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
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
                String status = bounty.isAccepted() ? 
                    ChatColor.RED + " [ACCEPTED by " + bounty.getHunterName() + "]" : 
                    ChatColor.GREEN + " [AVAILABLE]";
                player.sendMessage(ChatColor.GRAY + "- " + targetPlayer.getName() + 
                    ": $" + String.format("%.2f", bounty.getAmount()) +
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
        
        // Target is online
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
    
    private boolean handleCooldownCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty cooldown <check|list|clear> [player]");
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        
        switch (subCommand) {
            case "check":
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /bounty cooldown check <player>");
                    return true;
                }
                return checkPlayerCooldown(player, args[2]);
            case "list":
                if (!player.hasPermission("bountyhunter.admin")) {
                    player.sendMessage(ChatColor.RED + "You don't have permission to list cooldowns.");
                    return true;
                }
                return listAllCooldowns(player);
            case "clear":
                if (args.length < 3) {
                    if (!player.hasPermission("bountyhunter.admin")) {
                        player.sendMessage(ChatColor.RED + "You don't have permission to clear cooldowns.");
                        return true;
                    }
                    return clearAllCooldowns(player);
                } else {
                    return clearPlayerCooldown(player, args[2]);
                }
            default:
                player.sendMessage(ChatColor.RED + "Unknown cooldown command. Use: check, list, or clear");
                return true;
        }
    }
    
    private boolean handleTrackCommand(Player player) {
        // Check if player is a hunter
        UUID acceptedBountyTarget = BountyManager.getAcceptedBountyTarget(player.getUniqueId());
        if (acceptedBountyTarget != null) {
            Player target = Bukkit.getPlayer(acceptedBountyTarget);
            if (target != null && target.isOnline()) {
                double distance = player.getLocation().distance(target.getLocation());
                String distanceStr = String.format("%.1f", distance);
                
                player.sendMessage(ChatColor.GREEN + "=== Tracking Your Target ===");
                player.sendMessage(ChatColor.GRAY + "Target: " + ChatColor.WHITE + target.getName());
                player.sendMessage(ChatColor.GRAY + "Distance: " + ChatColor.WHITE + distanceStr + " blocks");
                player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + target.getWorld().getName());
                player.sendMessage(ChatColor.GRAY + "Coordinates: " + ChatColor.WHITE + 
                    String.format("%.0f, %.0f, %.0f", target.getLocation().getX(), 
                        target.getLocation().getY(), target.getLocation().getZ()));
                
                if (distance <= 1000) {
                    player.sendMessage(ChatColor.GREEN + "✓ Compass tracking active!");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "⚠ Target too far for compass tracking (>1000 blocks)");
                }
                
                return true;
            } else {
                player.sendMessage(ChatColor.YELLOW + "Your target is currently offline.");
                return true;
            }
        }
        
        // Check if player is a target
        if (BountyManager.hasBounty(player.getUniqueId())) {
            BountyData bounty = BountyManager.getBounty(player.getUniqueId());
            if (bounty.isAccepted()) {
                Player hunter = Bukkit.getPlayer(bounty.getHunterUUID());
                if (hunter != null && hunter.isOnline()) {
                    double distance = player.getLocation().distance(hunter.getLocation());
                    String distanceStr = String.format("%.1f", distance);
                    
                    player.sendMessage(ChatColor.RED + "=== Tracking Your Hunter ===");
                    player.sendMessage(ChatColor.GRAY + "Hunter: " + ChatColor.WHITE + hunter.getName());
                    player.sendMessage(ChatColor.GRAY + "Distance: " + ChatColor.WHITE + distanceStr + " blocks");
                    player.sendMessage(ChatColor.GRAY + "World: " + ChatColor.WHITE + hunter.getWorld().getName());
                    player.sendMessage(ChatColor.GRAY + "Coordinates: " + ChatColor.WHITE + 
                        String.format("%.0f, %.0f, %.0f", hunter.getLocation().getX(), 
                            hunter.getLocation().getY(), hunter.getLocation().getZ()));
                    
                    if (distance <= 1000) {
                        player.sendMessage(ChatColor.GREEN + "✓ Compass tracking active!");
                    } else {
                        player.sendMessage(ChatColor.YELLOW + "⚠ Hunter too far for compass tracking (>1000 blocks)");
                    }
                    
                    return true;
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Your hunter is currently offline.");
                    return true;
                }
            } else {
                player.sendMessage(ChatColor.YELLOW + "You have a bounty but no one has accepted it yet.");
                return true;
            }
        }
        
        player.sendMessage(ChatColor.YELLOW + "You are not currently hunting or being hunted.");
        return true;
    }
    
    private boolean showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== BountyHunter Commands ===");
        player.sendMessage(ChatColor.GRAY + "• /bounty - Open the bounty menu");
        player.sendMessage(ChatColor.GRAY + "• /bounty gui - Open the bounty menu");
        player.sendMessage(ChatColor.GRAY + "• /bounty set <player> <amount> - Set a bounty on a player");
        player.sendMessage(ChatColor.GRAY + "• /bounty list - List all active bounties");
        player.sendMessage(ChatColor.GRAY + "• /bounty remove <player> - Remove a bounty you placed");
        player.sendMessage(ChatColor.GRAY + "• /bounty cooldown check <player> - Check if a player is on cooldown");
        player.sendMessage(ChatColor.GRAY + "• /bounty cooldown list - List all cooldowns");
        player.sendMessage(ChatColor.GRAY + "• /bounty track - Track your target or hunter");
        
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
}
