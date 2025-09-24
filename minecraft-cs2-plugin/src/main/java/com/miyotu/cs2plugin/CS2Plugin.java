package com.miyotu.cs2plugin;

import com.miyotu.cs2plugin.commands.CS2Command;
import com.miyotu.cs2plugin.database.DatabaseManager;
import com.miyotu.cs2plugin.listeners.PlayerListener;
import com.miyotu.cs2plugin.managers.LobbyManager;
import com.miyotu.cs2plugin.managers.PlayerDataManager;
import com.miyotu.cs2plugin.managers.NPCManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CS2Plugin extends JavaPlugin {
    
    private static CS2Plugin instance;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private LobbyManager lobbyManager;
    private NPCManager npcManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize database
        initializeDatabase();
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        getLogger().info("CS2Plugin has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Cleanup
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        if (npcManager != null) {
            npcManager.cleanup();
        }
        
        getLogger().info("CS2Plugin has been disabled!");
    }
    
    private void initializeDatabase() {
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }
    
    private void initializeManagers() {
        playerDataManager = new PlayerDataManager(this);
        lobbyManager = new LobbyManager(this);
        npcManager = new NPCManager(this);
    }
    
    private void registerCommands() {
        getCommand("cs2").setExecutor(new CS2Command(this));
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    // Getters
    public static CS2Plugin getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }
    
    public NPCManager getNPCManager() {
        return npcManager;
    }
}