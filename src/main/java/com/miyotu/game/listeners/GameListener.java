package com.miyotu.game.listeners;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.event.Listener;

public class GameListener implements Listener {
    private final MiyotuGamePlugin plugin;
    
    public GameListener(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
}
