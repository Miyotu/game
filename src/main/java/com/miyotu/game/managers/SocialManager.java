package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;

/**
 * Manager for social features (friends, messages, chat)
 */
public class SocialManager {
    
    private final MiyotuGamePlugin plugin;
    
    public SocialManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Reload social manager
     */
    public void reload() {
        plugin.getLogger().info("SocialManager reloaded");
    }
}