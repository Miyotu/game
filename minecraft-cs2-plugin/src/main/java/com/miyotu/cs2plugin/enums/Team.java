package com.miyotu.cs2plugin.enums;

import org.bukkit.ChatColor;

public enum Team {
    CT("Counter-Terrorists", ChatColor.BLUE, "CT"),
    T("Terrorists", ChatColor.RED, "T");
    
    private final String displayName;
    private final ChatColor color;
    private final String shortName;
    
    Team(String displayName, ChatColor color, String shortName) {
        this.displayName = displayName;
        this.color = color;
        this.shortName = shortName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public String getShortName() {
        return shortName;
    }
    
    public String getColoredName() {
        return color + displayName + ChatColor.RESET;
    }
    
    public static Team fromString(String teamStr) {
        if (teamStr == null) return null;
        
        String lower = teamStr.toLowerCase();
        switch (lower) {
            case "ct":
            case "counter-terrorists":
            case "counter":
                return CT;
            case "t":
            case "terrorists":
            case "terrorist":
                return T;
            default:
                return null;
        }
    }
}