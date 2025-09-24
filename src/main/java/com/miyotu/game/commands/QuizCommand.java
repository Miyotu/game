package com.miyotu.game.commands;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class QuizCommand implements CommandExecutor {
    private final MiyotuGamePlugin plugin;
    
    public QuizCommand(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Command QuizCommand is not yet implemented!");
        return true;
    }
}
