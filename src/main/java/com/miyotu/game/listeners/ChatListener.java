package com.miyotu.game.listeners;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {
    private final MiyotuGamePlugin plugin;
    
    public ChatListener(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
}
