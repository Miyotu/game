# MiyotuGame - Minecraft 1.20.1 Bukkit/Spigot Plugin

A comprehensive Minecraft plugin with extensive features including player management, economy system, protection mechanics, mini-games, social features, and admin tools.

## 🌟 Features

### 📊 Player Management
- **Custom Commands**: `/heal`, `/fly`, `/god`, `/tp`
- **Player Statistics**: Comprehensive tracking of player activities
- **Rank System**: Hierarchical permission system with customizable ranks
- **God Mode & Flight**: Toggle invincibility and flight capabilities

### 💰 Economy System
- **Currency System**: Full-featured economy with configurable currency
- **Balance Management**: View, send, and manage player balances
- **Daily Rewards**: Daily rewards with streak bonuses (up to 30 days)
- **Transaction Logging**: Complete audit trail of all economic activities
- **Shop System**: GUI-based shop with categories and items

### 🛡️ Protection System
- **Region Protection**: WorldGuard-like region management
- **PvP Controls**: Fine-grained PvP settings per region
- **Grief Protection**: Automated protection against griefing
- **Block Protection**: Protect valuable blocks and containers

### 🎮 Mini Games
- **Parkour System**: Create and manage parkour courses with leaderboards
- **Quiz Game**: Interactive quiz system with rewards
- **Treasure Hunt**: Exciting treasure hunting with clues and rewards
- **Competitive Elements**: Rankings and achievements

### 👥 Social Features
- **Chat Formatting**: Rich chat formatting with rank integration
- **Private Messages**: Secure player-to-player messaging
- **Friend System**: Add/remove friends with online notifications
- **Social Commands**: `/msg`, `/reply`, `/friend`

### 🔧 Admin Tools
- **Ban/Kick System**: Comprehensive moderation tools
- **Admin Menu**: GUI-based administration interface
- **Logging System**: Detailed logging of all admin actions
- **Permission Management**: Advanced permission system integration

## 🗄️ Database Schema

The plugin uses MySQL with the following tables:

- **mg_players**: Player data, balances, ranks, and statistics
- **mg_friends**: Friend relationships between players
- **mg_regions**: Protected regions and their settings
- **mg_parkour**: Parkour courses and configurations
- **mg_parkour_records**: Player completion times and records
- **mg_bans**: Ban records and management
- **mg_logs**: Comprehensive action logging
- **mg_transactions**: Economy transaction history

## ⚙️ Configuration

### Main Configuration (`config.yml`)

```yaml
# General Settings
general:
  language: "tr"  # tr, en
  prefix: "&6[&bMiyotuGame&6] "
  debug: false
  auto-save-interval: 5

# Database Configuration
database:
  type: "mysql"
  mysql:
    host: "localhost"
    port: 3306
    database: "miyotugame"
    username: "root"
    password: "password"
    pool:
      maximum-pool-size: 10
      minimum-idle: 5

# Economy System
economy:
  enabled: true
  starting-balance: 1000
  currency-symbol: "₺"
  currency-name:
    singular: "coin"
    plural: "coins"
  daily-reward:
    enabled: true
    base-amount: 500
    streak-bonus:
      enabled: true
      max-streak: 30
      bonus-per-day: 50
```

### Language Support

The plugin supports multiple languages:
- **Turkish** (`tr.yml`): Complete Turkish translation
- **English** (`en.yml`): Complete English translation

All messages, commands, and GUI elements are fully localized.

## 🚀 Installation

1. **Requirements**:
   - Minecraft Server 1.20.1
   - Bukkit/Spigot/Paper
   - MySQL Database
   - Java 17+

2. **Installation Steps**:
   ```bash
   # Build the plugin
   mvn clean package
   
   # Copy to plugins folder
   cp target/game-plugin-1.0.0.jar /path/to/server/plugins/
   
   # Configure database in config.yml
   # Start server
   ```

3. **Database Setup**:
   - Create MySQL database
   - Configure connection in `config.yml`
   - Tables will be created automatically

