package com.miyotu.game.utils;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * Permission manager for handling plugin permissions
 */
public class PermissionManager {
    
    private final MiyotuGamePlugin plugin;
    
    // Permission constants
    public static final String ADMIN_ALL = "miyotugame.admin.*";
    public static final String PLAYER_ALL = "miyotugame.player.*";
    
    // Player management permissions
    public static final String HEAL = "miyotugame.heal";
    public static final String FLY = "miyotugame.fly";
    public static final String GOD = "miyotugame.god";
    public static final String TELEPORT = "miyotugame.tp";
    
    // Economy permissions
    public static final String BALANCE = "miyotugame.balance";
    public static final String PAY = "miyotugame.pay";
    public static final String SHOP = "miyotugame.shop";
    public static final String DAILY = "miyotugame.daily";
    
    // Social permissions
    public static final String MESSAGE = "miyotugame.msg";
    public static final String FRIEND = "miyotugame.friend";
    
    // Mini game permissions
    public static final String PARKOUR = "miyotugame.parkour";
    public static final String QUIZ = "miyotugame.quiz";
    public static final String TREASURE = "miyotugame.treasure";
    
    // Admin permissions
    public static final String ADMIN = "miyotugame.admin";
    public static final String BAN = "miyotugame.ban";
    public static final String KICK = "miyotugame.kick";
    public static final String REGION = "miyotugame.region";
    public static final String RANK = "miyotugame.rank";
    public static final String RELOAD = "miyotugame.reload";
    public static final String STATS = "miyotugame.stats";
    
    public PermissionManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
        registerPermissions();
    }
    
    /**
     * Register all plugin permissions
     */
    private void registerPermissions() {
        // Register parent permissions
        registerPermission(ADMIN_ALL, "All admin permissions", PermissionDefault.OP);
        registerPermission(PLAYER_ALL, "All player permissions", PermissionDefault.TRUE);
        
        // Register individual permissions
        registerPermission(HEAL, "Heal players", PermissionDefault.OP);
        registerPermission(FLY, "Toggle flight", PermissionDefault.OP);
        registerPermission(GOD, "Toggle god mode", PermissionDefault.OP);
        registerPermission(TELEPORT, "Teleport to players", PermissionDefault.OP);
        
        registerPermission(BALANCE, "Check balance", PermissionDefault.TRUE);
        registerPermission(PAY, "Pay other players", PermissionDefault.TRUE);
        registerPermission(SHOP, "Use shop", PermissionDefault.TRUE);
        registerPermission(DAILY, "Claim daily rewards", PermissionDefault.TRUE);
        
        registerPermission(MESSAGE, "Send private messages", PermissionDefault.TRUE);
        registerPermission(FRIEND, "Manage friends", PermissionDefault.TRUE);
        
        registerPermission(PARKOUR, "Play parkour", PermissionDefault.TRUE);
        registerPermission(QUIZ, "Play quiz", PermissionDefault.TRUE);
        registerPermission(TREASURE, "Play treasure hunt", PermissionDefault.TRUE);
        
        registerPermission(ADMIN, "Admin commands", PermissionDefault.OP);
        registerPermission(BAN, "Ban players", PermissionDefault.OP);
        registerPermission(KICK, "Kick players", PermissionDefault.OP);
        registerPermission(REGION, "Manage regions", PermissionDefault.OP);
        registerPermission(RANK, "Manage ranks", PermissionDefault.OP);
        registerPermission(RELOAD, "Reload plugin", PermissionDefault.OP);
        registerPermission(STATS, "View statistics", PermissionDefault.TRUE);
    }
    
    /**
     * Register a permission with the server
     * 
     * @param permission Permission node
     * @param description Permission description
     * @param defaultValue Default permission value
     */
    private void registerPermission(String permission, String description, PermissionDefault defaultValue) {
        try {
            Permission perm = new Permission(permission, description, defaultValue);
            plugin.getServer().getPluginManager().addPermission(perm);
        } catch (IllegalArgumentException e) {
            // Permission already exists, ignore
        }
    }
    
    /**
     * Check if a player has a specific permission
     * 
     * @param player Player to check
     * @param permission Permission to check
     * @return True if player has permission
     */
    public boolean hasPermission(Player player, String permission) {
        if (player == null) return false;
        return player.hasPermission(permission);
    }
    
    /**
     * Check if a player has admin permissions
     * 
     * @param player Player to check
     * @return True if player is admin
     */
    public boolean isAdmin(Player player) {
        return hasPermission(player, ADMIN_ALL) || hasPermission(player, ADMIN);
    }
    
    /**
     * Check if a player can heal others
     * 
     * @param player Player to check
     * @return True if can heal
     */
    public boolean canHeal(Player player) {
        return hasPermission(player, HEAL);
    }
    
    /**
     * Check if a player can use fly
     * 
     * @param player Player to check
     * @return True if can fly
     */
    public boolean canFly(Player player) {
        return hasPermission(player, FLY);
    }
    
    /**
     * Check if a player can use god mode
     * 
     * @param player Player to check
     * @return True if can use god mode
     */
    public boolean canGod(Player player) {
        return hasPermission(player, GOD);
    }
    
    /**
     * Check if a player can teleport
     * 
     * @param player Player to check
     * @return True if can teleport
     */
    public boolean canTeleport(Player player) {
        return hasPermission(player, TELEPORT);
    }
    
    /**
     * Check if a player can ban others
     * 
     * @param player Player to check
     * @return True if can ban
     */
    public boolean canBan(Player player) {
        return hasPermission(player, BAN);
    }
    
    /**
     * Check if a player can kick others
     * 
     * @param player Player to check
     * @return True if can kick
     */
    public boolean canKick(Player player) {
        return hasPermission(player, KICK);
    }
    
    /**
     * Check if a player can manage regions
     * 
     * @param player Player to check
     * @return True if can manage regions
     */
    public boolean canManageRegions(Player player) {
        return hasPermission(player, REGION);
    }
    
    /**
     * Check if a player can manage ranks
     * 
     * @param player Player to check
     * @return True if can manage ranks
     */
    public boolean canManageRanks(Player player) {
        return hasPermission(player, RANK);
    }
    
    /**
     * Check permission and send no permission message if denied
     * 
     * @param player Player to check
     * @param permission Permission to check
     * @return True if has permission
     */
    public boolean checkPermissionWithMessage(Player player, String permission) {
        if (hasPermission(player, permission)) {
            return true;
        }
        
        plugin.getLanguageManager().sendMessage(player, "general.no-permission");
        return false;
    }
    
    /**
     * Get permission level for a player (0=player, 1=mod, 2=admin)
     * 
     * @param player Player to check
     * @return Permission level
     */
    public int getPermissionLevel(Player player) {
        if (hasPermission(player, ADMIN_ALL)) {
            return 2; // Admin
        } else if (hasPermission(player, KICK) || hasPermission(player, REGION)) {
            return 1; // Moderator
        } else {
            return 0; // Player
        }
    }
}