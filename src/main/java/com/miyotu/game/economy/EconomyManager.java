package com.miyotu.game.economy;

import com.miyotu.game.MiyotuGamePlugin;
import com.miyotu.game.models.GamePlayer;
import com.miyotu.game.utils.LanguageManager;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Economy manager for handling currency and transactions
 */
public class EconomyManager {
    
    private final MiyotuGamePlugin plugin;
    private final String currencySymbol;
    private final String currencyNameSingular;
    private final String currencyNamePlural;
    private final double maxBalance;
    
    public EconomyManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
        this.currencySymbol = plugin.getConfigManager().getCurrencySymbol();
        this.currencyNameSingular = plugin.getConfigManager().getCurrencyNameSingular();
        this.currencyNamePlural = plugin.getConfigManager().getCurrencyNamePlural();
        this.maxBalance = plugin.getConfigManager().getMaxBalance();
    }
    
    /**
     * Get a player's balance
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with balance
     */
    public CompletableFuture<Double> getBalance(UUID uuid) {
        return plugin.getPlayerManager().getPlayer(uuid).thenApply(gamePlayer -> {
            if (gamePlayer != null) {
                return gamePlayer.getBalance();
            }
            return 0.0;
        });
    }
    
    /**
     * Get a player's balance synchronously
     * 
     * @param player Bukkit player
     * @return Balance amount
     */
    public double getBalance(Player player) {
        GamePlayer gamePlayer = plugin.getPlayerManager().getOnlinePlayer(player);
        if (gamePlayer != null) {
            return gamePlayer.getBalance();
        }
        return 0.0;
    }
    
    /**
     * Set a player's balance
     * 
     * @param uuid Player UUID
     * @param amount New balance amount
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> setBalance(UUID uuid, double amount) {
        amount = Math.max(0, Math.min(amount, maxBalance));
        final double finalAmount = amount;
        
        return plugin.getPlayerManager().getPlayer(uuid).thenCompose(gamePlayer -> {
            if (gamePlayer != null) {
                double oldBalance = gamePlayer.getBalance();
                gamePlayer.setBalance(finalAmount);
                
                // Log transaction
                logTransaction(null, uuid, finalAmount - oldBalance, "BALANCE_SET", "Balance set by admin");
                
                return plugin.getPlayerManager().savePlayerAsync(gamePlayer).thenApply(v -> true);
            }
            return CompletableFuture.completedFuture(false);
        });
    }
    
    /**
     * Add money to a player's balance
     * 
     * @param uuid Player UUID
     * @param amount Amount to add
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> addBalance(UUID uuid, double amount) {
        if (amount <= 0) return CompletableFuture.completedFuture(false);
        
        return plugin.getPlayerManager().getPlayer(uuid).thenCompose(gamePlayer -> {
            if (gamePlayer != null) {
                double newBalance = Math.min(gamePlayer.getBalance() + amount, maxBalance);
                double actualAmount = newBalance - gamePlayer.getBalance();
                
                if (actualAmount > 0) {
                    gamePlayer.setBalance(newBalance);
                    
                    // Log transaction
                    logTransaction(null, uuid, actualAmount, "DEPOSIT", "Money added");
                    
                    return plugin.getPlayerManager().savePlayerAsync(gamePlayer).thenApply(v -> true);
                }
            }
            return CompletableFuture.completedFuture(false);
        });
    }
    
    /**
     * Remove money from a player's balance
     * 
     * @param uuid Player UUID
     * @param amount Amount to remove
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> removeBalance(UUID uuid, double amount) {
        if (amount <= 0) return CompletableFuture.completedFuture(false);
        
        return plugin.getPlayerManager().getPlayer(uuid).thenCompose(gamePlayer -> {
            if (gamePlayer != null && gamePlayer.getBalance() >= amount) {
                gamePlayer.removeBalance(amount);
                
                // Log transaction
                logTransaction(uuid, null, amount, "WITHDRAWAL", "Money removed");
                
                return plugin.getPlayerManager().savePlayerAsync(gamePlayer).thenApply(v -> true);
            }
            return CompletableFuture.completedFuture(false);
        });
    }
    
    /**
     * Transfer money between players
     * 
     * @param fromUuid Sender UUID
     * @param toUuid Receiver UUID
     * @param amount Amount to transfer
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> transferMoney(UUID fromUuid, UUID toUuid, double amount) {
        if (amount <= 0 || fromUuid.equals(toUuid)) {
            return CompletableFuture.completedFuture(false);
        }
        
        return plugin.getPlayerManager().getPlayer(fromUuid).thenCompose(fromPlayer -> {
            if (fromPlayer == null || fromPlayer.getBalance() < amount) {
                return CompletableFuture.completedFuture(false);
            }
            
            return plugin.getPlayerManager().getPlayer(toUuid).thenCompose(toPlayer -> {
                if (toPlayer == null) {
                    return CompletableFuture.completedFuture(false);
                }
                
                // Check if receiver would exceed max balance
                double newToBalance = Math.min(toPlayer.getBalance() + amount, maxBalance);
                double actualAmount = newToBalance - toPlayer.getBalance();
                
                if (actualAmount < amount) {
                    // Receiver would exceed max balance
                    return CompletableFuture.completedFuture(false);
                }
                
                // Perform transfer
                fromPlayer.removeBalance(amount);
                toPlayer.addBalance(amount);
                
                // Log transaction
                logTransaction(fromUuid, toUuid, amount, "TRANSFER", "Player to player transfer");
                
                // Save both players
                CompletableFuture<Void> save1 = plugin.getPlayerManager().savePlayerAsync(fromPlayer);
                CompletableFuture<Void> save2 = plugin.getPlayerManager().savePlayerAsync(toPlayer);
                
                return CompletableFuture.allOf(save1, save2).thenApply(v -> true);
            });
        });
    }
    
    /**
     * Process daily reward for a player
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with reward info (amount, streak)
     */
    public CompletableFuture<DailyReward> processDailyReward(UUID uuid) {
        if (!plugin.getConfigManager().isDailyRewardEnabled()) {
            return CompletableFuture.completedFuture(null);
        }
        
        return plugin.getPlayerManager().getPlayer(uuid).thenCompose(gamePlayer -> {
            if (gamePlayer == null || !gamePlayer.canClaimDaily()) {
                return CompletableFuture.completedFuture(null);
            }
            
            // Claim daily and update streak
            gamePlayer.claimDaily();
            
            // Calculate reward
            double baseReward = plugin.getConfigManager().getBaseDailyReward();
            double bonus = 0;
            
            if (plugin.getConfigManager().isStreakBonusEnabled()) {
                int streak = gamePlayer.getDailyStreak();
                int maxStreak = plugin.getConfigManager().getMaxStreak();
                double bonusPerDay = plugin.getConfigManager().getBonusPerDay();
                
                bonus = Math.min(streak - 1, maxStreak - 1) * bonusPerDay;
            }
            
            double totalReward = baseReward + bonus;
            gamePlayer.addBalance(totalReward);
            
            // Log transaction
            logTransaction(null, uuid, totalReward, "DAILY_REWARD", 
                "Daily reward (streak: " + gamePlayer.getDailyStreak() + ")");
            
            DailyReward reward = new DailyReward(totalReward, bonus, gamePlayer.getDailyStreak());
            
            return plugin.getPlayerManager().savePlayerAsync(gamePlayer).thenApply(v -> reward);
        });
    }
    
    /**
     * Check if a player can claim daily reward
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with boolean result
     */
    public CompletableFuture<Boolean> canClaimDaily(UUID uuid) {
        return plugin.getPlayerManager().getPlayer(uuid).thenApply(gamePlayer -> {
            return gamePlayer != null && gamePlayer.canClaimDaily();
        });
    }
    
    /**
     * Get time until next daily reward
     * 
     * @param uuid Player UUID
     * @return CompletableFuture with milliseconds until next claim
     */
    public CompletableFuture<Long> getTimeUntilDaily(UUID uuid) {
        return plugin.getPlayerManager().getPlayer(uuid).thenApply(gamePlayer -> {
            if (gamePlayer == null) return 0L;
            
            long dayInMillis = 24 * 60 * 60 * 1000;
            long timeSinceLastDaily = System.currentTimeMillis() - gamePlayer.getLastDaily();
            
            if (timeSinceLastDaily >= dayInMillis) {
                return 0L; // Can claim now
            }
            
            return dayInMillis - timeSinceLastDaily;
        });
    }
    
    /**
     * Log a transaction to the database
     * 
     * @param fromUuid Sender UUID (null for system)
     * @param toUuid Receiver UUID (null for system)
     * @param amount Transaction amount
     * @param type Transaction type
     * @param description Transaction description
     */
    private void logTransaction(UUID fromUuid, UUID toUuid, double amount, String type, String description) {
        if (!plugin.getConfigManager().isLogToDatabase()) return;
        
        try {
            String sql = """
                INSERT INTO mg_transactions (from_uuid, to_uuid, amount, transaction_type, description, created_at)
                VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
            
            plugin.getDatabaseManager().executeUpdateAsync(sql,
                fromUuid != null ? fromUuid.toString() : null,
                toUuid != null ? toUuid.toString() : null,
                amount,
                type,
                description
            );
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to log transaction", e);
        }
    }
    
    /**
     * Format currency amount for display
     * 
     * @param amount Amount to format
     * @return Formatted currency string
     */
    public String formatCurrency(double amount) {
        if (amount == 1.0) {
            return String.format("%.2f %s", amount, currencyNameSingular);
        } else {
            return String.format("%.2f %s", amount, currencyNamePlural);
        }
    }
    
    /**
     * Format currency amount with symbol
     * 
     * @param amount Amount to format
     * @return Formatted currency string with symbol
     */
    public String formatCurrencyWithSymbol(double amount) {
        return String.format("%s%.2f", currencySymbol, amount);
    }
    
    /**
     * Get currency symbol
     * 
     * @return Currency symbol
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }
    
    /**
     * Get currency name (singular)
     * 
     * @return Currency name singular
     */
    public String getCurrencyNameSingular() {
        return currencyNameSingular;
    }
    
    /**
     * Get currency name (plural)
     * 
     * @return Currency name plural
     */
    public String getCurrencyNamePlural() {
        return currencyNamePlural;
    }
    
    /**
     * Get maximum balance
     * 
     * @return Maximum balance
     */
    public double getMaxBalance() {
        return maxBalance;
    }
    
    /**
     * Setup Vault integration if available
     */
    public void setupVaultIntegration() {
        // TODO: Implement Vault economy provider
        plugin.getLogger().info("Vault integration would be set up here");
    }
    
    /**
     * Reload economy manager
     */
    public void reload() {
        plugin.getLogger().info("EconomyManager reloaded");
    }
    
    /**
     * Daily reward information
     */
    public static class DailyReward {
        private final double totalAmount;
        private final double bonusAmount;
        private final int streak;
        
        public DailyReward(double totalAmount, double bonusAmount, int streak) {
            this.totalAmount = totalAmount;
            this.bonusAmount = bonusAmount;
            this.streak = streak;
        }
        
        public double getTotalAmount() {
            return totalAmount;
        }
        
        public double getBonusAmount() {
            return bonusAmount;
        }
        
        public int getStreak() {
            return streak;
        }
        
        public double getBaseAmount() {
            return totalAmount - bonusAmount;
        }
    }
}