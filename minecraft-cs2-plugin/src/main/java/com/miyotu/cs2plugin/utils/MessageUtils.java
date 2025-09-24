package com.miyotu.cs2plugin.utils;

import com.miyotu.cs2plugin.CS2Plugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtils {
    private final CS2Plugin plugin;
    
    public MessageUtils(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void sendMessage(Player player, String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, key);
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6CS2&8]&r ");
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        player.sendMessage(colorize(prefix + message));
    }
    
    public void broadcast(String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, key);
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6CS2&8]&r ");
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        plugin.getServer().broadcastMessage(colorize(prefix + message));
    }
    
    public String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getMessage(String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, key);
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        return colorize(message);
    }
}