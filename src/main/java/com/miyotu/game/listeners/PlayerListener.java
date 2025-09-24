package com.miyotu.game.listeners;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.event.Listener;

public class PlayerListener implements Listener {
    private final MiyotuGamePlugin plugin;
    
    public PlayerListener(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
}
