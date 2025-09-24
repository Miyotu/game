package com.miyotu.cs2plugin.models;

import com.miyotu.cs2plugin.enums.PlayerState;
import com.miyotu.cs2plugin.enums.Team;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CS2Player {
    private final UUID uuid;
    private final String name;
    private PlayerState state;
    private Team team;
    private Lobby currentLobby;
    private Player bukkitPlayer;
    
    public CS2Player(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.state = PlayerState.LOBBY;
        this.team = null;
        this.currentLobby = null;
    }
    
    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }
    
    public String getName() {
        return name;
    }
    
    public PlayerState getState() {
        return state;
    }
    
    public void setState(PlayerState state) {
        this.state = state;
    }
    
    public Team getTeam() {
        return team;
    }
    
    public void setTeam(Team team) {
        this.team = team;
    }
    
    public Lobby getCurrentLobby() {
        return currentLobby;
    }
    
    public void setCurrentLobby(Lobby currentLobby) {
        this.currentLobby = currentLobby;
    }
    
    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }
    
    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }
    
    public boolean isInLobby() {
        return currentLobby != null;
    }
    
    public boolean hasTeam() {
        return team != null;
    }
}