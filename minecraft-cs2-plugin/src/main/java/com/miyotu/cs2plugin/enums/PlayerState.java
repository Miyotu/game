package com.miyotu.cs2plugin.enums;

public enum PlayerState {
    LOBBY("In Lobby"),
    WAITING("Waiting for Game"),
    PLAYING("In Game"),
    SPECTATING("Spectating");
    
    private final String displayName;
    
    PlayerState(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}