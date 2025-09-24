package com.miyotu.game.utils;

import com.miyotu.game.MiyotuGamePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Language manager for multi-language support
 * Handles loading and accessing language files
 */
public class LanguageManager {
    
    private final MiyotuGamePlugin plugin;
    private final Map<String, FileConfiguration> languages;
    private String defaultLanguage;
    
    public LanguageManager(MiyotuGamePlugin plugin) {
        this.plugin = plugin;
        this.languages = new HashMap<>();
    }
    
    /**
     * Load all language files
     */
    public void loadLanguages() {
        // Get default language from config
        defaultLanguage = plugin.getConfigManager().getLanguage();
        
        // Load Turkish language
        loadLanguage("tr");
        
        // Load English language
        loadLanguage("en");
        
        plugin.getLogger().info("Loaded " + languages.size() + " language files");
    }
    
    /**
     * Load a specific language file
     * 
     * @param language Language code (e.g., "tr", "en")
     */
    private void loadLanguage(String language) {
        try {
            File langDir = new File(plugin.getDataFolder(), "lang");
            if (!langDir.exists()) {
                langDir.mkdirs();
            }
            
            File langFile = new File(langDir, language + ".yml");
            
            // Create default language file if it doesn't exist
            if (!langFile.exists()) {
                InputStream resource = plugin.getResource("lang/" + language + ".yml");
                if (resource != null) {
                    plugin.saveResource("lang/" + language + ".yml", false);
                }
            }
            
            if (langFile.exists()) {
                FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
                
                // Load defaults from resource
                InputStream defConfigStream = plugin.getResource("lang/" + language + ".yml");
                if (defConfigStream != null) {
                    YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defConfigStream, StandardCharsets.UTF_8)
                    );
                    langConfig.setDefaults(defConfig);
                }
                
                languages.put(language, langConfig);
                plugin.getLogger().info("Loaded language: " + language);
            } else {
                plugin.getLogger().warning("Language file not found: " + language + ".yml");
            }
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load language: " + language, e);
        }
    }
    
    /**
     * Get a message from the language files
     * 
     * @param key Message key
     * @param language Language code
     * @param placeholders Key-value pairs for placeholder replacement
     * @return Formatted message
     */
    public String getMessage(String key, String language, Map<String, String> placeholders) {
        FileConfiguration langConfig = languages.get(language);
        
        // Fallback to default language if specified language not found
        if (langConfig == null) {
            langConfig = languages.get(defaultLanguage);
        }
        
        // Fallback to English if default language not found
        if (langConfig == null) {
            langConfig = languages.get("en");
        }
        
        // Return key if no language config found
        if (langConfig == null) {
            plugin.getLogger().warning("No language configuration found for key: " + key);
            return key;
        }
        
        String message = langConfig.getString(key);
        if (message == null) {
            plugin.getLogger().warning("Message not found for key: " + key + " in language: " + language);
            return key;
        }
        
        // Replace placeholders
        if (placeholders != null && !placeholders.isEmpty()) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return ColorUtils.colorize(message);
    }
    
    /**
     * Get a message using default language
     * 
     * @param key Message key
     * @param placeholders Key-value pairs for placeholder replacement
     * @return Formatted message
     */
    public String getMessage(String key, Map<String, String> placeholders) {
        return getMessage(key, defaultLanguage, placeholders);
    }
    
    /**
     * Get a message without placeholders
     * 
     * @param key Message key
     * @param language Language code
     * @return Formatted message
     */
    public String getMessage(String key, String language) {
        return getMessage(key, language, null);
    }
    
    /**
     * Get a message without placeholders using default language
     * 
     * @param key Message key
     * @return Formatted message
     */
    public String getMessage(String key) {
        return getMessage(key, defaultLanguage, null);
    }
    
    /**
     * Get a message for a specific player (uses player's preferred language if available)
     * 
     * @param key Message key
     * @param player Player to get message for
     * @param placeholders Key-value pairs for placeholder replacement
     * @return Formatted message
     */
    public String getPlayerMessage(String key, Player player, Map<String, String> placeholders) {
        // For now, use default language. In the future, this could check player's preferred language
        String playerLanguage = getPlayerLanguage(player);
        return getMessage(key, playerLanguage, placeholders);
    }
    
    /**
     * Get a message for a specific player without placeholders
     * 
     * @param key Message key
     * @param player Player to get message for
     * @return Formatted message
     */
    public String getPlayerMessage(String key, Player player) {
        return getPlayerMessage(key, player, null);
    }
    
    /**
     * Send a message to a player
     * 
     * @param player Player to send message to
     * @param key Message key
     * @param placeholders Key-value pairs for placeholder replacement
     */
    public void sendMessage(Player player, String key, Map<String, String> placeholders) {
        String message = getPlayerMessage(key, player, placeholders);
        player.sendMessage(message);
    }
    
    /**
     * Send a message to a player without placeholders
     * 
     * @param player Player to send message to
     * @param key Message key
     */
    public void sendMessage(Player player, String key) {
        sendMessage(player, key, null);
    }
    
    /**
     * Get the player's preferred language
     * For now, returns default language. Could be extended to store player preferences
     * 
     * @param player Player to get language for
     * @return Language code
     */
    private String getPlayerLanguage(Player player) {
        // TODO: Implement player language preferences
        // This could check a database table or player data file
        return defaultLanguage;
    }
    
    /**
     * Get the default language
     * 
     * @return Default language code
     */
    public String getDefaultLanguage() {
        return defaultLanguage;
    }
    
    /**
     * Check if a language is available
     * 
     * @param language Language code to check
     * @return True if language is available
     */
    public boolean isLanguageAvailable(String language) {
        return languages.containsKey(language);
    }
    
    /**
     * Get all available languages
     * 
     * @return Array of available language codes
     */
    public String[] getAvailableLanguages() {
        return languages.keySet().toArray(new String[0]);
    }
    
    /**
     * Reload all language files
     */
    public void reload() {
        languages.clear();
        loadLanguages();
    }
    
    /**
     * Helper method to create a placeholder map
     * 
     * @param keys Keys and values alternating
     * @return Map of placeholders
     */
    public static Map<String, String> placeholders(String... keys) {
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < keys.length; i += 2) {
            if (i + 1 < keys.length) {
                placeholders.put(keys[i], keys[i + 1]);
            }
        }
        return placeholders;
    }
}