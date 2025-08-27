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

## How It Works

### Setting Bounties
1. Use `/bounty` to open the GUI
2. Click "Set Bounty" to select a target player
3. Choose from preset amounts ($100, $500, $1000) or select "Custom Amount"
4. For custom amounts, select from common values or enter a specific amount
5. The money is automatically deducted from your account

### Accepting Bounties
1. View active bounties in the GUI
2. Click on an available bounty to accept it
3. You become the exclusive hunter for that bounty
4. Enter "Hunter Mode" with special abilities

### Completing Bounties
- **Hunter kills target**: Hunter receives the bounty money
- **Target kills hunter**: Target receives the bounty money (reverse bounty)
- **Bounty removal**: Money is refunded to the person who placed it

### Economy Integration
- **Automatic payments**: Money is handled through Vault
- **Balance checking**: Players must have sufficient funds
- **Secure transactions**: All money transfers are handled by the economy plugin
- **Compatibility**: Works with any Vault-compatible economy plugin

## GUI Features

### Main Menu
- Set Bounty
- View Active Bounties
- My Accepted Bounties
- Player Status Display

### Bounty Setting
- Preset amounts: $100, $500, $1000
- Custom amount selection
- Target player selection (online/offline)
- Real-time balance checking

### Bounty Management
- List all active bounties
- Accept available bounties
- Track accepted bounties
- Manage hunter/target modes

## Technical Details

- **Data Storage**: YAML-based configuration files
- **Economy API**: Full Vault integration
- **Event Handling**: Comprehensive Bukkit event system
- **Player Tracking**: Real-time player location tracking
- **Mode Management**: Dynamic player mode switching

## Support

For issues or questions:
1. Check the console for error messages
2. Verify Vault and economy plugin are properly installed
3. Ensure proper permissions are set
4. Check server logs for detailed information

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