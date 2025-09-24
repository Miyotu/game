package com.miyotu.cs2plugin.commands;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.enums.Team;
import com.miyotu.cs2plugin.gui.LobbyGUI;
import com.miyotu.cs2plugin.gui.TeamGUI;
import com.miyotu.cs2plugin.models.CS2Player;
import com.miyotu.cs2plugin.models.Lobby;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CS2Command implements CommandExecutor {
    private final CS2Plugin plugin;
    
    public CS2Command(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "createlobby":
                return handleCreateLobby(player, args);
            case "setlobby":
                return handleSetLobby(player, args);
            case "join":
                return handleJoin(player, args);
            case "leave":
                return handleLeave(player);
            case "team":
                return handleTeam(player, args);
            case "list":
                return handleList(player);
            default:
                sendHelp(player);
                return true;
        }
    }
    
    private boolean handleCreateLobby(Player player, String[] args) {
        if (!player.hasPermission("cs2.createlobby")) {
            sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cs2 createlobby <name>");
            return true;
        }
        
        String lobbyName = args[1];
        
        if (plugin.getLobbyManager().createLobby(lobbyName, player.getLocation())) {
            sendMessage(player, "lobby-created", "{lobby}", lobbyName);
        } else {
            player.sendMessage("§cFailed to create lobby! Lobby might already exist.");
        }
        
        return true;
    }
    
    private boolean handleSetLobby(Player player, String[] args) {
        if (!player.hasPermission("cs2.setlobby")) {
            sendMessage(player, "no-permission");
            return true;
        }
        
        if (args.length < 2) {
            player.sendMessage("§cUsage: /cs2 setlobby <name>");
            return true;
        }
        
        String lobbyName = args[1];
        
        if (plugin.getLobbyManager().setLobbySpawn(lobbyName, player.getLocation())) {
            sendMessage(player, "lobby-set", "{lobby}", lobbyName);
        } else {
            player.sendMessage("§cFailed to set lobby spawn! Lobby might not exist.");
        }
        
        return true;
    }
    
    private boolean handleJoin(Player player, String[] args) {
        if (!player.hasPermission("cs2.join")) {
            sendMessage(player, "no-permission");
            return true;
        }
        
        CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player == null) {
            player.sendMessage("§cPlayer data not loaded! Please rejoin.");
            return true;
        }
        
        if (cs2Player.isInLobby()) {
            sendMessage(player, "already-in-lobby");
            return true;
        }
        
        if (args.length >= 2) {
            // Join specific lobby
            String lobbyName = args[1];
            Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
            
            if (lobby == null) {
                player.sendMessage("§cLobby not found!");
                return true;
            }
            
            if (lobby.isFull()) {
                sendMessage(player, "lobby-full");
                return true;
            }
            
            if (lobby.addPlayer(cs2Player)) {
                sendMessage(player, "joined-lobby", "{lobby}", lobbyName);
                
                // Teleport to lobby spawn
                if (lobby.getSpawnLocation() != null) {
                    player.teleport(lobby.getSpawnLocation());
                }
            } else {
                player.sendMessage("§cFailed to join lobby!");
            }
        } else {
            // Open lobby selection GUI
            new LobbyGUI(plugin).open(player);
        }
        
        return true;
    }
    
    private boolean handleLeave(Player player) {
        if (!player.hasPermission("cs2.leave")) {
            sendMessage(player, "no-permission");
            return true;
        }
        
        CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player == null || !cs2Player.isInLobby()) {
            sendMessage(player, "not-in-lobby");
            return true;
        }
        
        Lobby lobby = cs2Player.getCurrentLobby();
        lobby.removePlayer(player.getUniqueId());
        sendMessage(player, "left-lobby");
        
        return true;
    }
    
    private boolean handleTeam(Player player, String[] args) {
        if (!player.hasPermission("cs2.team")) {
            sendMessage(player, "no-permission");
            return true;
        }
        
        CS2Player cs2Player = plugin.getPlayerDataManager().getPlayer(player);
        if (cs2Player == null || !cs2Player.isInLobby()) {
            sendMessage(player, "not-in-lobby");
            return true;
        }
        
        if (args.length >= 2) {
            String teamStr = args[1];
            Team team = Team.fromString(teamStr);
            
            if (team == null) {
                sendMessage(player, "invalid-team");
                return true;
            }
            
            Lobby lobby = cs2Player.getCurrentLobby();
            if (lobby.assignPlayerToTeam(player.getUniqueId(), team)) {
                sendMessage(player, "team-selected", "{team}", team.getDisplayName());
            } else {
                player.sendMessage("§cTeam is full!");
            }
        } else {
            // Open team selection GUI
            new TeamGUI(plugin).open(player);
        }
        
        return true;
    }
    
    private boolean handleList(Player player) {
        var lobbies = plugin.getLobbyManager().getAllLobbies();
        
        if (lobbies.isEmpty()) {
            player.sendMessage("§eNo lobbies available.");
            return true;
        }
        
        player.sendMessage("§6=== CS2 Lobbies ===");
        for (Lobby lobby : lobbies.values()) {
            String status = lobby.isGameStarted() ? "§cIn Game" : "§aWaiting";
            player.sendMessage(String.format("§e%s §7- §f%d/%d §7- %s", 
                lobby.getName(), 
                lobby.getCurrentPlayerCount(), 
                lobby.getMaxPlayers(),
                status));
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§6=== CS2 Commands ===");
        player.sendMessage("§e/cs2 join §7- Open lobby selection or join game");
        player.sendMessage("§e/cs2 leave §7- Leave current lobby");
        player.sendMessage("§e/cs2 team §7- Open team selection");
        player.sendMessage("§e/cs2 list §7- List all lobbies");
        
        if (player.hasPermission("cs2.admin")) {
            player.sendMessage("§c=== Admin Commands ===");
            player.sendMessage("§e/cs2 createlobby <name> §7- Create a new lobby");
            player.sendMessage("§e/cs2 setlobby <name> §7- Set lobby spawn point");
        }
    }
    
    private void sendMessage(Player player, String key, String... replacements) {
        String message = plugin.getConfig().getString("messages." + key, key);
        String prefix = plugin.getConfig().getString("messages.prefix", "&8[&6CS2&8]&r ");
        
        // Apply replacements
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(replacements[i], replacements[i + 1]);
            }
        }
        
        player.sendMessage((prefix + message).replace("&", "§"));
    }
}