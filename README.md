# BountyHunter Plugin

A Minecraft Bukkit/Spigot plugin that allows players to place bounties on other players. When a player with a bounty is killed, the killer receives the bounty reward.

## Features

- Place bounties on other players
- List all active bounties
- Remove bounties (with refund)
- Automatic bounty claiming when targets are killed
- Economy integration with Vault
- Configurable messages and settings

## Requirements

- Minecraft Server (Bukkit/Spigot/Paper) 1.20+
- Java 17 or higher
- Vault plugin
- An economy plugin (like EssentialsX, iConomy, etc.)

## Installation

1. **Install Maven** (if not already installed):
   - Download from: https://maven.apache.org/download.cgi
   - Extract to a directory
   - Add Maven's bin directory to your system PATH

2. **Build the plugin**:
   ```bash
   mvn clean package
   ```

3. **Install on your server**:
   - Copy the generated JAR file from `target/` to your server's `plugins/` folder
   - Restart your server

4. **Install dependencies**:
   - Install Vault plugin
   - Install an economy plugin (EssentialsX recommended)

## Commands

- `/bounty set <player> <amount>` - Place a bounty on a player
- `/bounty list` - List all active bounties
- `/bounty remove <player>` - Remove a bounty (refunds the money)
- `/bh` or `/bounties` - Aliases for the bounty command

## Permissions

Currently, no permissions are required. All players can use the bounty system.

## Configuration

The plugin creates a `config.yml` file in the plugin folder with customizable settings:

- `minimum-bounty`: Minimum amount for bounties (default: 1.0)
- `maximum-bounty`: Maximum amount for bounties (default: -1, no limit)
- `messages`: Customizable messages for all plugin interactions

## How it Works

1. **Setting a Bounty**: Players can place bounties on other players using `/bounty set <player> <amount>`. The money is withdrawn from their account immediately.

2. **Bounty List**: Players can view all active bounties using `/bounty list`.

3. **Bounty Removal**: Players can remove bounties using `/bounty remove <player>`, which refunds the money.

4. **Bounty Claiming**: When a player with a bounty is killed by another player, the killer automatically receives the bounty reward.

5. **Bounty Loss**: If a player with a bounty dies from environmental damage (falling, drowning, etc.) or commits suicide, the bounty is lost.

## Troubleshooting

### Common Issues

1. **"Vault with an economy plugin not found"**
   - Make sure Vault is installed
   - Make sure an economy plugin is installed and configured

2. **Plugin won't load**
   - Check that you're using Java 17 or higher
   - Check that your server supports the API version (1.20)

3. **Commands not working**
   - Make sure the plugin is properly loaded
   - Check server logs for any error messages

### Building Issues

If you encounter Maven build issues:

1. Make sure Maven is properly installed and in your PATH
2. Try running `mvn clean` first, then `mvn compile`
3. Check that you have Java 17+ installed

## Development

This plugin is built with:
- Java 17
- Maven
- Spigot API 1.20.1
- Vault API

## License

This project is open source. Feel free to modify and distribute as needed. 