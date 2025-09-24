package com.miyotu.cs2plugin.managers;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.models.CS2Player;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    private final CS2Plugin plugin;
    private final Map<UUID, CS2Player> players;
    
    public PlayerDataManager(CS2Plugin plugin) {
        this.plugin = plugin;
        this.players = new ConcurrentHashMap<>();
    }
    
    public CS2Player getPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    public CS2Player getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
    
    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        CS2Player cs2Player = loadPlayerFromDatabase(uuid, player.getName());
        if (cs2Player != null) {
            cs2Player.setBukkitPlayer(player);
            players.put(uuid, cs2Player);
        }
    }
    
    public void unloadPlayer(UUID uuid) {
        CS2Player player = players.remove(uuid);
        if (player != null) {
            savePlayerToDatabase(player);
        }
    }
    
    private CS2Player loadPlayerFromDatabase(UUID uuid, String name) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM cs2_players WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Player exists, load data
                        return new CS2Player(uuid, name);
                    } else {
                        // New player, create entry
                        return createNewPlayer(uuid, name);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player " + name + ": " + e.getMessage());
            return null;
        }
    }
    
    private CS2Player createNewPlayer(UUID uuid, String name) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO cs2_players (uuid, name) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, name);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create new player " + name + ": " + e.getMessage());
            return null;
        }
        
        return new CS2Player(uuid, name);
    }
    
    private void savePlayerToDatabase(CS2Player player) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE cs2_players SET name = ? WHERE uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, player.getName());
                statement.setString(2, player.getUuid().toString());
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player " + player.getName() + ": " + e.getMessage());
        }
    }
    
    public Map<UUID, CS2Player> getAllPlayers() {
        return players;
    }
}