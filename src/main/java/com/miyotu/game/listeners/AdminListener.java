package com.miyotu.game.listeners;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.event.Listener;

public class AdminListener implements Listener {
    private final MiyotuGamePlugin plugin;
    
    public AdminListener(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
}
