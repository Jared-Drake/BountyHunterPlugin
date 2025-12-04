# BountyHunter Plugin

A comprehensive bounty hunting plugin for Minecraft servers with economy integration using Vault and EssentialsX.

## Features

- **Economy Integration**: Uses Vault to integrate with EssentialsX, iConomy, and other economy plugins
- **GUI-Based Interface**: Easy-to-use graphical interface for managing bounties
- **Bounty System**: Place bounties on players using money instead of materials
- **Hunter/Target Modes**: Special gameplay modes for hunters and targets
- **Compass Tracking**: Hunters can track their targets using compass
- **Cooldown System**: Prevents bounty spam with configurable cooldowns
- **Offline Support**: Place bounties on offline players
- **Permission System**: Comprehensive permission-based access control

## Dependencies

- **Vault**: Required for economy integration
- **EssentialsX**: Recommended economy plugin (or any Vault-compatible economy plugin)
- **Spigot/Paper**: Server software (1.20+)

## Installation

1. **Install Vault** on your server
2. **Install an economy plugin** (EssentialsX, iConomy, etc.)
3. **Download** the BountyHunter plugin JAR
4. **Place** the JAR in your server's `plugins` folder
5. **Restart** your server

## Configuration

The plugin will automatically create configuration files on first run:

- `config.yml` - Main plugin configuration
- `bounties.yml` - Bounty data storage
- `plugin.yml` - Plugin metadata and permissions

## Commands

### Main Commands
- `/bounty` - Open the bounty management GUI
- `/bounty gui` - Open the bounty management GUI
- `/bounty set <player> <amount>` - Set a bounty on a player (e.g., `/bounty set PlayerName 1000`)
- `/bounty list` - List all active bounties
- `/bounty remove <player>` - Remove a bounty you placed
- `/bounty track` - Track your target or hunter

### Cooldown Commands
- `/bounty cooldown check <player>` - Check if a player is on cooldown
- `/bounty cooldown list` - List all active cooldowns (Admin only)
- `/bounty cooldown clear <player>` - Clear a player's cooldown (Admin only)
- `/bounty cooldown clear` - Clear all cooldowns (Admin only)

## Permissions

- `bountyhunter.set` - Allow setting bounties
- `bountyhunter.remove` - Allow removing bounties
- `bountyhunter.admin` - Administrative access
- `bountyhunter.selfremove` - Allow removing own bounty


## Version History

### 2.1-SNAPSHOT
- **Major Update**: Replaced material-based currency with economy integration
- **Added**: Vault dependency and economy plugin support
- **Updated**: GUI to show money amounts instead of materials
- **Improved**: Bounty amount selection with preset and custom options
- **Enhanced**: Error handling for economy transactions
- **Fixed**: Various compatibility issues

### Previous Versions
- Material-based currency system (Diamonds, Emeralds, Netherite)
- Basic bounty functionality
- Simple GUI interface

## License

This plugin is provided as-is for educational and server use purposes.
