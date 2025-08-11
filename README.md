# BountyHunter Plugin v2.1

A comprehensive Minecraft bounty hunting plugin with GUI interface, hunter/target modes, enhanced tracking, and 24-hour cooldown protection.

## ✨ Key Features

### 🎯 **Smart Bounty System**
- **GUI Interface**: Easy-to-use graphical menus for all bounty operations
- **Multiple Currencies**: Diamonds, emeralds, and netherite ingots (1-64 amounts)
- **Offline Support**: Place bounties on players who have previously joined
- **24-Hour Cooldown**: Prevents bounty spam after claims
- **Data Persistence**: Survives server restarts and crashes
- **Persistent Bounties**: Bounties only expire when the hunter kills the target, not from environmental deaths or suicide

### ⚔️ **Hunter & Target Modes**
- **Exclusive Hunting**: Accept bounties to become the designated hunter
- **Hunter Mode**: Night vision + enhanced tracking tools
- **Target Mode**: Alert notifications when being hunted
- **Reverse Bounty**: Targets can claim the bounty by killing their hunter
- **Mode Effects**: Automatic activation when both players are online

### 🧭 **Enhanced Tracking System**
- **Real-Time Compass**: Auto-updates every 2 seconds pointing to target
- **Distance Alerts**: Proximity-based notifications (Very Close, Nearby, etc.)
- **Tracking Tools**: Automatic compass and spyglass provision
- **Direction Info**: Live distance and direction updates
- **Cross-Dimension**: Handles different worlds gracefully

## 🎮 Quick Start

### Commands
- `/bounty gui` - Open main menu
- `/bounty set <player> <currency> <amount>` - Set bounty
- `/bounty accept <player>` - Accept a bounty hunt
- `/bounty track` - Enhanced tracking info (hunter mode only)
- `/bounty status` - Check your current mode and bounty info
- `/bounty cooldown` - Check cooldown status

### GUI Usage
1. **Set Bounty**: Select player → Choose currency → Set amount
2. **Accept Hunt**: Browse available bounties → Click to accept
3. **Track Target**: Use compass, spyglass, and `/bounty track` command
4. **Complete Hunt**: Kill target to claim reward (or target kills you!)

## 🛡️ Anti-Abuse Features

- **24-Hour Cooldowns**: No new bounties on recently claimed players
- **No Speed Effects**: Balanced gameplay without movement advantages  
- **Enhanced Tracking**: Skill-based hunting with proper tools
- **Cooldown Commands**: Check and manage cooldowns (admin can clear)
- **Persistent Bounties**: Bounties remain active until properly claimed by the hunter

## 🔧 Installation

1. Download `bountyhunter-2.1-SNAPSHOT.jar`
2. Place in server's `plugins` folder
3. Restart server
4. Ready to use! No permissions or configuration needed

## 📁 Data Files

- `bounties.yml` - Active bounties
- `players.yml` - Known player database  
- `cooldowns.yml` - 24-hour cooldown tracking

## 🎯 How It Works

1. **Place Bounty**: Currency deducted, bounty becomes available
2. **Accept Hunt**: Enter hunter mode with tracking tools
3. **Track Target**: Use compass, enhanced tracking, and proximity alerts
4. **Complete Hunt**: Kill target for reward, or target kills hunter for reverse bounty
5. **Cooldown**: 24-hour protection prevents immediate re-bounty

## 🔄 Version History

- **v2.1**: Hunter/target modes, enhanced tracking, 24-hour cooldowns, reverse bounties, persistent bounties
- **v2.0**: GUI interface, multiple currencies, bounty acceptance system
- **v1.0**: Original command-based version

## 🎮 What Makes This Special?

- **No Economy Plugin**: Self-contained currency system
- **Balanced PvP**: No speed advantages, skill-based hunting
- **Smart Protection**: Cooldowns prevent harassment
- **Rich Tracking**: Real-time updates with tools and notifications
- **Fair Play**: Targets can fight back and claim bounties themselves
- **Persistent Bounties**: Bounties don't disappear from environmental deaths or suicide - only when properly claimed

## ⚠️ Important Bounty Behavior

**Bounties are persistent and only expire when:**
- The **hunter kills the target** (normal completion)
- The **target kills the hunter** (reverse bounty)
- The bounty is **manually removed** by the placer
- The bounty is **abandoned** by the hunter

**Bounties do NOT expire from:**
- Environmental deaths (falling, drowning, etc.)
- Suicide
- Being killed by someone other than the hunter
- Server restarts or crashes

This ensures that bounties remain active until properly resolved through PvP combat between the hunter and target.

Perfect for PvP servers wanting structured bounty hunting without exploits!