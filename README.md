# BountyHunter Plugin v2.0

A GUI-based bounty hunting plugin for Minecraft servers that uses diamonds, emeralds, and netherite ingots as currency with persistent data storage.

## Features

- **GUI Interface**: Easy-to-use graphical interface for managing bounties
- **Multiple Currencies**: Support for diamonds, emeralds, and netherite ingots
- **Large Amount Support**: Bounties can range from 1-64 of any currency
- **Real-time Updates**: Bounties are automatically claimed when players are killed
- **Inventory Integration**: Currency is taken from and given to player inventories
- **Data Persistence**: Bounties survive server restarts and crashes
- **Offline Player Support**: Place bounties on offline players

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
5. **Choose Amount**: Select 1-64 of the chosen currency
6. **View Bounties**: Click "View Active Bounties" to see all current bounties

## Currency Types

- **Diamonds**: Blue currency option (1-64 diamonds)
- **Emeralds**: Green currency option (1-64 emeralds)  
- **Netherite Ingots**: Purple currency option (1-64 netherite ingots)

## How It Works

1. **Setting Bounties**: Players must have the required currency in their inventory
2. **Currency Removal**: The specified amount is removed from the player's inventory
3. **Bounty Storage**: Bounties are stored persistently in `bounties.yml` file
4. **Claiming Bounties**: When a player with a bounty is killed, the killer receives the currency
5. **Bounty Removal**: Only the player who placed the bounty can remove it
6. **Persistence**: Bounties survive server restarts and are automatically loaded
7. **Offline Support**: Bounties remain active even if the target player is offline

## Installation

1. Download the plugin JAR file
2. Place it in your server's `plugins` folder
3. Restart your server
4. The plugin will be ready to use!

## Permissions

No special permissions are required. All players can use the bounty system.

## Configuration

The plugin creates a `bounties.yml` file in the plugin folder that stores all active bounties. This file is automatically managed by the plugin and should not be edited manually.

### Data Persistence

- **Automatic Loading**: Bounties are automatically loaded when the server starts
- **Automatic Saving**: Bounties are saved immediately when placed or removed
- **Server Shutdown**: All bounties are saved when the server shuts down
- **Offline Players**: Bounties remain active even if the target player is offline
- **Crash Recovery**: Bounties are preserved even if the server crashes

### File Structure

The `bounties.yml` file stores bounty data in this format:
```yaml
bounties:
  "uuid-of-target-player":
    targetUUID: "uuid-of-target-player"
    placedByUUID: "uuid-of-player-who-placed-bounty"
    placedByName: "PlayerName"
    currency: "DIAMOND"
    amount: 10
```

## Dependencies

- Spigot/Paper 1.20+
- No external dependencies required

## Future Features in the works

 - Trackers compass for bounty hunters
 - Clues for hunters
 - Bounty Hunter leveling system
 - Bounty hunter contracts

## Version History

- **v2.0**: Complete rewrite with GUI interface, custom currency system, and persistent data storage
- **v1.0**: Original version with economy plugin integration 
