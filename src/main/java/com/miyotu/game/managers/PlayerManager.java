package com.miyotu.game.managers;

import com.miyotu.game.MiyotuGamePlugin;
import com.miyotu.game.models.GamePlayer;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Manager for handling player data and operations
 */
public class PlayerManager {
    
    private final MiyotuGamePlugin plugin;
    private final Map<UUID, GamePlayer> onlinePlayers;
    private final Map<UUID, GamePlayer> cachedPlayers;
    
    public PlayerManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
        this.onlinePlayers = new HashMap<>();
        this.cachedPlayers = new HashMap<>();
    }
    
    /**
     * Load a player from database or create new if not exists
     * 
     * @param player Bukkit player
     * @return CompletableFuture with GamePlayer
     */
    public CompletableFuture<GamePlayer> loadPlayer(Player player) {
        return loadPlayer(player.getUniqueId(), player.getName()).thenApply(gamePlayer -> {
            gamePlayer.setBukkitPlayer(player);
            gamePlayer.setOnline(true);
            onlinePlayers.put(player.getUniqueId(), gamePlayer);
            return gamePlayer;
        });
    }
    
    /**
     * Load a player by UUID and username
     * 
     * @param uuid Player UUID
     * @param username Player username
     * @return CompletableFuture with GamePlayer
     */
    public CompletableFuture<GamePlayer> loadPlayer(UUID uuid, String username) {
        // Check cache first
        if (cachedPlayers.containsKey(uuid)) {
            GamePlayer cached = cachedPlayers.get(uuid);
            cached.setUsername(username); // Update username in case it changed
            return CompletableFuture.completedFuture(cached);
        }
        
        return plugin.getDatabaseManager().executeQueryAsync(
            "SELECT * FROM mg_players WHERE uuid = ?", uuid.toString()
        ).thenApply(resultSet -> {
            try {
                if (resultSet.next()) {
                    // Player exists, load data
                    GamePlayer gamePlayer = createGamePlayerFromResultSet(resultSet);
                    gamePlayer.setUsername(username); // Update username
                    cachedPlayers.put(uuid, gamePlayer);
                    return gamePlayer;
                } else {
                    // New player, create with defaults
                    GamePlayer gamePlayer = new GamePlayer(uuid, username);
                    cachedPlayers.put(uuid, gamePlayer);
                    
                    // Save to database
                    savePlayerAsync(gamePlayer);
                    
                    return gamePlayer;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
                // Return new player as fallback
                return new GamePlayer(uuid, username);
            }
        });
    }
    
    /**
     * Create GamePlayer from database ResultSet
     * 
     * @param resultSet Database result
     * @return GamePlayer instance
     * @throws SQLException If database error occurs
     */
    private GamePlayer createGamePlayerFromResultSet(ResultSet resultSet) throws SQLException {
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        String username = resultSet.getString("username");
        
        GamePlayer gamePlayer = new GamePlayer(uuid, username);
        gamePlayer.setBalance(resultSet.getDouble("balance"));
        gamePlayer.setRank(resultSet.getString("rank"));
        gamePlayer.setGodMode(resultSet.getBoolean("god_mode"));
        gamePlayer.setFlyMode(resultSet.getBoolean("fly_mode"));
        gamePlayer.setLastDaily(resultSet.getLong("last_daily"));
        gamePlayer.setDailyStreak(resultSet.getInt("daily_streak"));
        gamePlayer.setPlaytime(resultSet.getLong("playtime"));
        gamePlayer.setFirstJoin(resultSet.getLong("first_join"));
        gamePlayer.setLastSeen(resultSet.getLong("last_seen"));
        gamePlayer.setLanguage(resultSet.getString("language"));
        
        return gamePlayer;
    }
    
    /**
     * Save a player to database asynchronously
     * 
     * @param gamePlayer Player to save
     * @return CompletableFuture for completion
     */
    public CompletableFuture<Void> savePlayerAsync(GamePlayer gamePlayer) {
        String sql = """
            INSERT INTO mg_players 
            (uuid, username, balance, rank, god_mode, fly_mode, last_daily, daily_streak, 
             playtime, first_join, last_seen, language, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE
            username = VALUES(username),
            balance = VALUES(balance),
            rank = VALUES(rank),
            god_mode = VALUES(god_mode),
            fly_mode = VALUES(fly_mode),
            last_daily = VALUES(last_daily),
            daily_streak = VALUES(daily_streak),
            playtime = VALUES(playtime),
            last_seen = VALUES(last_seen),
            language = VALUES(language),
            updated_at = CURRENT_TIMESTAMP
            """;
        
        return plugin.getDatabaseManager().executeUpdateAsync(sql,
            gamePlayer.getUuid().toString(),
            gamePlayer.getUsername(),
            gamePlayer.getBalance(),
            gamePlayer.getRank(),
            gamePlayer.isGodMode(),
            gamePlayer.isFlyMode(),
            gamePlayer.getLastDaily(),
            gamePlayer.getDailyStreak(),
            gamePlayer.getTotalPlaytime(),
            gamePlayer.getFirstJoin(),
            gamePlayer.getLastSeen(),
            gamePlayer.getLanguage()
        ).thenRun(() -> {
            // Update cache
            cachedPlayers.put(gamePlayer.getUuid(), gamePlayer);
        }).exceptionally(throwable -> {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + gamePlayer.getUsername(), throwable);
            return null;
        });
    }
    
    /**
     * Save a player to database synchronously
     * 
     * @param gamePlayer Player to save
     */
    public void savePlayer(GamePlayer gamePlayer) {
        try {
            String sql = """
                INSERT INTO mg_players 
                (uuid, username, balance, rank, god_mode, fly_mode, last_daily, daily_streak, 
                 playtime, first_join, last_seen, language, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                username = VALUES(username),
                balance = VALUES(balance),
                rank = VALUES(rank),
                god_mode = VALUES(god_mode),
                fly_mode = VALUES(fly_mode),
                last_daily = VALUES(last_daily),
                daily_streak = VALUES(daily_streak),
                playtime = VALUES(playtime),
                last_seen = VALUES(last_seen),
                language = VALUES(language),
                updated_at = CURRENT_TIMESTAMP
                """;
            
            plugin.getDatabaseManager().executeUpdate(sql,
                gamePlayer.getUuid().toString(),
                gamePlayer.getUsername(),
                gamePlayer.getBalance(),
                gamePlayer.getRank(),
                gamePlayer.isGodMode(),
                gamePlayer.isFlyMode(),
                gamePlayer.getLastDaily(),
                gamePlayer.getDailyStreak(),
                gamePlayer.getTotalPlaytime(),
                gamePlayer.getFirstJoin(),
                gamePlayer.getLastSeen(),
                gamePlayer.getLanguage()
            );
            
            // Update cache
            cachedPlayers.put(gamePlayer.getUuid(), gamePlayer);
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player data for " + gamePlayer.getUsername(), e);
        }
    }
    
    /**
     * Unload a player when they disconnect
     * 
     * @param player Bukkit player
     */
    public void unloadPlayer(Player player) {
        GamePlayer gamePlayer = onlinePlayers.remove(player.getUniqueId());
        if (gamePlayer != null) {
            gamePlayer.setOnline(false);
            gamePlayer.setBukkitPlayer(null);
            
            // Save player data
            savePlayerAsync(gamePlayer);
        }
    }
    
    /**
     * Get an online player
     * 
     * @param uuid Player UUID
     * @return GamePlayer or null if not online
     */
    public GamePlayer getOnlinePlayer(UUID uuid) {
        return onlinePlayers.get(uuid);
    }
    
    /**
     * Get an online player by Bukkit player
     * 
     * @param player Bukkit player
     * @return GamePlayer or null if not found
     */
    public GamePlayer getOnlinePlayer(Player player) {
        return getOnlinePlayer(player.getUniqueId());
    }
    
    /**
     * Get a player (online or from cache/database)
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with GamePlayer
     */
    public CompletableFuture<GamePlayer> getPlayer(UUID uuid) {
        // Check if online first
        if (onlinePlayers.containsKey(uuid)) {
            return CompletableFuture.completedFuture(onlinePlayers.get(uuid));
        }
        
        // Check cache
        if (cachedPlayers.containsKey(uuid)) {
            return CompletableFuture.completedFuture(cachedPlayers.get(uuid));
        }
        
        // Load from database
        return plugin.getDatabaseManager().executeQueryAsync(
            "SELECT * FROM mg_players WHERE uuid = ?", uuid.toString()
        ).thenApply(resultSet -> {
            try {
                if (resultSet.next()) {
                    GamePlayer gamePlayer = createGamePlayerFromResultSet(resultSet);
                    cachedPlayers.put(uuid, gamePlayer);
                    return gamePlayer;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + uuid, e);
            }
            return null;
        });
    }
    
    /**
     * Get a player by username
     * 
     * @param username Player username
     * @return CompletableFuture with GamePlayer
     */
    public CompletableFuture<GamePlayer> getPlayerByName(String username) {
        // Check online players first
        for (GamePlayer gamePlayer : onlinePlayers.values()) {
            if (gamePlayer.getUsername().equalsIgnoreCase(username)) {
                return CompletableFuture.completedFuture(gamePlayer);
            }
        }
        
        // Check cache
        for (GamePlayer gamePlayer : cachedPlayers.values()) {
            if (gamePlayer.getUsername().equalsIgnoreCase(username)) {
                return CompletableFuture.completedFuture(gamePlayer);
            }
        }
        
        // Load from database
        return plugin.getDatabaseManager().executeQueryAsync(
            "SELECT * FROM mg_players WHERE username = ?", username
        ).thenApply(resultSet -> {
            try {
                if (resultSet.next()) {
                    GamePlayer gamePlayer = createGamePlayerFromResultSet(resultSet);
                    cachedPlayers.put(gamePlayer.getUuid(), gamePlayer);
                    return gamePlayer;
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load player data for " + username, e);
            }
            return null;
        });
    }
    
    /**
     * Check if a player exists in database
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with boolean result
     */
    public CompletableFuture<Boolean> playerExists(UUID uuid) {
        return plugin.getDatabaseManager().executeQueryAsync(
            "SELECT 1 FROM mg_players WHERE uuid = ? LIMIT 1", uuid.toString()
        ).thenApply(resultSet -> {
            try {
                return resultSet.next();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to check if player exists: " + uuid, e);
                return false;
            }
        });
    }
    
    /**
     * Save all online players
     */
    public void saveAllPlayers() {
        for (GamePlayer gamePlayer : onlinePlayers.values()) {
            savePlayer(gamePlayer);
        }
        plugin.getLogger().info("Saved " + onlinePlayers.size() + " online players");
    }
    
    /**
     * Get all online players
     * 
     * @return Map of online players
     */
    public Map<UUID, GamePlayer> getOnlinePlayers() {
        return new HashMap<>(onlinePlayers);
    }
    
    /**
     * Clear player cache (useful for memory management)
     */
    public void clearCache() {
        cachedPlayers.clear();
        plugin.getLogger().info("Player cache cleared");
    }
    
    /**
     * Reload player manager
     */
    public void reload() {
        // Save all current data
        saveAllPlayers();
        
        // Clear cache to force reload from database
        clearCache();
        
        plugin.getLogger().info("PlayerManager reloaded");
    }
}