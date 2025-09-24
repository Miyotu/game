# MiyotuGame Plugin Setup Guide

This guide will walk you through setting up the MiyotuGame plugin on your Minecraft server.

## 📋 Prerequisites

### Server Requirements
- **Minecraft Version**: 1.20.1
- **Server Software**: Bukkit, Spigot, or Paper
- **Java Version**: 17 or higher
- **RAM**: Minimum 2GB recommended
- **Database**: MySQL 8.0 or higher

### Dependencies (Optional but Recommended)
- **Vault**: For economy integration with other plugins
- **PlaceholderAPI**: For placeholder support in other plugins
- **WorldGuard**: For enhanced region protection

## 🗄️ Database Setup

### 1. Install MySQL

#### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install mysql-server
sudo mysql_secure_installation
```

#### Windows:
Download and install MySQL from the official website: https://dev.mysql.com/downloads/mysql/

#### Docker:
```bash
docker run --name miyotugame-mysql -e MYSQL_ROOT_PASSWORD=your_password -p 3306:3306 -d mysql:8.0
```

### 2. Create Database and User

Connect to MySQL as root:
```bash
mysql -u root -p
```

Execute the following SQL commands:
```sql
-- Create database
CREATE DATABASE miyotugame CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user (replace 'your_password' with a strong password)
CREATE USER 'miyotugame'@'localhost' IDENTIFIED BY 'your_password';

-- Grant privileges
GRANT ALL PRIVILEGES ON miyotugame.* TO 'miyotugame'@'localhost';

-- If connecting from a different server, use:
-- GRANT ALL PRIVILEGES ON miyotugame.* TO 'miyotugame'@'%';

-- Apply changes
FLUSH PRIVILEGES;

-- Exit MySQL
EXIT;
```

### 3. Test Database Connection

```bash
mysql -u miyotugame -p miyotugame
```

If successful, you should see the MySQL prompt. Type `EXIT;` to quit.

## 🔧 Plugin Installation

### 1. Build the Plugin

If you have the source code:
```bash
# Clone the repository
git clone https://github.com/Miyotu/game.git
cd game

# Build with Maven
mvn clean package

# The JAR file will be in target/game-plugin-1.0.0.jar
```

### 2. Install on Server

1. Stop your Minecraft server
2. Copy the JAR file to your server's `plugins` folder:
   ```bash
   cp target/game-plugin-1.0.0.jar /path/to/your/server/plugins/
   ```
3. Start your server to generate the default configuration files
4. Stop the server again to configure the plugin

## ⚙️ Configuration

### 1. Database Configuration

Edit `plugins/MiyotuGame/config.yml`:

```yaml
database:
  type: "mysql"
  mysql:
    host: "localhost"
    port: 3306
    database: "miyotugame"
    username: "miyotugame"
    password: "your_password"
    pool:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 2. General Settings

Configure basic plugin settings:

```yaml
general:
  language: "tr"  # or "en" for English
  prefix: "&6[&bMiyotuGame&6] "
  debug: false
  auto-save-interval: 5
```

### 3. Economy Settings

Configure the economy system:

```yaml
economy:
  enabled: true
  starting-balance: 1000
  currency-symbol: "₺"
  currency-name:
    singular: "coin"
    plural: "coins"
  max-balance: 999999999
  daily-reward:
    enabled: true
    base-amount: 500
    streak-bonus:
      enabled: true
      max-streak: 30
      bonus-per-day: 50
```

### 4. Shop Configuration

Configure the shop system:

```yaml
shop:
  enabled: true
  categories:
    blocks:
      name: "Blocks"
      icon: "STONE"
      items:
        - "STONE:10"
        - "DIRT:5"
        - "COBBLESTONE:8"
    tools:
      name: "Tools"
      icon: "DIAMOND_PICKAXE"
      items:
        - "WOODEN_PICKAXE:50"
        - "STONE_PICKAXE:100"
        - "IRON_PICKAXE:500"
```

### 5. Ranks Configuration

Configure the rank system:

```yaml
ranks:
  enabled: true
  default-rank: "player"
  ranks:
    player:
      name: "&7Player"
      prefix: "&7[Player] "
      permissions:
        - "miyotugame.player.*"
    vip:
      name: "&6VIP"
      prefix: "&6[VIP] "
      permissions:
        - "miyotugame.player.*"
        - "miyotugame.heal"
    admin:
      name: "&cAdmin"
      prefix: "&c[Admin] "
      permissions:
        - "miyotugame.*"
```

## 🔐 Permissions Setup

### Using LuckPerms (Recommended)

1. Install LuckPerms: https://luckperms.net/download
2. Configure permissions:

