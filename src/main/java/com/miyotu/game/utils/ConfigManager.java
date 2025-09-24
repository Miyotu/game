package com.miyotu.game.utils;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration manager for the plugin
 * Handles all configuration loading and accessing
 */
public class ConfigManager {
    
    private final MiyotuGamePlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Load configuration from file
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    // General Settings
    public String getLanguage() {
        return config.getString("general.language", "tr");
    }
    
    public String getPrefix() {
        return ColorUtils.colorize(config.getString("general.prefix", "&6[&bMiyotuGame&6] "));
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("general.debug", false);
    }
    
    public int getAutoSaveInterval() {
        return config.getInt("general.auto-save-interval", 5);
    }
    
    // Database Settings
    public String getDatabaseType() {
        return config.getString("database.type", "mysql");
    }
    
    public String getMySQLHost() {
        return config.getString("database.mysql.host", "localhost");
    }
    
    public int getMySQLPort() {
        return config.getInt("database.mysql.port", 3306);
    }
    
    public String getMySQLDatabase() {
        return config.getString("database.mysql.database", "miyotugame");
    }
    
    public String getMySQLUsername() {
        return config.getString("database.mysql.username", "root");
    }
    
    public String getMySQLPassword() {
        return config.getString("database.mysql.password", "password");
    }
    
    public int getMaxPoolSize() {
        return config.getInt("database.mysql.pool.maximum-pool-size", 10);
    }
    
    public int getMinIdleConnections() {
        return config.getInt("database.mysql.pool.minimum-idle", 5);
    }
    
    public long getConnectionTimeout() {
        return config.getLong("database.mysql.pool.connection-timeout", 30000);
    }
    
    public long getIdleTimeout() {
        return config.getLong("database.mysql.pool.idle-timeout", 600000);
    }
    
    public long getMaxLifetime() {
        return config.getLong("database.mysql.pool.max-lifetime", 1800000);
    }
    