## 📝 Commands

### Player Commands
- `/balance [player]` - Check balance
- `/pay <player> <amount>` - Send money
- `/daily` - Claim daily reward
- `/shop` - Open shop GUI
- `/friend <add|remove|list> [player]` - Manage friends
- `/msg <player> <message>` - Send private message
- `/reply <message>` - Reply to last message
- `/stats [player]` - View statistics

### Mini Game Commands
- `/parkour <create|join|leave> [name]` - Parkour management
- `/quiz <start|answer> [answer]` - Quiz game
- `/treasure <start|hint>` - Treasure hunt

### Admin Commands
- `/heal [player]` - Heal player
- `/fly [player]` - Toggle flight
- `/god [player]` - Toggle god mode
- `/tp <player|coordinates>` - Teleport
- `/ban <player> [reason]` - Ban player
- `/kick <player> [reason]` - Kick player
- `/region <create|delete> <name>` - Region management
- `/rank <set|get> <player> [rank]` - Rank management
- `/gameadmin` - Admin menu

## 🔐 Permissions

### Player Permissions (Default: True)
- `miyotugame.balance` - Check balance
- `miyotugame.pay` - Send money
- `miyotugame.daily` - Claim daily rewards
- `miyotugame.shop` - Use shop
- `miyotugame.msg` - Send messages
- `miyotugame.friend` - Manage friends
- `miyotugame.parkour` - Play parkour
- `miyotugame.quiz` - Play quiz
- `miyotugame.treasure` - Treasure hunt
- `miyotugame.stats` - View statistics

### Admin Permissions (Default: OP)
- `miyotugame.heal` - Heal players
- `miyotugame.fly` - Toggle flight
- `miyotugame.god` - Toggle god mode
- `miyotugame.tp` - Teleport
- `miyotugame.ban` - Ban players
- `miyotugame.kick` - Kick players
- `miyotugame.region` - Manage regions
- `miyotugame.rank` - Manage ranks
- `miyotugame.admin` - Admin access
- `miyotugame.reload` - Reload plugin

## 🏗️ Architecture

### Manager Pattern
The plugin uses a manager pattern for clean separation of concerns:

- **ConfigManager**: Configuration handling
- **LanguageManager**: Multilingual support
- **DatabaseManager**: Database operations with connection pooling
- **PlayerManager**: Player data management
- **EconomyManager**: Economy and transactions
- **RegionManager**: Protection and regions
- **GameManager**: Mini-games
- **SocialManager**: Chat and friends
- **AdminManager**: Admin tools and logging
- **RankManager**: Permission and ranks
- **StatisticsManager**: Player statistics

### Database Layer
- **HikariCP**: High-performance connection pooling
- **Async Operations**: Non-blocking database operations
- **Transaction Support**: ACID compliance
- **Migration System**: Automatic schema updates

### Performance Features
- **Caching**: Smart caching for frequently accessed data
- **Async Processing**: Non-blocking operations
- **Connection Pooling**: Efficient database connections
- **Metrics**: Performance monitoring and statistics

## 🔧 Development

### Building from Source
```bash
git clone https://github.com/Miyotu/game.git
cd game
mvn clean package
```

### Adding Features
1. Create manager class in `managers/` package
2. Add configuration options to `config.yml`
3. Add language keys to language files
4. Register commands in main plugin class
5. Add permissions to `plugin.yml`

### Database Changes
- Modify `DatabaseManager.createTables()`
- Update data models in `models/` package
- Add migration scripts if needed

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📞 Support

For support, please create an issue on GitHub or contact the development team.

## 🎯 Roadmap

- [ ] Web interface for administration
- [ ] Advanced statistics and analytics
- [ ] Integration with Discord bots
- [ ] More mini-games and activities
- [ ] Advanced economy features (banks, loans)
- [ ] Clan/guild system
- [ ] Achievement system
- [ ] Market and auction house

---

**MiyotuGame** - The most comprehensive Minecraft server plugin for 1.20.1