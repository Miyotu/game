package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;

/**
 * Manager for rank system
 */
public class RankManager {
    
    private final MiyotuGamePlugin plugin;
    
    public RankManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Reload rank manager
     */
    public void reload() {
        plugin.getLogger().info("RankManager reloaded");
    }
}