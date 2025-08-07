package com.jared.bountyhunter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {
    public static HashMap<UUID, Double> bounties = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty <set|list|remove> [player] [amount]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set":
                return handleSetBounty(player, args);
            case "list":
                return handleListBounties(player);
            case "remove":
                return handleRemoveBounty(player, args);
            default:
                player.sendMessage(ChatColor.RED + "Unknown subcommand. Use: set, list, or remove");
                return true;
        }
    }

    private boolean handleSetBounty(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /bounty set <player> <amount>");
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
        if (bounties.containsKey(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "A bounty already exists on " + target.getName() + "!");
            return true;
        }

        try {
            double amount = Double.parseDouble(args[2]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive.");
                return true;
            }

            if (amount < 1.0) {
                player.sendMessage(ChatColor.RED + "Minimum bounty amount is $1.00");
                return true;
            }

            if (BountyHunter.getEconomy().getBalance(player) < amount) {
                player.sendMessage(ChatColor.RED + "You don't have enough money! You have: $" + 
                    String.format("%.2f", BountyHunter.getEconomy().getBalance(player)));
                return true;
            }

            // Withdraw money and set bounty
            BountyHunter.getEconomy().withdrawPlayer(player, amount);
            bounties.put(target.getUniqueId(), amount);
            
            player.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", amount) + 
                " placed on " + target.getName() + "!");
            Bukkit.broadcastMessage(ChatColor.GOLD + player.getName() + " placed a bounty of $" + 
                String.format("%.2f", amount) + " on " + target.getName() + "!");
            
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount: " + args[2]);
        }
        return true;
    }

    private boolean handleListBounties(Player player) {
        if (bounties.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No active bounties.");
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "=== Active Bounties ===");
        for (UUID uuid : bounties.keySet()) {
            Player targetPlayer = Bukkit.getPlayer(uuid);
            if (targetPlayer != null) {
                double reward = bounties.get(uuid);
                player.sendMessage(ChatColor.GRAY + "- " + targetPlayer.getName() + 
                    ": $" + String.format("%.2f", reward));
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

        if (!bounties.containsKey(target.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "No bounty exists on " + target.getName() + ".");
            return true;
        }

        double amount = bounties.remove(target.getUniqueId());
        BountyHunter.getEconomy().depositPlayer(player, amount);
        
        player.sendMessage(ChatColor.GREEN + "Bounty of $" + String.format("%.2f", amount) + 
            " removed from " + target.getName() + " and refunded to you!");
        return true;
    }
}
