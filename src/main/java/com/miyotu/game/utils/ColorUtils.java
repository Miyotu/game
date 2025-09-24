package com.miyotu.game.utils;

import org.bukkit.ChatColor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for handling color codes and text formatting
 */
public class ColorUtils {
    
    /**
     * Colorize a string by translating color codes
     * 
     * @param text The text to colorize
     * @return The colorized text
     */
    public static String colorize(String text) {
        if (text == null) return null;
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    
    /**
     * Colorize a list of strings
     * 
     * @param textList The list of text to colorize
     * @return The colorized list
     */
    public static List<String> colorize(List<String> textList) {
        if (textList == null) return null;
        return textList.stream()
                .map(ColorUtils::colorize)
                .collect(Collectors.toList());
    }
    
    /**
     * Strip color codes from text
     * 
     * @param text The text to strip colors from
     * @return The text without color codes
     */
    public static String stripColors(String text) {
        if (text == null) return null;
        return ChatColor.stripColor(colorize(text));
    }
    
    /**
     * Get a colored progress bar
     * 
     * @param current Current value
     * @param max Maximum value
     * @param length Length of the progress bar
     * @param completeColor Color for completed parts
     * @param incompleteColor Color for incomplete parts
     * @return Formatted progress bar
     */
    public static String getProgressBar(double current, double max, int length, 
                                      ChatColor completeColor, ChatColor incompleteColor) {
        double percentage = Math.min(current / max, 1.0);
        int completed = (int) (length * percentage);
        int remaining = length - completed;
        
        StringBuilder bar = new StringBuilder();
        bar.append(completeColor);
        for (int i = 0; i < completed; i++) {
            bar.append("█");
        }
        bar.append(incompleteColor);
        for (int i = 0; i < remaining; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
    
    /**
     * Get a centered text line
     * 
     * @param text Text to center
     * @param length Total length of the line
     * @param fillChar Character to fill with
     * @return Centered text
     */
    public static String getCenteredText(String text, int length, char fillChar) {
        if (text.length() >= length) return text;
        
        int padding = (length - stripColors(text).length()) / 2;
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < padding; i++) {
            result.append(fillChar);
        }
        result.append(text);
        for (int i = 0; i < padding; i++) {
            result.append(fillChar);
        }
        
        // Add extra character if odd length
        if (result.length() < length) {
            result.append(fillChar);
        }
        
        return result.toString();
    }
    
    /**
     * Format a large number with abbreviations (K, M, B, T)
     * 
     * @param number The number to format
     * @return Formatted number string
     */
    public static String formatNumber(double number) {
        if (number < 1000) {
            return String.format("%.0f", number);
        } else if (number < 1000000) {
            return String.format("%.1fK", number / 1000);
        } else if (number < 1000000000) {
            return String.format("%.1fM", number / 1000000);
        } else if (number < 1000000000000L) {
            return String.format("%.1fB", number / 1000000000);
        } else {
            return String.format("%.1fT", number / 1000000000000L);
        }
    }
    
    /**
     * Format time duration in a human-readable format
     * 
     * @param seconds Duration in seconds
     * @return Formatted time string
     */
    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " saniye";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            if (remainingSeconds == 0) {
                return minutes + " dakika";
            } else {
                return minutes + " dakika " + remainingSeconds + " saniye";
            }
        } else if (seconds < 86400) {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            if (remainingMinutes == 0) {
                return hours + " saat";
            } else {
                return hours + " saat " + remainingMinutes + " dakika";
            }
        } else {
            long days = seconds / 86400;
            long remainingHours = (seconds % 86400) / 3600;
            if (remainingHours == 0) {
                return days + " gün";
            } else {
                return days + " gün " + remainingHours + " saat";
            }
        }
    }
    
    /**
     * Create a gradient effect between two colors
     * 
     * @param text Text to apply gradient to
     * @param startColor Starting color
     * @param endColor Ending color
     * @return Text with gradient effect
     */
    public static String createGradient(String text, ChatColor startColor, ChatColor endColor) {
        if (text == null || text.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder();
        String cleanText = stripColors(text);
        
        for (int i = 0; i < cleanText.length(); i++) {
            // Simple alternating effect for now
            if (i % 2 == 0) {
                result.append(startColor);
            } else {
                result.append(endColor);
            }
            result.append(cleanText.charAt(i));
        }
        
        return result.toString();
    }
    
    /**
     * Get a rainbow colored text
     * 
     * @param text Text to apply rainbow colors to
     * @return Rainbow colored text
     */
    public static String getRainbowText(String text) {
        if (text == null || text.isEmpty()) return text;
        
        ChatColor[] colors = {
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
            ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE,
            ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE
        };
        
        StringBuilder result = new StringBuilder();
        String cleanText = stripColors(text);
        
        for (int i = 0; i < cleanText.length(); i++) {
            result.append(colors[i % colors.length]);
            result.append(cleanText.charAt(i));
        }
        
        return result.toString();
    }
}