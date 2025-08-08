# BountyHunter Plugin v2.1

A comprehensive GUI-based bounty hunting plugin for Minecraft servers with an advanced bounty acceptance system. Features exclusive hunter assignments, interactive GUI management, and persistent data storage using diamonds, emeralds, and netherite ingots as currency.

## Features

### 🎯 **Core Bounty System**
- **GUI Interface**: Easy-to-use graphical interface for managing bounties
- **Multiple Currencies**: Support for diamonds, emeralds, and netherite ingots
- **Large Amount Support**: Bounties can range from 1-64 of any currency
- **Real-time Updates**: Bounties are automatically claimed when players are killed
- **Inventory Integration**: Currency is taken from and given to player inventories
- **Data Persistence**: Bounties survive server restarts and crashes
- **Offline Player Support**: Place bounties on offline players

### 🎮 **NEW in v2.1: Bounty Acceptance System**
- **Exclusive Hunting**: Players can accept specific bounties to become the designated hunter
- **Hunter Protection**: Only the accepted hunter can claim the bounty reward
- **Interactive GUI**: Accept, abandon, and manage bounties through intuitive menus
- **Compass Tracking**: Point your compass toward your accepted bounty target
- **Smart Validation**: Prevents accepting your own bounties or bounties on yourself
- **Flexible Management**: Abandon hunts to make bounties available again
- **Dual Interface**: Both command and GUI support for all bounty operations

## Commands

### 📝 **Basic Commands**
- `/bounty gui` - Opens the main bounty GUI menu
- `/bounty set <player> <currency> <amount>` - Set a bounty via command
- `/bounty list` - List all active bounties with acceptance status
- `/bounty remove <player>` - Remove a bounty you placed

### 🎯 **NEW in v2.1: Acceptance Commands**
- `/bounty accept <player>` - Accept a bounty on the specified player
- `/bounty abandon` - Abandon your currently accepted bounty

## GUI Usage

### 🏠 **Main Menu Navigation**
1. **Set Bounty** - Place bounties on players
2. **View Active Bounties** - Browse and accept available bounties
3. **My Accepted Bounties** - Manage your active hunts (NEW!)

### 💰 **Setting Bounties**
1. Click "Set Bounty" → Select target player → Choose currency → Select amount (1-64)
2. Currency is automatically deducted from your inventory
3. Bounty becomes available for other players to accept

### 🎯 **NEW in v2.1: Accepting & Managing Bounties**
1. **Browse Available Bounties**: Click "View Active Bounties"
   - 🟢 **[AVAILABLE]** bounties can be accepted
   - 🔴 **[ACCEPTED by PlayerName]** bounties are already claimed
2. **Accept a Bounty**: Click on an available bounty → Confirm acceptance
3. **Manage Your Hunt**: Click "My Accepted Bounties"
   - **Track Target**: Set compass to point at your target
   - **Abandon Hunt**: Release bounty back to available pool
   - View target online/offline status
4. **Complete Hunt**: Kill your target to automatically claim the reward!

## Currency Types

- **Diamonds**: Blue currency option (1-64 diamonds)
- **Emeralds**: Green currency option (1-64 emeralds)  
- **Netherite Ingots**: Purple currency option (1-64 netherite ingots)

## How It Works

### 💰 **Bounty Creation & Management**
1. **Setting Bounties**: Players must have the required currency in their inventory
2. **Currency Removal**: The specified amount is removed from the player's inventory
3. **Bounty Storage**: Bounties are stored persistently in `bounties.yml` file
4. **Bounty Removal**: Only the player who placed the bounty can remove it

### 🎯 **NEW in v2.1: Bounty Acceptance System**
5. **Browse & Accept**: Players can view all available bounties and accept specific ones
6. **Exclusive Hunting**: Once accepted, only the hunter can claim that bounty
7. **Hunter Validation**: System prevents accepting own bounties or bounties on yourself
8. **Flexible Management**: Hunters can abandon bounties, making them available again

### ⚔️ **Bounty Completion**
9. **Mixed Claims**: Unaccepted bounties can be claimed by anyone (legacy behavior)
10. **Exclusive Claims**: Accepted bounties can only be claimed by the designated hunter
11. **Automatic Rewards**: Currency is automatically given when bounties are claimed
12. **Smart Notifications**: Different messages for accepted vs. unaccepted bounty claims

### 🔄 **Persistence & Reliability**
13. **Data Persistence**: All bounty data including hunter assignments survive server restarts
14. **Offline Support**: Bounties and acceptances remain active for offline players
15. **Crash Recovery**: All bounty information is preserved even during server crashes

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
    hunterUUID: "uuid-of-hunter-who-accepted" # NEW in v2.1
    hunterName: "HunterPlayerName"            # NEW in v2.1
    isAccepted: true                          # NEW in v2.1
```

## Dependencies

- Spigot/Paper 1.20+
- No external dependencies required

## Version History

- **v2.1**: 🎯 **Major Update** - Added bounty acceptance system with exclusive hunter assignments, enhanced GUI with interactive bounty management, compass tracking, and smart validation
- **v2.0**: Complete rewrite with GUI interface, custom currency system, and persistent data storage
- **v1.0**: Original version with economy plugin integration

## 🎮 What's New in v2.1?

### 🎯 **Bounty Acceptance System**
- Players can now accept specific bounties to become exclusive hunters
- Only the accepted hunter can claim the bounty reward
- Prevents bounty stealing and creates strategic hunter-target relationships

### 🖱️ **Enhanced GUI Experience**
- **My Accepted Bounties** menu for managing active hunts
- Interactive bounty browsing with clear status indicators
- Confirmation dialogs for important actions
- Compass tracking integration

### 🧭 **Smart Features**
- **Compass Tracking**: Point compass toward accepted bounty targets
- **Status Indicators**: Clear visual feedback on bounty availability
- **Smart Validation**: Prevents accepting own bounties or invalid targets
- **Flexible Management**: Abandon hunts to make bounties available again

### 🔄 **Backwards Compatibility**
- All existing bounties continue to work normally
- Unaccepted bounties can still be claimed by anyone
- Command system enhanced but maintains full compatibility
- No breaking changes to existing functionality 