    // Economy Settings
    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", true);
    }
    
    public double getStartingBalance() {
        return config.getDouble("economy.starting-balance", 1000.0);
    }
    
    public String getCurrencySymbol() {
        return config.getString("economy.currency-symbol", "₺");
    }
    
    public String getCurrencyNameSingular() {
        return config.getString("economy.currency-name.singular", "coin");
    }
    
    public String getCurrencyNamePlural() {
        return config.getString("economy.currency-name.plural", "coins");
    }
    
    public double getMaxBalance() {
        return config.getDouble("economy.max-balance", 999999999.0);
    }
    
    public boolean isDailyRewardEnabled() {
        return config.getBoolean("economy.daily-reward.enabled", true);
    }
    
    public double getBaseDailyReward() {
        return config.getDouble("economy.daily-reward.base-amount", 500.0);
    }
    
    public boolean isStreakBonusEnabled() {
        return config.getBoolean("economy.daily-reward.streak-bonus.enabled", true);
    }
    
    public int getMaxStreak() {
        return config.getInt("economy.daily-reward.streak-bonus.max-streak", 30);
    }
    
    public double getBonusPerDay() {
        return config.getDouble("economy.daily-reward.streak-bonus.bonus-per-day", 50.0);
    }
    
    // Shop Settings
    public boolean isShopEnabled() {
        return config.getBoolean("shop.enabled", true);
    }
    
    // Player Management Settings
    public int getTeleportDelay() {
        return config.getInt("player-management.teleport.delay", 3);
    }
    
    public boolean isCancelOnMove() {
        return config.getBoolean("player-management.teleport.cancel-on-move", true);
    }
    
    public boolean isCancelOnDamage() {
        return config.getBoolean("player-management.teleport.cancel-on-damage", true);
    }
    
    public boolean isRestoreFood() {
        return config.getBoolean("player-management.heal.restore-food", true);
    }
    
    public boolean isPreventFallDamage() {
        return config.getBoolean("player-management.god-mode.prevent-fall-damage", true);
    }
    
    public boolean isPreventDrowning() {
        return config.getBoolean("player-management.god-mode.prevent-drowning", true);
    }
    
    // Rank Settings
    public boolean isRankSystemEnabled() {
        return config.getBoolean("ranks.enabled", true);
    }
    
    public String getDefaultRank() {
        return config.getString("ranks.default-rank", "player");
    }
    
    // Protection Settings
    public boolean isProtectionEnabled() {
        return config.getBoolean("protection.enabled", true);
    }
    
    public boolean isWorldGuardIntegration() {
        return config.getBoolean("protection.worldguard-integration", true);
    }
    
    public boolean isGriefProtectionEnabled() {
        return config.getBoolean("protection.grief-protection.enabled", true);
    }
    
    // Mini Games Settings
    public boolean isParkourEnabled() {
        return config.getBoolean("mini-games.parkour.enabled", true);
    }
    
    public boolean isResetOnFall() {
        return config.getBoolean("mini-games.parkour.reset-on-fall", true);
    }
    
    public boolean isTeleportOnFail() {
        return config.getBoolean("mini-games.parkour.teleport-on-fail", true);
    }
    
    public double getParkourCompletionReward() {
        return config.getDouble("mini-games.parkour.rewards.completion", 1000.0);
    }
    
    public boolean isTimeBonusEnabled() {
        return config.getBoolean("mini-games.parkour.rewards.time-bonus", true);
    }
    
    public boolean isQuizEnabled() {
        return config.getBoolean("mini-games.quiz.enabled", true);
    }
    
    public int getQuizTimeLimit() {
        return config.getInt("mini-games.quiz.time-limit", 30);
    }
    
    public double getRewardPerQuestion() {
        return config.getDouble("mini-games.quiz.reward-per-question", 100.0);
    }
    
    public boolean isTreasureHuntEnabled() {
        return config.getBoolean("mini-games.treasure-hunt.enabled", true);
    }
    
    public int getMaxActiveHunts() {
        return config.getInt("mini-games.treasure-hunt.max-active-hunts", 5);
    }
    
    public int getHuntDuration() {
        return config.getInt("mini-games.treasure-hunt.hunt-duration", 30);
    }
    
    // Social Settings
    public boolean isChatEnabled() {
        return config.getBoolean("social.chat.enabled", true);
    }
    
    public String getChatFormat() {
        return config.getString("social.chat.format", "{prefix}{player}: {message}");
    }
    
    public boolean isRankColorsEnabled() {
        return config.getBoolean("social.chat.rank-colors", true);
    }
    
    public boolean isPrivateMessagesEnabled() {
        return config.getBoolean("social.private-messages.enabled", true);
    }
    
    public boolean isSoundNotification() {
        return config.getBoolean("social.private-messages.sound-notification", true);
    }
    
    public String getPrivateMessageFormat() {
        return config.getString("social.private-messages.format", "&d[{sender} -> {receiver}] &f{message}");
    }
    
    public boolean isFriendsEnabled() {
        return config.getBoolean("social.friends.enabled", true);
    }
    
    public int getMaxFriends() {
        return config.getInt("social.friends.max-friends", 50);
    }
    
    public boolean isOnlineNotifications() {
        return config.getBoolean("social.friends.online-notifications", true);
    }
    
    // Admin Tools Settings
    public boolean isBanSystemEnabled() {
        return config.getBoolean("admin-tools.ban-system.enabled", true);
    }
    
    public boolean isBroadcastBans() {
        return config.getBoolean("admin-tools.ban-system.broadcast-bans", true);
    }
    
    public boolean isLoggingEnabled() {
        return config.getBoolean("admin-tools.logging.enabled", true);
    }
    
    public boolean isLogToFile() {
        return config.getBoolean("admin-tools.logging.log-to-file", true);
    }
    
    public boolean isLogToDatabase() {
        return config.getBoolean("admin-tools.logging.log-to-database", true);
    }
    
    // Performance Settings
    public boolean isAsyncDatabase() {
        return config.getBoolean("performance.async-database", true);
    }
    
    public int getPlayerDataCacheTime() {
        return config.getInt("performance.cache.player-data", 30);
    }
    
    public int getRegionDataCacheTime() {
        return config.getInt("performance.cache.region-data", 60);
    }
    
    public boolean isMetricsEnabled() {
        return config.getBoolean("performance.metrics.enabled", true);
    }
    
    public boolean isBStatsEnabled() {
        return config.getBoolean("performance.metrics.bstats", true);
    }
    
    /**
     * Get the raw configuration object
     */
    public FileConfiguration getConfig() {
        return config;
    }
}