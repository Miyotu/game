package com.miyotu.game.commands;

import com.miyotu.game.MiyotuGamePlugin;
import com.miyotu.game.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Heal command implementation
 */
public class HealCommand implements CommandExecutor {
    
    private final MiyotuGamePlugin plugin;
    
    public HealCommand(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        // Check permission
        if (!plugin.getPermissionManager().checkPermissionWithMessage(player, "miyotugame.heal")) {
            return true;
        }
        
        Player targetPlayer;
        
        if (args.length == 0) {
            // Heal self
            targetPlayer = player;
        } else if (args.length == 1) {
            // Heal specified player
            targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                plugin.getLanguageManager().sendMessage(player, "general.player-not-found", 
                    LanguageManager.placeholders("player", args[0]));
                return true;
            }
        } else {
            plugin.getLanguageManager().sendMessage(player, "general.command-usage",
                LanguageManager.placeholders("usage", "/heal [player]"));
            return true;
        }
        
        // Perform heal
        healPlayer(targetPlayer);
        
        // Send messages
        if (targetPlayer.equals(player)) {
            // Self heal
            plugin.getLanguageManager().sendMessage(player, "player.heal.self");
        } else {
            // Heal other
            plugin.getLanguageManager().sendMessage(player, "player.heal.other",
                LanguageManager.placeholders("player", targetPlayer.getName()));
            plugin.getLanguageManager().sendMessage(targetPlayer, "player.heal.received",
                LanguageManager.placeholders("player", player.getName()));
        }
        
        return true;
    }
    
    /**
     * Heal a player
     * 
     * @param player Player to heal
     */
    private void healPlayer(Player player) {
        // Restore health
        player.setHealth(player.getMaxHealth());
        
        // Restore food if configured
        if (plugin.getConfigManager().isRestoreFood()) {
            player.setFoodLevel(20);
            player.setSaturation(20.0f);
        }
        
        // Clear negative effects
        player.getActivePotionEffects().forEach(effect -> {
            switch (effect.getType().getName().toLowerCase()) {
                case "poison", "wither", "hunger", "weakness", "slowness", "mining_fatigue", 
                     "nausea", "blindness", "bad_omen", "unluck" -> player.removePotionEffect(effect.getType());
            }
        });
        
        // Log the action
        plugin.getAdminManager().logAction(player, "HEAL", "Player healed: " + player.getName());
    }
}