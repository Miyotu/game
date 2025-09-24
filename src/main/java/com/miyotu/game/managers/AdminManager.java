package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.entity.Player;

import java.util.logging.Level;

/**
 * Manager for admin tools and logging
 */
public class AdminManager {
    
    private final MiyotuGamePlugin plugin;
    
    public AdminManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Log an admin action
     * 
     * @param player Player performing the action
     * @param action Action type
     * @param details Action details
     */
    public void logAction(Player player, String action, String details) {
        if (!plugin.getConfigManager().isLoggingEnabled()) {
            return;
        }
        
        try {
            // Log to console
            plugin.getLogger().info("[ADMIN] " + player.getName() + " performed " + action + ": " + details);
            
            // Log to database if enabled
            if (plugin.getConfigManager().isLogToDatabase()) {
                String sql = """
                    INSERT INTO mg_logs (event_type, player_uuid, player_name, action, details, 
                                       world, x, y, z, created_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                    """;
                
                plugin.getDatabaseManager().executeUpdateAsync(sql,
                    "ADMIN_ACTION",
                    player.getUniqueId().toString(),
                    player.getName(),
                    action,
                    details,
                    player.getWorld().getName(),
                    player.getLocation().getX(),
                    player.getLocation().getY(),
                    player.getLocation().getZ()
                );
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to log admin action", e);
        }
    }
    
    /**
     * Reload admin manager
     */
    public void reload() {
        plugin.getLogger().info("AdminManager reloaded");
    }
}