package com.miyotu.cs2plugin.database;

import com.miyotu.cs2plugin.CS2Plugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {
    private final CS2Plugin plugin;
    private HikariDataSource dataSource;
    
    public DatabaseManager(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean initialize() {
        try {
            setupDataSource();
            createTables();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            return false;
        }
    }
    
    private void setupDataSource() {
        FileConfiguration config = plugin.getConfig();
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + 
            config.getString("database.host", "localhost") + ":" +
            config.getInt("database.port", 3306) + "/" +
            config.getString("database.database", "cs2_plugin") + 
            "?useSSL=false&allowPublicKeyRetrieval=true");
        hikariConfig.setUsername(config.getString("database.username", "minecraft"));
        hikariConfig.setPassword(config.getString("database.password", "password"));
        hikariConfig.setMaximumPoolSize(config.getInt("database.pool-size", 10));
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(hikariConfig);
    }
    
    private void createTables() throws SQLException {
        try (Connection connection = getConnection()) {
            // Players table
            executeStatement(connection, 
                "CREATE TABLE IF NOT EXISTS cs2_players (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "name VARCHAR(16) NOT NULL," +
                "kills INT DEFAULT 0," +
                "deaths INT DEFAULT 0," +
                "wins INT DEFAULT 0," +
                "losses INT DEFAULT 0," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")");
            
            // Lobbies table
            executeStatement(connection,
                "CREATE TABLE IF NOT EXISTS cs2_lobbies (" +
                "name VARCHAR(32) PRIMARY KEY," +
                "world VARCHAR(32)," +
                "x DOUBLE," +
                "y DOUBLE," +
                "z DOUBLE," +
                "yaw FLOAT," +
                "pitch FLOAT," +
                "max_players INT DEFAULT 10," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")");
            
            // Games table
            executeStatement(connection,
                "CREATE TABLE IF NOT EXISTS cs2_games (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "lobby_name VARCHAR(32)," +
                "started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "ended_at TIMESTAMP NULL," +
                "winner_team VARCHAR(5)," +
                "ct_score INT DEFAULT 0," +
                "t_score INT DEFAULT 0," +
                "FOREIGN KEY (lobby_name) REFERENCES cs2_lobbies(name)" +
                ")");
            
            // Teams table (for game participation)
            executeStatement(connection,
                "CREATE TABLE IF NOT EXISTS cs2_game_teams (" +
                "game_id INT," +
                "player_uuid VARCHAR(36)," +
                "team VARCHAR(5)," +
                "kills INT DEFAULT 0," +
                "deaths INT DEFAULT 0," +
                "PRIMARY KEY (game_id, player_uuid)," +
                "FOREIGN KEY (game_id) REFERENCES cs2_games(id)," +
                "FOREIGN KEY (player_uuid) REFERENCES cs2_players(uuid)" +
                ")");
        }
    }
    
    private void executeStatement(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }
    
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized");
        }
        return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}