```bash
# Give all players basic permissions
lp group default permission set miyotugame.player.* true

# Create VIP group
lp creategroup vip
lp group vip permission set miyotugame.player.* true
lp group vip permission set miyotugame.heal true

# Create admin group
lp creategroup admin
lp group admin permission set miyotugame.* true

# Add players to groups
lp user PlayerName parent set vip
lp user AdminName parent set admin
```

### Using PEX (PermissionsEx)

Add to your permissions.yml:

```yaml
groups:
  default:
    permissions:
      - miyotugame.player.*
  
  vip:
    inheritance:
      - default
    permissions:
      - miyotugame.heal
  
  admin:
    permissions:
      - miyotugame.*
```

## 🚀 First Run

### 1. Start the Server

Start your Minecraft server and watch the console for any errors:

```bash
# Look for these messages:
[INFO] [MiyotuGame] Enabling MiyotuGame Plugin v1.0.0
[INFO] [MiyotuGame] Configuration loaded successfully!
[INFO] [MiyotuGame] Language system initialized!
[INFO] [MiyotuGame] Successfully connected to MySQL database!
[INFO] [MiyotuGame] Database tables created successfully!
[INFO] [MiyotuGame] All managers initialized!
[INFO] [MiyotuGame] Commands registered successfully!
[INFO] [MiyotuGame] Event listeners registered successfully!
[INFO] [MiyotuGame] MiyotuGame Plugin has been enabled successfully!
```

### 2. Test Basic Functionality

Connect to your server and test basic commands:

```bash
# Check your balance
/balance

# Claim daily reward
/daily

# If you're an admin, test admin commands
/heal
/fly
```

### 3. Verify Database

Check that tables were created in MySQL:

```sql
USE miyotugame;
SHOW TABLES;

-- You should see:
-- mg_players, mg_friends, mg_regions, mg_parkour, 
-- mg_parkour_records, mg_bans, mg_logs, mg_transactions
```

## 🔍 Troubleshooting

### Common Issues

#### "Failed to connect to database"
- Check MySQL server is running: `sudo systemctl status mysql`
- Verify database credentials in config.yml
- Check network connectivity if using remote database
- Ensure MySQL user has proper permissions

#### "Plugin failed to load"
- Check server.log for detailed error messages
- Verify Java version: `java -version`
- Ensure server is compatible (Bukkit/Spigot 1.20.1)
- Check for plugin conflicts

#### "Commands not working"
- Verify permissions are set correctly
- Check that plugin is enabled: `/plugins`
- Look for permission errors in console
- Test with OP permissions first

#### "Database tables not created"
- Check MySQL user has CREATE privileges
- Verify database name is correct
- Look for SQL errors in console
- Try connecting to database manually

### Debug Mode

Enable debug mode in config.yml for more detailed logging:

```yaml
general:
  debug: true
```

This will provide additional information in the console about plugin operations.

### Log Files

Check these log files for errors:
- `logs/latest.log` - Server log
- `plugins/MiyotuGame/debug.log` - Plugin debug log (if enabled)

## 📊 Monitoring and Maintenance

### Database Maintenance

Regular maintenance tasks:

```sql
-- Check table sizes
SELECT 
  table_name,
  round(((data_length + index_length) / 1024 / 1024), 2) as 'Size (MB)'
FROM information_schema.tables 
WHERE table_schema = 'miyotugame';

-- Clean old logs (older than 30 days)
DELETE FROM mg_logs WHERE created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);

-- Optimize tables
OPTIMIZE TABLE mg_players, mg_transactions, mg_logs;
```

### Performance Monitoring

Watch for these metrics:
- Database connection pool usage
- Memory usage of plugin
- Command execution times
- Player data cache hit rates

### Backup Strategy

Regular backups are essential:

```bash
# Database backup
mysqldump -u miyotugame -p miyotugame > backup_$(date +%Y%m%d).sql

# Configuration backup
tar -czf config_backup_$(date +%Y%m%d).tar.gz plugins/MiyotuGame/
```

## 🆙 Updates

### Updating the Plugin

1. Stop the server
2. Backup current plugin and database
3. Replace the JAR file
4. Check for configuration changes
5. Start the server
6. Verify functionality

### Configuration Migration

When updating, check for new configuration options and add them to your config.yml file. The plugin will use defaults for missing values.

## 📞 Support

If you encounter issues:

1. Check this setup guide
2. Review the troubleshooting section
3. Check the plugin logs
4. Search existing GitHub issues
5. Create a new issue with detailed information

Include this information when asking for help:
- Server software and version
- Plugin version
- Java version
- Error messages from logs
- Configuration files (remove sensitive data)

---

**Happy Gaming with MiyotuGame!** 🎮