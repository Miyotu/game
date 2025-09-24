package com.miyotu.cs2plugin.managers;

import com.miyotu.cs2plugin.CS2Plugin;
import com.miyotu.cs2plugin.enums.GameState;
import com.miyotu.cs2plugin.enums.Team;
import com.miyotu.cs2plugin.models.CS2Player;
import com.miyotu.cs2plugin.models.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class RoundManager {
    private final CS2Plugin plugin;
    private final Map<String, GameState> lobbyStates = new HashMap<>();
    private final Map<String, Integer> roundNumbers = new HashMap<>();
    private final Map<String, Integer> ctScores = new HashMap<>();
    private final Map<String, Integer> tScores = new HashMap<>();
    private final Map<String, BukkitTask> roundTasks = new HashMap<>();
    private final Map<String, List<Location>> ctSpawns = new HashMap<>();
    private final Map<String, List<Location>> tSpawns = new HashMap<>();
    
    // Round Settings
    private static final int FREEZE_TIME = 15; // seconds
    private static final int ROUND_TIME = 115; // seconds (1:55)
    private static final int ROUND_END_TIME = 7; // seconds
    private static final int MAX_ROUNDS = 30;
    private static final int ROUNDS_TO_WIN = 16;
    
    public RoundManager(CS2Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void startMatch(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        // Initialize match
        lobbyStates.put(lobbyName, GameState.WARMUP);
        roundNumbers.put(lobbyName, 1);
        ctScores.put(lobbyName, 0);
        tScores.put(lobbyName, 0);
        
        // Setup spawn points
        setupDefaultSpawns(lobbyName, lobby.getSpawnLocation());
        
        // Start warmup
        startWarmup(lobbyName);
    }
    
    private void startWarmup(String lobbyName) {
        lobbyStates.put(lobbyName, GameState.WARMUP);
        
        broadcastToLobby(lobbyName, "§e⚡ WARMUP PHASE - Practice your aim!");
        broadcastToLobby(lobbyName, "§7Type §f/ready §7when all players are ready");
        
        // Spawn all players
        respawnAllPlayers(lobbyName);
        
        // Enable infinite respawn during warmup
        BukkitTask warmupTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Keep respawning dead players in warmup
                respawnAllPlayers(lobbyName);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Every second
        
        roundTasks.put(lobbyName + "_warmup", warmupTask);
    }
    
    public void startRound(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        int roundNum = roundNumbers.getOrDefault(lobbyName, 1);
        
        // Check for halftime
        if (roundNum == 16) {
            handleHalftime(lobbyName);
            return;
        }
        
        // Check for match end
        int ctScore = ctScores.getOrDefault(lobbyName, 0);
        int tScore = tScores.getOrDefault(lobbyName, 0);
        
        if (ctScore >= ROUNDS_TO_WIN || tScore >= ROUNDS_TO_WIN) {
            endMatch(lobbyName);
            return;
        }
        
        // Start freeze time
        startFreezeTime(lobbyName);
    }
    
    private void startFreezeTime(String lobbyName) {
        lobbyStates.put(lobbyName, GameState.FREEZE_TIME);
        
        // Spawn all players
        respawnAllPlayers(lobbyName);
        
        // Give starting weapons and money
        giveStartingEquipment(lobbyName);
        
        // Freeze time countdown
        BukkitTask freezeTask = new BukkitRunnable() {
            int timeLeft = FREEZE_TIME;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    startLiveRound(lobbyName);
                    this.cancel();
                    return;
                }
                
                if (timeLeft <= 5 || timeLeft % 5 == 0) {
                    broadcastToLobby(lobbyName, "§e⏰ Freeze Time: §f" + timeLeft + "s");
                    playSoundToLobby(lobbyName, Sound.BLOCK_NOTE_BLOCK_PLING);
                }
                
                // Prevent movement during freeze time
                freezeAllPlayers(lobbyName);
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        roundTasks.put(lobbyName + "_freeze", freezeTask);
    }
    
    private void startLiveRound(String lobbyName) {
        lobbyStates.put(lobbyName, GameState.LIVE);
        
        // Unfreeze players
        unfreezeAllPlayers(lobbyName);
        
        broadcastToLobby(lobbyName, "§a🚀 ROUND START! GO GO GO!");
        playSoundToLobby(lobbyName, Sound.ENTITY_ENDER_DRAGON_GROWL);
        
        // Round timer
        BukkitTask roundTask = new BukkitRunnable() {
            int timeLeft = ROUND_TIME;
            
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Time up - CT wins if bomb not planted
                    endRound(lobbyName, Team.CT, "Time expired");
                    this.cancel();
                    return;
                }
                
                // Show timer warnings
                if (timeLeft == 60) {
                    broadcastToLobby(lobbyName, "§c⚠️ 1 minute remaining!");
                } else if (timeLeft == 30) {
                    broadcastToLobby(lobbyName, "§c⚠️ 30 seconds remaining!");
                } else if (timeLeft <= 10 && timeLeft > 0) {
                    broadcastToLobby(lobbyName, "§c" + timeLeft);
                    playSoundToLobby(lobbyName, Sound.BLOCK_NOTE_BLOCK_BASS);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
        
        roundTasks.put(lobbyName + "_round", roundTask);
    }
    
    public void endRound(String lobbyName, Team winner, String reason) {
        // Cancel any running tasks
        cancelRoundTasks(lobbyName);
        
        lobbyStates.put(lobbyName, GameState.ROUND_END);
        
        // Update scores
        if (winner == Team.CT) {
            ctScores.put(lobbyName, ctScores.getOrDefault(lobbyName, 0) + 1);
        } else if (winner == Team.T) {
            tScores.put(lobbyName, tScores.getOrDefault(lobbyName, 0) + 1);
        }
        
        // Show round end
        int ctScore = ctScores.getOrDefault(lobbyName, 0);
        int tScore = tScores.getOrDefault(lobbyName, 0);
        
        broadcastToLobby(lobbyName, "§6╔══════════════════╗");
        broadcastToLobby(lobbyName, "§6║ §f" + winner.name() + " WINS! §6║");
        broadcastToLobby(lobbyName, "§6║ §7" + reason + " §6║");
        broadcastToLobby(lobbyName, "§6║ §9CT: §f" + ctScore + " §c| §eT: §f" + tScore + " §6║");
        broadcastToLobby(lobbyName, "§6╚══════════════════╝");
        
        // Play win sound
        if (winner == Team.CT) {
            playSoundToLobby(lobbyName, Sound.ENTITY_PLAYER_LEVELUP);
        } else {
            playSoundToLobby(lobbyName, Sound.ENTITY_GHAST_SCREAM);
        }
        
        // Award money for round result
        awardRoundMoney(lobbyName, winner);
        
        // Schedule next round
        BukkitTask nextRoundTask = new BukkitRunnable() {
            @Override
            public void run() {
                roundNumbers.put(lobbyName, roundNumbers.getOrDefault(lobbyName, 1) + 1);
                startRound(lobbyName);
            }
        }.runTaskLater(plugin, ROUND_END_TIME * 20L);
        
        roundTasks.put(lobbyName + "_next", nextRoundTask);
    }
    
    private void handleHalftime(String lobbyName) {
        broadcastToLobby(lobbyName, "§6🔄 HALFTIME! Teams are switching sides...");
        
        // Switch all players' teams
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby != null) {
            for (Player player : lobby.getPlayers()) {
                CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
                if (cs2Player != null) {
                    Team currentTeam = cs2Player.getTeam();
                    Team newTeam = (currentTeam == Team.CT) ? Team.T : Team.CT;
                    cs2Player.setTeam(newTeam);
                    
                    player.sendMessage("§aYou are now on team: §f" + newTeam.name());
                }
            }
        }
        
        // Continue with next round
        BukkitTask halftimeTask = new BukkitRunnable() {
            @Override
            public void run() {
                startRound(lobbyName);
            }
        }.runTaskLater(plugin, 5 * 20L); // 5 second break
        
        roundTasks.put(lobbyName + "_halftime", halftimeTask);
    }
    
    private void endMatch(String lobbyName) {
        cancelRoundTasks(lobbyName);
        
        int ctScore = ctScores.getOrDefault(lobbyName, 0);
        int tScore = tScores.getOrDefault(lobbyName, 0);
        Team winner = (ctScore > tScore) ? Team.CT : Team.T;
        
        broadcastToLobby(lobbyName, "§6╔════════════════════╗");
        broadcastToLobby(lobbyName, "§6║ §lMATCH FINISHED! §6║");
        broadcastToLobby(lobbyName, "§6║ §f" + winner.name() + " VICTORY! §6║");
        broadcastToLobby(lobbyName, "§6║ §9CT: §f" + ctScore + " §c- §eT: §f" + tScore + " §6║");
        broadcastToLobby(lobbyName, "§6╚════════════════════╝");
        
        playSoundToLobby(lobbyName, Sound.UI_TOAST_CHALLENGE_COMPLETE);
        
        // Reset lobby state
        lobbyStates.put(lobbyName, GameState.ENDED);
        
        // Return players to main lobby after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
                if (lobby != null) {
                    for (Player player : new ArrayList<>(lobby.getPlayers())) {
                        plugin.getLobbyManager().removePlayerFromLobby(player);
                        player.sendMessage("§7Match ended. You have been returned to the main lobby.");
                    }
                }
            }
        }.runTaskLater(plugin, 10 * 20L);
    }
    
    private void respawnAllPlayers(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        for (Player player : lobby.getPlayers()) {
            CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
            if (cs2Player != null && cs2Player.getTeam() != Team.SPECTATOR) {
                respawnPlayer(player, lobbyName);
            }
        }
    }
    
    private void respawnPlayer(Player player, String lobbyName) {
        CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
        if (cs2Player == null) return;
        
        Location spawnLocation = getSpawnLocation(lobbyName, cs2Player.getTeam());
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.setHealth(20.0);
            player.setFoodLevel(20);
            player.getInventory().clear();
            
            // Give spawn protection
            giveSpawnProtection(player);
        }
    }
    
    private Location getSpawnLocation(String lobbyName, Team team) {
        List<Location> spawns = (team == Team.CT) ? 
            ctSpawns.get(lobbyName) : tSpawns.get(lobbyName);
        
        if (spawns == null || spawns.isEmpty()) {
            // Fallback to lobby spawn
            Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
            return lobby != null ? lobby.getSpawnLocation() : null;
        }
        
        // Return random spawn from team spawns
        Random random = new Random();
        return spawns.get(random.nextInt(spawns.size()));
    }
    
    private void setupDefaultSpawns(String lobbyName, Location lobbySpawn) {
        List<Location> ctSpawnList = new ArrayList<>();
        List<Location> tSpawnList = new ArrayList<>();
        
        // Generate spawn points around lobby spawn
        for (int i = 0; i < 5; i++) {
            // CT spawns (north side)
            Location ctSpawn = lobbySpawn.clone().add(i * 3 - 6, 0, -10);
            ctSpawnList.add(ctSpawn);
            
            // T spawns (south side)  
            Location tSpawn = lobbySpawn.clone().add(i * 3 - 6, 0, 10);
            tSpawnList.add(tSpawn);
        }
        
        ctSpawns.put(lobbyName, ctSpawnList);
        tSpawns.put(lobbyName, tSpawnList);
    }
    
    private void giveStartingEquipment(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        for (Player player : lobby.getPlayers()) {
            CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
            if (cs2Player != null && cs2Player.getTeam() != Team.SPECTATOR) {
                // Give starting pistol based on team
                if (cs2Player.getTeam() == Team.CT) {
                    player.getInventory().addItem(plugin.getWeaponManager().createWeapon("USP-S"));
                } else {
                    player.getInventory().addItem(plugin.getWeaponManager().createWeapon("GLOCK"));
                }
                
                // Give starting money if first round
                if (roundNumbers.getOrDefault(lobbyName, 1) == 1) {
                    plugin.getEconomyManager().setMoney(player, 800);
                }
            }
        }
    }
    
    private void freezeAllPlayers(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        for (Player player : lobby.getPlayers()) {
            // Prevent movement by teleporting back to spawn if they move
            CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
            if (cs2Player != null) {
                cs2Player.setFrozen(true);
            }
        }
    }
    
    private void unfreezeAllPlayers(String lobbyName) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        for (Player player : lobby.getPlayers()) {
            CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
            if (cs2Player != null) {
                cs2Player.setFrozen(false);
            }
        }
    }
    
    private void giveSpawnProtection(Player player) {
        // 3 seconds of spawn protection
        new BukkitRunnable() {
            int timeLeft = 3;
            
            @Override
            public void run() {
                if (timeLeft <= 0 || !player.isOnline()) {
                    // Remove protection
                    CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
                    if (cs2Player != null) {
                        cs2Player.setSpawnProtected(false);
                    }
                    this.cancel();
                    return;
                }
                
                // Set protection
                CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
                if (cs2Player != null) {
                    cs2Player.setSpawnProtected(true);
                }
                
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }
    
    private void awardRoundMoney(String lobbyName, Team winner) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby == null) return;
        
        for (Player player : lobby.getPlayers()) {
            CS2Player cs2Player = plugin.getPlayerManager().getCS2Player(player);
            if (cs2Player != null && cs2Player.getTeam() != Team.SPECTATOR) {
                int currentMoney = plugin.getEconomyManager().getMoney(player);
                
                if (cs2Player.getTeam() == winner) {
                    // Winner gets $3250
                    plugin.getEconomyManager().setMoney(player, currentMoney + 3250);
                    player.sendMessage("§a+$3,250 §7(Round Win)");
                } else {
                    // Loser gets loss bonus ($1400-$2900)
                    int lossBonus = calculateLossBonus(lobbyName, cs2Player.getTeam());
                    plugin.getEconomyManager().setMoney(player, currentMoney + lossBonus);
                    player.sendMessage("§e+$" + lossBonus + " §7(Loss Bonus)");
                }
            }
        }
    }
    
    private int calculateLossBonus(String lobbyName, Team team) {
        // Simplified loss bonus calculation
        int consecutiveLosses = getConsecutiveLosses(lobbyName, team);
        return Math.min(1400 + (consecutiveLosses * 500), 2900);
    }
    
    private int getConsecutiveLosses(String lobbyName, Team team) {
        // TODO: Track consecutive losses per team
        return 0; // Placeholder
    }
    
    private void cancelRoundTasks(String lobbyName) {
        String[] taskKeys = {"_warmup", "_freeze", "_round", "_next", "_halftime"};
        
        for (String key : taskKeys) {
            BukkitTask task = roundTasks.get(lobbyName + key);
            if (task != null) {
                task.cancel();
                roundTasks.remove(lobbyName + key);
            }
        }
    }
    
    private void broadcastToLobby(String lobbyName, String message) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby != null) {
            for (Player player : lobby.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }
    
    private void playSoundToLobby(String lobbyName, Sound sound) {
        Lobby lobby = plugin.getLobbyManager().getLobby(lobbyName);
        if (lobby != null) {
            for (Player player : lobby.getPlayers()) {
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            }
        }
    }
    
    // Getters
    public GameState getGameState(String lobbyName) {
        return lobbyStates.getOrDefault(lobbyName, GameState.WAITING);
    }
    
    public int getRoundNumber(String lobbyName) {
        return roundNumbers.getOrDefault(lobbyName, 1);
    }
    
    public int getCTScore(String lobbyName) {
        return ctScores.getOrDefault(lobbyName, 0);
    }
    
    public int getTScore(String lobbyName) {
        return tScores.getOrDefault(lobbyName, 0);
    }
    
    public boolean isInBuyTime(String lobbyName) {
        GameState state = getGameState(lobbyName);
        return state == GameState.FREEZE_TIME || 
               (state == GameState.LIVE && getRoundTime(lobbyName) <= 15);
    }
    
    private int getRoundTime(String lobbyName) {
        // TODO: Track actual round time remaining
        return ROUND_TIME; // Placeholder
    }
}