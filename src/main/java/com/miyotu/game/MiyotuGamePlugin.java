package com.miyotu.game;

import com.miyotu.game.commands.*;
import com.miyotu.game.database.DatabaseManager;
import com.miyotu.game.economy.EconomyManager;
import com.miyotu.game.listeners.*;
import com.miyotu.game.managers.*;
import com.miyotu.game.utils.ConfigManager;
import com.miyotu.game.utils.LanguageManager;
import com.miyotu.game.utils.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for MiyotuGame
 * Comprehensive Minecraft 1.20.1 Bukkit/Spigot Plugin
 * 
 * @author Miyotu
 * @version 1.0.0
 */
public class MiyotuGamePlugin extends JavaPlugin {
    
    private static MiyotuGamePlugin instance;
    
    // Managers
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private DatabaseManager databaseManager;
    private EconomyManager economyManager;
    private PermissionManager permissionManager;
    private PlayerManager playerManager;
    private RegionManager regionManager;
    private GameManager gameManager;
    private SocialManager socialManager;
    private AdminManager adminManager;
    private RankManager rankManager;
    private StatisticsManager statisticsManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize plugin
        getLogger().info("Enabling MiyotuGame Plugin v" + getDescription().getVersion());
        getLogger().info("Designed for Minecraft 1.20.1 Bukkit/Spigot");
        
        try {
            // Initialize configuration
            initializeConfig();
            
            // Initialize language system
            initializeLanguage();
            
            // Initialize database
            initializeDatabase();
            
            // Initialize managers
            initializeManagers();
            
            // Register commands
            registerCommands();
            
            // Register listeners
            registerListeners();
            
            // Setup integrations
            setupIntegrations();
            
            // Start auto-save task
            startAutoSave();
            
            getLogger().info("MiyotuGame Plugin has been enabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to enable MiyotuGame Plugin!", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        getLogger().info("Disabling MiyotuGame Plugin...");
        
        try {
            // Save all data
            if (playerManager != null) {
                playerManager.saveAllPlayers();
            }
            
            if (regionManager != null) {
                regionManager.saveAllRegions();
            }
            
            if (gameManager != null) {
                gameManager.stopAllGames();
            }
            
            // Close database connections
            if (databaseManager != null) {
                databaseManager.close();
            }
            
            getLogger().info("MiyotuGame Plugin has been disabled successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error while disabling plugin!", e);
        }
        
        instance = null;
    }
    
    /**
     * Initialize configuration manager
     */
    private void initializeConfig() {
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        getLogger().info("Configuration loaded successfully!");
    }
    
    /**
     * Initialize language manager
     */
    private void initializeLanguage() {
        languageManager = new LanguageManager(this);
        languageManager.loadLanguages();
        
        getLogger().info("Language system initialized!");
    }
    
    /**
     * Initialize database connection
     */
    private void initializeDatabase() {
        databaseManager = new DatabaseManager(this);
        
        if (!databaseManager.connect()) {
            throw new RuntimeException("Failed to connect to database!");
        }
        
        // Create tables
        databaseManager.createTables();
        
        getLogger().info("Database connection established!");
    }
    
    /**
     * Initialize all managers
     */
    private void initializeManagers() {
        // Core managers
        economyManager = new EconomyManager(this);
        permissionManager = new PermissionManager(this);
        playerManager = new PlayerManager(this);
        
        // Feature managers
        regionManager = new RegionManager(this);
        gameManager = new GameManager(this);
        socialManager = new SocialManager(this);
        adminManager = new AdminManager(this);
        rankManager = new RankManager(this);
        statisticsManager = new StatisticsManager(this);
        
        getLogger().info("All managers initialized!");
    }
    
    /**
     * Register all commands
     */
    private void registerCommands() {
        // Player management commands
        getCommand("heal").setExecutor(new HealCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("god").setExecutor(new GodCommand(this));
        getCommand("tp").setExecutor(new TeleportCommand(this));
        
        // Economy commands
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("daily").setExecutor(new DailyCommand(this));
        
        // Social commands
        getCommand("msg").setExecutor(new MessageCommand(this));
        getCommand("reply").setExecutor(new ReplyCommand(this));
        getCommand("friend").setExecutor(new FriendCommand(this));
        
        // Mini game commands
        getCommand("parkour").setExecutor(new ParkourCommand(this));
        getCommand("quiz").setExecutor(new QuizCommand(this));
        getCommand("treasure").setExecutor(new TreasureCommand(this));
        
        // Admin commands
        getCommand("gameadmin").setExecutor(new GameAdminCommand(this));
        getCommand("ban").setExecutor(new BanCommand(this));
        getCommand("kick").setExecutor(new KickCommand(this));
        
        // Region commands
        getCommand("region").setExecutor(new RegionCommand(this));
        
        // Stats and ranks
        getCommand("stats").setExecutor(new StatsCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        
        getLogger().info("Commands registered successfully!");
    }
    
    /**
     * Register all event listeners
     */
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        
        // Core listeners
        pm.registerEvents(new PlayerListener(this), this);
        pm.registerEvents(new ChatListener(this), this);
        pm.registerEvents(new ProtectionListener(this), this);
        pm.registerEvents(new EconomyListener(this), this);
        pm.registerEvents(new GameListener(this), this);
        pm.registerEvents(new AdminListener(this), this);
        
        getLogger().info("Event listeners registered successfully!");
    }
    
    /**
     * Setup external plugin integrations
     */
    private void setupIntegrations() {
        // Vault integration
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            economyManager.setupVaultIntegration();
            getLogger().info("Vault integration enabled!");
        }
        
        // PlaceholderAPI integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            // Register placeholders
            getLogger().info("PlaceholderAPI integration enabled!");
        }
        
        // WorldGuard integration
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            regionManager.setupWorldGuardIntegration();
            getLogger().info("WorldGuard integration enabled!");
        }
    }
    
    /**
     * Start auto-save task
     */
    private void startAutoSave() {
        int interval = configManager.getAutoSaveInterval() * 20 * 60; // Convert to ticks
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                playerManager.saveAllPlayers();
                regionManager.saveAllRegions();
                getLogger().info("Auto-save completed successfully!");
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Auto-save failed!", e);
            }
        }, interval, interval);
        
        getLogger().info("Auto-save task started (interval: " + configManager.getAutoSaveInterval() + " minutes)");
    }
    
    /**
     * Reload the plugin configuration and data
     */
    public void reloadPlugin() {
        try {
            // Reload configuration
            configManager.loadConfig();
            languageManager.loadLanguages();
            
            // Reload managers
            economyManager.reload();
            playerManager.reload();
            regionManager.reload();
            gameManager.reload();
            socialManager.reload();
            adminManager.reload();
            rankManager.reload();
            statisticsManager.reload();
            
            getLogger().info("Plugin reloaded successfully!");
            
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to reload plugin!", e);
            throw new RuntimeException("Reload failed", e);
        }
    }
    
    // Getters for managers
    public static MiyotuGamePlugin getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public LanguageManager getLanguageManager() {
        return languageManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public PlayerManager getPlayerManager() {
        return playerManager;
    }
    
    public RegionManager getRegionManager() {
        return regionManager;
    }
    
    public GameManager getGameManager() {
        return gameManager;
    }
    
    public SocialManager getSocialManager() {
        return socialManager;
    }
    
    public AdminManager getAdminManager() {
        return adminManager;
    }
    
    public RankManager getRankManager() {
        return rankManager;
    }
    
    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }
}