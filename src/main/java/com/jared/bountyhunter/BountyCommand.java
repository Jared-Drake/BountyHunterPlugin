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
            player.sendMessage(ChatColor.RED + "Usage: /bounty <gui|set|list|remove|accept|abandon|status> [player] [currency] [amount]");
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
            case "accept":
                return handleAcceptBounty(player, args);
            case "abandon":
                return handleAbandonBounty(player);
            case "status":
                return handleStatusCommand(player);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: gui, set, list, remove, accept, abandon, or status");
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

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }

        if (!BountyManager.hasBounty(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No bounty exists on " + target.getName() + ".");
            return true;
        }

        BountyData bounty = BountyManager.getBounty(target.getUniqueId());
        if (bounty.getPlacedByUUID().equals(player.getUniqueId())) {
            BountyManager.removeBounty(target.getUniqueId());
            String currencyName = getCurrencyName(bounty.getCurrency());
            player.sendMessage(ChatColor.GREEN + "Bounty of " + bounty.getAmount() + " " + 
                currencyName + (bounty.getAmount() > 1 ? "s" : "") + " removed from " + target.getName() + "!");
        } else {
            player.sendMessage(ChatColor.RED + "You can only remove bounties that you placed!");
        }
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
