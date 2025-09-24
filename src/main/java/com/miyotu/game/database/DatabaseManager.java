package com.miyotu.game.database;

import com.miyotu.game.MiyotuGamePlugin;
import com.miyotu.game.utils.ConfigManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

/**
 * Database manager for handling MySQL connections and operations
 */
public class DatabaseManager {
    
    private final MiyotuGamePlugin plugin;
    private final ConfigManager configManager;
    private HikariDataSource dataSource;
    private ExecutorService executor;
    
    public DatabaseManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.executor = Executors.newFixedThreadPool(5);
    }
    
    /**
     * Connect to the database
     * 
     * @return True if connection successful
     */
    public boolean connect() {
        try {
            String dbType = configManager.getDatabaseType();
            
            if ("mysql".equalsIgnoreCase(dbType)) {
                return connectMySQL();
            } else {
                plugin.getLogger().severe("Unsupported database type: " + dbType);
                return false;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to connect to database!", e);
            return false;
        }
    }
    
    /**
     * Setup MySQL connection with HikariCP
     * 
     * @return True if connection successful
     */
    private boolean connectMySQL() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Database connection settings
            String host = configManager.getMySQLHost();
            int port = configManager.getMySQLPort();
            String database = configManager.getMySQLDatabase();
            String username = configManager.getMySQLUsername();
            String password = configManager.getMySQLPassword();
            
            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + 
                           "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=utf8";
            
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Connection pool settings
            config.setMaximumPoolSize(configManager.getMaxPoolSize());
            config.setMinimumIdle(configManager.getMinIdleConnections());
            config.setConnectionTimeout(configManager.getConnectionTimeout());
            config.setIdleTimeout(configManager.getIdleTimeout());
            config.setMaxLifetime(configManager.getMaxLifetime());
            
            // Pool name
            config.setPoolName("MiyotuGame-Pool");
            
            // Additional settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            
            // Test connection
            try (Connection connection = dataSource.getConnection()) {
                plugin.getLogger().info("Successfully connected to MySQL database!");
                return true;
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to setup MySQL connection!", e);
            return false;
        }
    }
    
    /**
     * Get a connection from the pool
     * 
     * @return Database connection
     * @throws SQLException If connection fails
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not connected!");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Execute a query asynchronously
     * 
     * @param sql SQL query
     * @param params Parameters for the query
     * @return CompletableFuture with ResultSet
     */
    public CompletableFuture<ResultSet> executeQueryAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                
                return statement.executeQuery();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to execute query: " + sql, e);
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Execute an update asynchronously
     * 
     * @param sql SQL update statement
     * @param params Parameters for the statement
     * @return CompletableFuture with affected rows count
     */
    public CompletableFuture<Integer> executeUpdateAsync(String sql, Object... params) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                
                // Set parameters
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                
                return statement.executeUpdate();
                
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to execute update: " + sql, e);
                throw new RuntimeException(e);
            }
        }, executor);
    }
    
    /**
     * Execute a query synchronously
     * 
     * @param sql SQL query
     * @param params Parameters for the query
     * @return ResultSet
     */
    public ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        
        // Set parameters
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        
        return statement.executeQuery();
    }
    
    /**
     * Execute an update synchronously
     * 
     * @param sql SQL update statement
     * @param params Parameters for the statement
     * @return Number of affected rows
     */
    public int executeUpdate(String sql, Object... params) throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            // Set parameters
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            return statement.executeUpdate();
        }
    }
    
    /**
     * Create all necessary database tables
     */
    public void createTables() {
        try {
            // Players table
            String playersTable = """
                CREATE TABLE IF NOT EXISTS mg_players (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    balance DECIMAL(15,2) DEFAULT 1000.00,
                    rank VARCHAR(32) DEFAULT 'player',
                    god_mode BOOLEAN DEFAULT FALSE,
                    fly_mode BOOLEAN DEFAULT FALSE,
                    last_daily BIGINT DEFAULT 0,
                    daily_streak INT DEFAULT 0,
                    playtime BIGINT DEFAULT 0,
                    first_join BIGINT DEFAULT 0,
                    last_seen BIGINT DEFAULT 0,
                    language VARCHAR(5) DEFAULT 'tr',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_username (username),
                    INDEX idx_rank (rank)
                )
                """;
            
            // Friends table
            String friendsTable = """
                CREATE TABLE IF NOT EXISTS mg_friends (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    friend_uuid VARCHAR(36) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
                    FOREIGN KEY (friend_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
                    UNIQUE KEY unique_friendship (player_uuid, friend_uuid),
                    INDEX idx_player (player_uuid),
                    INDEX idx_friend (friend_uuid)
                )
                """;
            
            // Regions table
            String regionsTable = """
                CREATE TABLE IF NOT EXISTS mg_regions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(64) NOT NULL UNIQUE,
                    world VARCHAR(64) NOT NULL,
                    min_x INT NOT NULL,
                    min_y INT NOT NULL,
                    min_z INT NOT NULL,
                    max_x INT NOT NULL,
                    max_y INT NOT NULL,
                    max_z INT NOT NULL,
                    owner_uuid VARCHAR(36),
                    pvp BOOLEAN DEFAULT FALSE,
                    build BOOLEAN DEFAULT FALSE,
                    interact BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (owner_uuid) REFERENCES mg_players(uuid) ON DELETE SET NULL,
                    INDEX idx_name (name),
                    INDEX idx_world (world),
                    INDEX idx_owner (owner_uuid)
                )
                """;
            
            // Parkour courses table
            String parkourTable = """
                CREATE TABLE IF NOT EXISTS mg_parkour (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(64) NOT NULL UNIQUE,
                    world VARCHAR(64) NOT NULL,
                    start_x DOUBLE NOT NULL,
                    start_y DOUBLE NOT NULL,
                    start_z DOUBLE NOT NULL,
                    end_x DOUBLE NOT NULL,
                    end_y DOUBLE NOT NULL,
                    end_z DOUBLE NOT NULL,
                    reward DECIMAL(15,2) DEFAULT 1000.00,
                    difficulty VARCHAR(16) DEFAULT 'MEDIUM',
                    created_by VARCHAR(36),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (created_by) REFERENCES mg_players(uuid) ON DELETE SET NULL,
                    INDEX idx_name (name),
                    INDEX idx_world (world)
                )
                """;
            
            // Parkour records table
            String parkourRecordsTable = """
                CREATE TABLE IF NOT EXISTS mg_parkour_records (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    parkour_id INT NOT NULL,
                    player_uuid VARCHAR(36) NOT NULL,
                    completion_time BIGINT NOT NULL,
                    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (parkour_id) REFERENCES mg_parkour(id) ON DELETE CASCADE,
                    FOREIGN KEY (player_uuid) REFERENCES mg_players(uuid) ON DELETE CASCADE,
                    UNIQUE KEY unique_record (parkour_id, player_uuid),
                    INDEX idx_parkour (parkour_id),
                    INDEX idx_player (player_uuid),
                    INDEX idx_time (completion_time)
                )
                """;
            
            // Bans table
            String bansTable = """
                CREATE TABLE IF NOT EXISTS mg_bans (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    player_uuid VARCHAR(36) NOT NULL,
                    player_name VARCHAR(16) NOT NULL,
                    banned_by VARCHAR(36),
                    reason TEXT,
                    banned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NULL,
                    active BOOLEAN DEFAULT TRUE,
                    FOREIGN KEY (banned_by) REFERENCES mg_players(uuid) ON DELETE SET NULL,
                    INDEX idx_player (player_uuid),
                    INDEX idx_active (active),
                    INDEX idx_expires (expires_at)
                )
                """;
            
            // Logs table
            String logsTable = """
                CREATE TABLE IF NOT EXISTS mg_logs (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    event_type VARCHAR(32) NOT NULL,
                    player_uuid VARCHAR(36),
                    player_name VARCHAR(16),
                    action VARCHAR(255) NOT NULL,
                    details TEXT,
                    world VARCHAR(64),
                    x DOUBLE,
                    y DOUBLE,
                    z DOUBLE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_event_type (event_type),
                    INDEX idx_player (player_uuid),
                    INDEX idx_created_at (created_at)
                )
                """;
            
            // Economy transactions table
            String transactionsTable = """
                CREATE TABLE IF NOT EXISTS mg_transactions (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    from_uuid VARCHAR(36),
                    to_uuid VARCHAR(36),
                    amount DECIMAL(15,2) NOT NULL,
                    transaction_type VARCHAR(32) NOT NULL,
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_from (from_uuid),
                    INDEX idx_to (to_uuid),
                    INDEX idx_type (transaction_type),
                    INDEX idx_created_at (created_at)
                )
                """;
            
            // Execute table creation
            executeUpdate(playersTable);
            executeUpdate(friendsTable);
            executeUpdate(regionsTable);
            executeUpdate(parkourTable);
            executeUpdate(parkourRecordsTable);
            executeUpdate(bansTable);
            executeUpdate(logsTable);
            executeUpdate(transactionsTable);
            
            plugin.getLogger().info("Database tables created successfully!");
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to create database tables!", e);
        }
    }
    
    /**
     * Close the database connection
     */
    public void close() {
        try {
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
            }
            
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                plugin.getLogger().info("Database connection closed successfully!");
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error while closing database connection!", e);
        }
    }
    
    /**
     * Check if the database is connected
     * 
     * @return True if connected
     */
    public boolean isConnected() {
        if (dataSource == null) return false;
        
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}