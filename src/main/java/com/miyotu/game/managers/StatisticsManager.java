package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;

/**
 * Manager for player statistics
 */
public class StatisticsManager {
    
    private final MiyotuGamePlugin plugin;
    
    public StatisticsManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Reload statistics manager
     */
    public void reload() {
        plugin.getLogger().info("StatisticsManager reloaded");
    }
}