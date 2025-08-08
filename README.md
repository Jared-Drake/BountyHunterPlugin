# BountyHunter Plugin v2.0

A GUI-based bounty hunting plugin for Minecraft servers that uses diamonds, emeralds, and netherite ingots as currency.

## Features

- **GUI Interface**: Easy-to-use graphical interface for managing bounties
- **Multiple Currencies**: Support for diamonds, emeralds, and netherite ingots
- **Real-time Updates**: Bounties are automatically claimed when players are killed
- **Inventory Integration**: Currency is taken from and given to player inventories

## Commands

- `/bounty gui` - Opens the main bounty GUI menu
- `/bounty set <player> <currency> <amount>` - Set a bounty via command
- `/bounty list` - List all active bounties
- `/bounty remove <player>` - Remove a bounty you placed

## GUI Usage

1. **Main Menu**: Use `/bounty gui` to open the main menu
2. **Set Bounty**: Click "Set Bounty" to open the player selection menu
3. **Select Player**: Click on a player's head to select them as your target
4. **Select Currency**: Choose between diamonds, emeralds, or netherite ingots
5. **Choose Amount**: Select 1-9 of the chosen currency
6. **View Bounties**: Click "View Active Bounties" to see all current bounties

## Currency Types

- **Diamonds**: Blue currency option (1-9 diamonds)
- **Emeralds**: Green currency option (1-9 emeralds)  
- **Netherite Ingots**: Purple currency option (1-9 netherite ingots)

## How It Works

1. **Setting Bounties**: Players must have the required currency in their inventory
2. **Currency Removal**: The specified amount is removed from the player's inventory
3. **Bounty Storage**: Bounties are stored in memory (not persistent across server restarts)
4. **Claiming Bounties**: When a player with a bounty is killed, the killer receives the currency
5. **Bounty Removal**: Only the player who placed the bounty can remove it

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. The plugin will be ready to use!

## Permissions

No special permissions are required. All players can use the bounty system.

## Configuration

The plugin uses default settings and doesn't require configuration files.

## Dependencies

- Spigot/Paper 1.20+
- No external dependencies required

## Version History

- **v2.0**: Complete rewrite with GUI interface and custom currency system
- **v1.0**: Original version with economy plugin integration 