package com.miyotu.game.commands;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCommand implements CommandExecutor {
    private final MiyotuGamePlugin plugin;
    
    public FlyCommand(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (!plugin.getPermissionManager().checkPermissionWithMessage(player, "miyotugame.fly")) {
            return true;
        }
        
        // Toggle fly mode
        boolean newFlyMode = !player.getAllowFlight();
        player.setAllowFlight(newFlyMode);
        player.setFlying(newFlyMode);
        
        String messageKey = newFlyMode ? "player.fly.enabled-self" : "player.fly.disabled-self";
        plugin.getLanguageManager().sendMessage(player, messageKey);
        
        return true;
    }
}
