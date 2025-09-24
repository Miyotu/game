package com.miyotu.game.commands;

import com.miyotu.game.MiyotuGamePlugin;
import com.miyotu.game.models.GamePlayer;
import com.miyotu.game.utils.LanguageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Balance command implementation
 */
public class BalanceCommand implements CommandExecutor {
    
    private final MiyotuGamePlugin plugin;
    
    public BalanceCommand(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length == 0) {
            // Show own balance
            showBalance(player, player);
        } else if (args.length == 1) {
            // Show another player's balance
            Player targetPlayer = Bukkit.getPlayer(args[0]);
            if (targetPlayer == null) {
                plugin.getLanguageManager().sendMessage(player, "general.player-not-found",
                    LanguageManager.placeholders("player", args[0]));
                return true;
            }
            showBalance(player, targetPlayer);
        } else {
            plugin.getLanguageManager().sendMessage(player, "general.command-usage",
                LanguageManager.placeholders("usage", "/balance [player]"));
            return true;
        }
        
        return true;
    }
    
    /**
     * Show balance to a player
     * 
     * @param viewer Player viewing the balance
     * @param target Player whose balance to show
     */
    private void showBalance(Player viewer, Player target) {
        GamePlayer gamePlayer = plugin.getPlayerManager().getOnlinePlayer(target);
        
        if (gamePlayer != null) {
            double balance = gamePlayer.getBalance();
            String formattedBalance = plugin.getEconomyManager().formatCurrency(balance);
            
            if (viewer.equals(target)) {
                // Own balance
                plugin.getLanguageManager().sendMessage(viewer, "economy.balance.self",
                    LanguageManager.placeholders(
                        "balance", String.valueOf(balance),
                        "currency", plugin.getEconomyManager().getCurrencyNamePlural()
                    ));
            } else {
                // Other player's balance
                plugin.getLanguageManager().sendMessage(viewer, "economy.balance.other",
                    LanguageManager.placeholders(
                        "player", target.getName(),
                        "balance", String.valueOf(balance),
                        "currency", plugin.getEconomyManager().getCurrencyNamePlural()
                    ));
            }
        } else {
            // Player not found in cache, try loading from database
            plugin.getPlayerManager().getPlayerByName(target.getName()).thenAccept(gamePlayerFromDb -> {
                if (gamePlayerFromDb != null) {
                    double balance = gamePlayerFromDb.getBalance();
                    
                    if (viewer.equals(target)) {
                        plugin.getLanguageManager().sendMessage(viewer, "economy.balance.self",
                            LanguageManager.placeholders(
                                "balance", String.valueOf(balance),
                                "currency", plugin.getEconomyManager().getCurrencyNamePlural()
                            ));
                    } else {
                        plugin.getLanguageManager().sendMessage(viewer, "economy.balance.other",
                            LanguageManager.placeholders(
                                "player", target.getName(),
                                "balance", String.valueOf(balance),
                                "currency", plugin.getEconomyManager().getCurrencyNamePlural()
                            ));
                    }
                } else {
                    plugin.getLanguageManager().sendMessage(viewer, "general.player-not-found",
                        LanguageManager.placeholders("player", target.getName()));
                }
            });
        }
    }
}