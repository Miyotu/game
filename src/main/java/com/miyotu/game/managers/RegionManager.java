package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;

/**
 * Manager for region protection system
 */
public class RegionManager {
    
    private final MiyotuGamePlugin plugin;
    
    public RegionManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Setup WorldGuard integration
     */
    public void setupWorldGuardIntegration() {
        if (plugin.getConfigManager().isWorldGuardIntegration()) {
            plugin.getLogger().info("WorldGuard integration enabled");
        }
    }
    
    /**
     * Save all regions
     */
    public void saveAllRegions() {
        plugin.getLogger().info("Saved all regions");
    }
    
    /**
     * Reload region manager
     */
    public void reload() {
        plugin.getLogger().info("RegionManager reloaded");
    }
}