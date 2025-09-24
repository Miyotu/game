package com.miyotu.cs2plugin.models;

import com.miyotu.cs2plugin.enums.Team;
import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {
    private final String name;
    private final Map<UUID, CS2Player> players;
    private final Map<Team, Set<UUID>> teams;
    private Location spawnLocation;
    private final int maxPlayers;
    private boolean gameStarted;
    
    public Lobby(String name, int maxPlayers) {
        this.name = name;
        this.maxPlayers = maxPlayers;
        this.players = new ConcurrentHashMap<>();
        this.teams = new EnumMap<>(Team.class);
        this.teams.put(Team.CT, ConcurrentHashMap.newKeySet());
        this.teams.put(Team.T, ConcurrentHashMap.newKeySet());
        this.gameStarted = false;
    }
    
    public boolean addPlayer(CS2Player player) {
        if (players.size() >= maxPlayers) {
            return false;
        }
        
        players.put(player.getUuid(), player);
        player.setCurrentLobby(this);
        return true;
    }
    
    public void removePlayer(UUID uuid) {
        CS2Player player = players.remove(uuid);
        if (player != null) {
            // Remove from team
            if (player.getTeam() != null) {
                teams.get(player.getTeam()).remove(uuid);
            }
            player.setCurrentLobby(null);
            player.setTeam(null);
        }
    }
    
    public boolean assignPlayerToTeam(UUID uuid, Team team) {
        CS2Player player = players.get(uuid);
        if (player == null) return false;
        
        // Remove from current team
        if (player.getTeam() != null) {
            teams.get(player.getTeam()).remove(uuid);
        }
        
        // Check if team is full
        Set<UUID> teamPlayers = teams.get(team);
        if (teamPlayers.size() >= 5) {
            return false;
        }
        
        // Add to new team
        teamPlayers.add(uuid);
        player.setTeam(team);
        return true;
    }
    
    public void autoBalanceTeams() {
        // Clear current teams
        teams.get(Team.CT).clear();
        teams.get(Team.T).clear();
        
        // Randomly assign players to teams
        List<UUID> playerList = new ArrayList<>(players.keySet());
        Collections.shuffle(playerList);
        
        for (int i = 0; i < playerList.size(); i++) {
            UUID playerId = playerList.get(i);
            Team team = (i % 2 == 0) ? Team.CT : Team.T;
            assignPlayerToTeam(playerId, team);
        }
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public Map<UUID, CS2Player> getPlayers() {
        return Collections.unmodifiableMap(players);
    }
    
    public Set<UUID> getTeamPlayers(Team team) {
        return Collections.unmodifiableSet(teams.get(team));
    }
    
    public Location getSpawnLocation() {
        return spawnLocation;
    }
    
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }
    
    public int getMaxPlayers() {
        return maxPlayers;
    }
    
    public int getCurrentPlayerCount() {
        return players.size();
    }
    
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    public boolean isEmpty() {
        return players.isEmpty();
    }
    
    public boolean isGameStarted() {
        return gameStarted;
    }
    
    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }
    
    public boolean canStart() {
        return players.size() >= 2 && !gameStarted;
    }
}