package com.miyotu.cs2plugin.managers;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.models.Lobby;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LobbyManager {
    private final CS2Plugin plugin;
    private final Map<String, Lobby> lobbies;
    
    public LobbyManager(CS2Plugin plugin) {
        this.plugin = plugin;
        this.lobbies = new ConcurrentHashMap<>();
        loadLobbiesFromDatabase();
    }
    
    public boolean createLobby(String name, Location location) {
        if (lobbies.containsKey(name)) {
            return false;
        }
        
        int maxPlayers = plugin.getConfig().getInt("lobby.max-players", 10);
        Lobby lobby = new Lobby(name, maxPlayers);
        lobby.setSpawnLocation(location);
        
        if (saveLobbyToDatabase(lobby)) {
            lobbies.put(name, lobby);
            return true;
        }
        
        return false;
    }
    
    public boolean deleteLobby(String name) {
        Lobby lobby = lobbies.remove(name);
        if (lobby != null) {
            return deleteLobbyFromDatabase(name);
        }
        return false;
    }
    
    public Lobby getLobby(String name) {
        return lobbies.get(name);
    }
    
    public Map<String, Lobby> getAllLobbies() {
        return new HashMap<>(lobbies);
    }
    
    public boolean setLobbySpawn(String name, Location location) {
        Lobby lobby = lobbies.get(name);
        if (lobby != null) {
            lobby.setSpawnLocation(location);
            return updateLobbyInDatabase(lobby);
        }
        return false;
    }
    
    private void loadLobbiesFromDatabase() {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "SELECT * FROM cs2_lobbies";
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    String name = resultSet.getString("name");
                    String world = resultSet.getString("world");
                    double x = resultSet.getDouble("x");
                    double y = resultSet.getDouble("y");
                    double z = resultSet.getDouble("z");
                    float yaw = resultSet.getFloat("yaw");
                    float pitch = resultSet.getFloat("pitch");
                    int maxPlayers = resultSet.getInt("max_players");
                    
                    Lobby lobby = new Lobby(name, maxPlayers);
                    
                    if (world != null && plugin.getServer().getWorld(world) != null) {
                        Location location = new Location(
                            plugin.getServer().getWorld(world),
                            x, y, z, yaw, pitch
                        );
                        lobby.setSpawnLocation(location);
                    }
                    
                    lobbies.put(name, lobby);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load lobbies from database: " + e.getMessage());
        }
    }
    
    private boolean saveLobbyToDatabase(Lobby lobby) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "INSERT INTO cs2_lobbies (name, world, x, y, z, yaw, pitch, max_players) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, lobby.getName());
                
                Location loc = lobby.getSpawnLocation();
                if (loc != null) {
                    statement.setString(2, loc.getWorld().getName());
                    statement.setDouble(3, loc.getX());
                    statement.setDouble(4, loc.getY());
                    statement.setDouble(5, loc.getZ());
                    statement.setFloat(6, loc.getYaw());
                    statement.setFloat(7, loc.getPitch());
                } else {
                    statement.setString(2, null);
                    statement.setDouble(3, 0);
                    statement.setDouble(4, 0);
                    statement.setDouble(5, 0);
                    statement.setFloat(6, 0);
                    statement.setFloat(7, 0);
                }
                
                statement.setInt(8, lobby.getMaxPlayers());
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save lobby to database: " + e.getMessage());
            return false;
        }
    }
    
    private boolean updateLobbyInDatabase(Lobby lobby) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "UPDATE cs2_lobbies SET world = ?, x = ?, y = ?, z = ?, yaw = ?, pitch = ? WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                Location loc = lobby.getSpawnLocation();
                if (loc != null) {
                    statement.setString(1, loc.getWorld().getName());
                    statement.setDouble(2, loc.getX());
                    statement.setDouble(3, loc.getY());
                    statement.setDouble(4, loc.getZ());
                    statement.setFloat(5, loc.getYaw());
                    statement.setFloat(6, loc.getPitch());
                } else {
                    statement.setString(1, null);
                    statement.setDouble(2, 0);
                    statement.setDouble(3, 0);
                    statement.setDouble(4, 0);
                    statement.setFloat(5, 0);
                    statement.setFloat(6, 0);
                }
                statement.setString(7, lobby.getName());
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update lobby in database: " + e.getMessage());
            return false;
        }
    }
    
    private boolean deleteLobbyFromDatabase(String name) {
        try (Connection connection = plugin.getDatabaseManager().getConnection()) {
            String sql = "DELETE FROM cs2_lobbies WHERE name = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete lobby from database: " + e.getMessage());
            return false;
        }
    }
}