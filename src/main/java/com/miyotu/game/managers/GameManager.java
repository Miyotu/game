package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;

/**
 * Manager for mini games
 */
public class GameManager {
    
    private final MiyotuGamePlugin plugin;
    
    public GameManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Stop all running games
     */
    public void stopAllGames() {
        plugin.getLogger().info("Stopped all games");
    }
    
    /**
     * Reload game manager
     */
    public void reload() {
        plugin.getLogger().info("GameManager reloaded");
    }
}