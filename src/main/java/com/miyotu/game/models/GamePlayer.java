package com.miyotu.game.models;

import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Represents a player in the game system
 */
public class GamePlayer {
    
    private final UUID uuid;
    private String username;
    private double balance;
    private String rank;
    private boolean godMode;
    private boolean flyMode;
    private long lastDaily;
    private int dailyStreak;
    private long playtime;
    private long firstJoin;
    private long lastSeen;
    private String language;
    private boolean isOnline;
    
    // Transient data (not stored in database)
    private Player bukkitPlayer;
    private boolean inGodMode;
    private boolean inFlyMode;
    private long sessionStartTime;
    
    public GamePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.balance = 1000.0; // Default starting balance
        this.rank = "player"; // Default rank
        this.godMode = false;
        this.flyMode = false;
        this.lastDaily = 0;
        this.dailyStreak = 0;
        this.playtime = 0;
        this.firstJoin = System.currentTimeMillis();
        this.lastSeen = System.currentTimeMillis();
        this.language = "tr"; // Default language
        this.isOnline = false;
        this.sessionStartTime = System.currentTimeMillis();
    }
    
    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = Math.max(0, balance);
    }
    
    public void addBalance(double amount) {
        this.balance += amount;
    }
    
    public boolean removeBalance(double amount) {
        if (this.balance >= amount) {
            this.balance -= amount;
            return true;
        }
        return false;
    }
    
    public String getRank() {
        return rank;
    }
    
    public void setRank(String rank) {
        this.rank = rank;
    }
    
    public boolean isGodMode() {
        return godMode;
    }
    
    public void setGodMode(boolean godMode) {
        this.godMode = godMode;
        this.inGodMode = godMode;
    }
    
    public boolean isFlyMode() {
        return flyMode;
    }
    
    public void setFlyMode(boolean flyMode) {
        this.flyMode = flyMode;
        this.inFlyMode = flyMode;
    }
    
    public long getLastDaily() {
        return lastDaily;
    }
    
    public void setLastDaily(long lastDaily) {
        this.lastDaily = lastDaily;
    }
    
    public int getDailyStreak() {
        return dailyStreak;
    }
    
    public void setDailyStreak(int dailyStreak) {
        this.dailyStreak = dailyStreak;
    }
    
    public long getPlaytime() {
        return playtime;
    }
    
    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }
    
    public void addPlaytime(long time) {
        this.playtime += time;
    }
    
    public long getFirstJoin() {
        return firstJoin;
    }
    
    public void setFirstJoin(long firstJoin) {
        this.firstJoin = firstJoin;
    }
    
    public long getLastSeen() {
        return lastSeen;
    }
    
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public boolean isOnline() {
        return isOnline;
    }
    
    public void setOnline(boolean online) {
        this.isOnline = online;
        if (online) {
            this.sessionStartTime = System.currentTimeMillis();
        } else {
            // Add session time to total playtime
            addPlaytime(System.currentTimeMillis() - sessionStartTime);
            setLastSeen(System.currentTimeMillis());
        }
    }
    
    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }
    
    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }
    
    public boolean isInGodMode() {
        return inGodMode;
    }
    
    public void setInGodMode(boolean inGodMode) {
        this.inGodMode = inGodMode;
    }
    
    public boolean isInFlyMode() {
        return inFlyMode;
    }
    
    public void setInFlyMode(boolean inFlyMode) {
        this.inFlyMode = inFlyMode;
    }
    
    public long getSessionStartTime() {
        return sessionStartTime;
    }
    
    /**
     * Check if the player can claim daily reward
     * 
     * @return True if can claim
     */
    public boolean canClaimDaily() {
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000; // 24 hours
        return (currentTime - lastDaily) >= dayInMillis;
    }
    
    /**
     * Claim daily reward and update streak
     * 
     * @return True if claimed successfully
     */
    public boolean claimDaily() {
        if (!canClaimDaily()) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        long dayInMillis = 24 * 60 * 60 * 1000; // 24 hours
        long twoDaysInMillis = 48 * 60 * 60 * 1000; // 48 hours
        
        // Check if streak should continue
        if ((currentTime - lastDaily) <= twoDaysInMillis) {
            // Continue streak
            dailyStreak++;
        } else {
            // Reset streak
            dailyStreak = 1;
        }
        
        lastDaily = currentTime;
        return true;
    }
    
    /**
     * Get current session playtime in milliseconds
     * 
     * @return Session playtime
     */
    public long getCurrentSessionTime() {
        if (isOnline) {
            return System.currentTimeMillis() - sessionStartTime;
        }
        return 0;
    }
    
    /**
     * Get total playtime including current session
     * 
     * @return Total playtime in milliseconds
     */
    public long getTotalPlaytime() {
        return playtime + getCurrentSessionTime();
    }
    
    @Override
    public String toString() {
        return "GamePlayer{" +
                "uuid=" + uuid +
                ", username='" + username + '\'' +
                ", balance=" + balance +
                ", rank='" + rank + '\'' +
                ", isOnline=" + isOnline +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePlayer that = (GamePlayer) o;
        return uuid.equals(that.uuid);
    }
    
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}