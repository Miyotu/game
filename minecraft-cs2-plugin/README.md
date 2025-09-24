# CS2 Plugin - Minecraft Counter-Strike 2 Style Plugin

## Phase 1: Basic Infrastructure and Lobby System

This plugin provides a Counter-Strike 2 style game mode for Minecraft servers.

### Features Implemented

#### Core Infrastructure
- ✅ Main plugin class (CS2Plugin)
- ✅ Configuration system (config.yml)
- ✅ MySQL database connection with HikariCP
- ✅ Player data manager
- ✅ Event handling system

#### Lobby System
- ✅ Lobby creation and management
- ✅ NPC system for game joining (requires Citizens plugin)
- ✅ Waiting lobby with 10 player capacity
- ✅ Team selection system (CT/T)

#### Commands
- ✅ `/cs2 createlobby <name>` - Create a new lobby
- ✅ `/cs2 setlobby <name>` - Set lobby spawn point
- ✅ `/cs2 join [lobby]` - Join a game or open lobby selector
- ✅ `/cs2 leave` - Leave current lobby
- ✅ `/cs2 team <ct/t>` - Select team or open team selector
- ✅ `/cs2 list` - List all available lobbies

#### GUI Systems
- ✅ Lobby selection menu
- ✅ Team selection menu

#### Player States
- ✅ LOBBY (in lobby)
- ✅ WAITING (waiting for game)
- ✅ PLAYING (in game) - *prepared for future phases*
- ✅ SPECTATING (spectating) - *prepared for future phases*

### Database Schema

The plugin creates the following tables:
- `cs2_players` - Player statistics and data
- `cs2_lobbies` - Lobby configurations and spawn points
- `cs2_games` - Game history and results
- `cs2_game_teams` - Team participation in games

### Requirements

- Bukkit/Spigot 1.20.1 or higher
- MySQL database
- Citizens plugin (for NPC functionality)
- Java 8 or higher

### Configuration

Edit the `config.yml` file to configure:
- Database connection settings
- Lobby settings (max players, waiting time, etc.)
- Team configurations
- NPC settings
- Custom messages

### Installation

1. Build the plugin with Maven: `mvn clean package`
2. Place the generated JAR file in your server's `plugins` folder
3. Install the Citizens plugin
4. Configure your database settings in `config.yml`
5. Restart the server

### Permissions

- `cs2.use` - Basic plugin usage (default: true)
- `cs2.admin` - Admin commands (default: op)
- `cs2.createlobby` - Create lobbies (default: op)
- `cs2.setlobby` - Set lobby spawn points (default: op)
- `cs2.join` - Join games (default: true)
- `cs2.leave` - Leave games (default: true)
- `cs2.team` - Select teams (default: true)

### Next Phases

This is Phase 1 of the CS2 plugin development. Future phases will include:
- Spawn system and round mechanics
- Economy and shop system
- Weapon system
- Round-based gameplay
- Statistics tracking
- Advanced game modes